package com.ccat.api.service;

import com.ccat.api.config.JwtProperties;
import com.ccat.api.dto.request.*;
import com.ccat.api.dto.request.VerifyEmailRequest;
import com.ccat.api.dto.response.AuthResponse;
import com.ccat.api.dto.response.UserResponse;
import com.ccat.api.exception.EmailAlreadyExistsException;
import com.ccat.api.exception.InvalidOtpException;
import com.ccat.api.exception.InvalidTokenException;
import com.ccat.api.mapper.UserMapper;
import com.ccat.api.model.entity.User;
import com.ccat.api.repository.UserRepository;
import com.ccat.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int OTP_TTL_MINUTES            = 60;
    private static final int VERIFICATION_TTL_HOURS     = 24;
    private static final SecureRandom SECURE_RANDOM     = new SecureRandom();

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final UserMapper            userMapper;
    private final AuthenticationManager authenticationManager;
    private final EmailService          emailService;
    private final JwtProperties         jwtProperties;

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------

    @Transactional
    public AuthResponse register(UserRegisterRequest request) {
        if (userRepository.existsByStrEmail(request.strEmail())) {
            throw new EmailAlreadyExistsException(request.strEmail());
        }

        User user = userMapper.toEntity(request);
        user.setStrUuid(UUID.randomUUID().toString());
        user.setStrPasswordHash(passwordEncoder.encode(request.strPassword()));
        applyNewVerificationToken(user);

        User saved = userRepository.save(user);
        // @Async — fires in a separate thread after this call returns, outside the transaction
        emailService.sendVerificationEmail(saved.getStrEmail(), saved.getStrVerificationToken());

        return buildAuthResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Email verification
    // -------------------------------------------------------------------------

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByStrEmail(request.strEmail())
                .orElseThrow(InvalidOtpException::new);

        boolean tokenMissing = user.getStrVerificationToken() == null || user.getDtVerificationTokenExpires() == null;
        boolean expired      = !tokenMissing && user.getDtVerificationTokenExpires().isBefore(LocalDateTime.now());
        boolean wrongOtp     = tokenMissing || !request.strOtp().equals(user.getStrVerificationToken());

        if (tokenMissing || expired || wrongOtp) throw new InvalidOtpException();

        user.setBEmailVerified(true);
        user.setStrVerificationToken(null);
        user.setDtVerificationTokenExpires(null);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerification(ForgotPasswordRequest request) {
        userRepository.findByStrEmail(request.strEmail()).ifPresent(user -> {
            if (user.getBEmailVerified()) return;

            boolean stillValid = user.getStrVerificationToken() != null
                    && user.getDtVerificationTokenExpires() != null
                    && user.getDtVerificationTokenExpires().isAfter(LocalDateTime.now());

            if (!stillValid) {
                applyNewVerificationToken(user);
                userRepository.save(user);
            }

            emailService.sendVerificationEmail(user.getStrEmail(), user.getStrVerificationToken());
        });
        // Always silent — anti-enumeration
    }

    // -------------------------------------------------------------------------
    // Me
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public UserResponse getMe(String email) {
        return userMapper.toResponse(
                userRepository.findByStrEmail(email).orElseThrow());
    }

    // -------------------------------------------------------------------------
    // Login / Logout / Refresh
    // -------------------------------------------------------------------------

    @Transactional
    public AuthResponse login(UserLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.strEmail(), request.strPassword()));

        User user = userRepository.findByStrEmail(request.strEmail()).orElseThrow();
        user.setIntLoginCount(user.getIntLoginCount() + 1);
        user.setDtLastLogin(LocalDateTime.now());

        return buildAuthResponse(userRepository.save(user));
    }

    @Transactional
    public void logout(String email) {
        userRepository.findByStrEmail(email).ifPresent(user -> {
            user.setStrRefreshToken(null);
            user.setDtRefreshTokenExpires(null);
            userRepository.save(user);
        });
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        User user = userRepository.findByStrRefreshToken(request.strRefreshToken())
                .orElseThrow(InvalidTokenException::new);

        if (user.getDtRefreshTokenExpires() == null
                || user.getDtRefreshTokenExpires().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException();
        }

        return AuthResponse.of(
                jwtService.generateToken(user.getStrEmail()),
                user.getStrRefreshToken(),
                jwtProperties.expirationMs() / 1000,
                userMapper.toResponse(user)
        );
    }

    // -------------------------------------------------------------------------
    // Change password
    // -------------------------------------------------------------------------

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByStrEmail(email).orElseThrow();

        if (!passwordEncoder.matches(request.strCurrentPassword(), user.getStrPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setStrPasswordHash(passwordEncoder.encode(request.strNewPassword()));
        user.setStrRefreshToken(null);
        user.setDtRefreshTokenExpires(null);
        userRepository.save(user);
    }

    // -------------------------------------------------------------------------
    // Forgot / Reset password
    // -------------------------------------------------------------------------

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByStrEmail(request.strEmail()).ifPresent(user -> {
            boolean stillValid = user.getStrResetToken() != null
                    && user.getDtResetTokenExpires() != null
                    && user.getDtResetTokenExpires().isAfter(LocalDateTime.now());

            String otp = stillValid ? user.getStrResetToken() : generateAndPersistOtp(user);
            emailService.sendOtp(user.getStrEmail(), otp);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByStrEmail(request.strEmail())
                .orElseThrow(InvalidOtpException::new);

        boolean tokenMissing = user.getStrResetToken() == null || user.getDtResetTokenExpires() == null;
        boolean expired      = !tokenMissing && user.getDtResetTokenExpires().isBefore(LocalDateTime.now());
        boolean wrongOtp     = tokenMissing || !request.strOtp().equals(user.getStrResetToken());

        if (tokenMissing || expired || wrongOtp) throw new InvalidOtpException();

        user.setStrPasswordHash(passwordEncoder.encode(request.strNewPassword()));
        user.setStrResetToken(null);
        user.setDtResetTokenExpires(null);
        user.setStrRefreshToken(null);
        user.setDtRefreshTokenExpires(null);
        userRepository.save(user);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private AuthResponse buildAuthResponse(User user) {
        String refreshToken = UUID.randomUUID().toString();
        user.setStrRefreshToken(refreshToken);
        user.setDtRefreshTokenExpires(
                LocalDateTime.now().plusSeconds(jwtProperties.refreshExpirationMs() / 1000));
        userRepository.save(user);

        return AuthResponse.of(
                jwtService.generateToken(user.getStrEmail()),
                refreshToken,
                jwtProperties.expirationMs() / 1000,
                userMapper.toResponse(user)
        );
    }

    private void applyNewVerificationToken(User user) {
        // OTP 6 chiffres — même mécanique que le reset password
        user.setStrVerificationToken(String.format("%06d", SECURE_RANDOM.nextInt(1_000_000)));
        user.setDtVerificationTokenExpires(LocalDateTime.now().plusHours(VERIFICATION_TTL_HOURS));
    }

    private String generateAndPersistOtp(User user) {
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        user.setStrResetToken(otp);
        user.setDtResetTokenExpires(LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES));
        userRepository.save(user);
        return otp;
    }
}
