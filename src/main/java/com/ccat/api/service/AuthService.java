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
import com.ccat.api.model.enums.UserStatus;
import com.ccat.api.repository.UserRepository;
import com.ccat.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long MAX_PROFILE_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final String PROFILE_IMAGE_PATH = "/uploads/profile-images/";

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public UserResponse updateMe(String email, UserUpdateRequest request) {
        User user = userRepository.findByStrEmail(email).orElseThrow();
        if (request.strFirstName() != null) user.setStrFirstName(request.strFirstName());
        if (request.strLastName()  != null) user.setStrLastName(request.strLastName());
        if (request.strTimezone()  != null) user.setStrTimezone(request.strTimezone());
        if (request.strLocale()    != null) user.setStrLocale(request.strLocale());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse uploadProfileImage(String email, MultipartFile file) {
        User user = userRepository.findByStrEmail(email).orElseThrow();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Profile image file is required");
        }
        if (file.getSize() > MAX_PROFILE_IMAGE_BYTES) {
            throw new IllegalArgumentException("Profile image must be 5 MB or smaller");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Profile image must be an image file");
        }

        String extension = extensionFor(contentType, file.getOriginalFilename());
        String fileName = "user_" + user.getLgId() + "_" + UUID.randomUUID() + extension;
        Path uploadDir = Paths.get("uploads", "profile-images").toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            deleteStoredProfileImage(user.getStrProfileImageUrl());
            user.setStrProfileImageUrl(normalizeBaseUrl(baseUrl) + PROFILE_IMAGE_PATH + fileName);
            return userMapper.toResponse(userRepository.save(user));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store profile image", e);
        }
    }

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
        assertUserCanAuthenticate(user);
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

        assertUserCanAuthenticate(user);

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
    // Google OAuth
    // -------------------------------------------------------------------------

    @Transactional
    @SuppressWarnings("unchecked")
    public AuthResponse googleLogin(String accessToken) {
        // Verify token and fetch user info from Google
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        Map<String, Object> info;
        try {
            info = rest.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            ).getBody();
        } catch (Exception e) {
            throw new InvalidTokenException();
        }

        if (info == null || info.get("email") == null || !Boolean.TRUE.equals(info.get("email_verified"))) {
            throw new InvalidTokenException();
        }

        String email     = (String) info.get("email");
        String googleId  = (String) info.get("sub");
        String firstName = (String) info.getOrDefault("given_name", "");
        String lastName  = (String) info.getOrDefault("family_name", "");

        User user = userRepository.findByStrEmail(email).orElseGet(() -> {
            User u = new User();
            u.setStrUuid(UUID.randomUUID().toString());
            u.setStrEmail(email);
            u.setStrFirstName(firstName);
            u.setStrLastName(lastName);
            u.setStrOauthProvider("google");
            u.setStrOauthId(googleId);
            u.setBEmailVerified(true);
            return userRepository.save(u);
        });

        // Link google to existing account if not yet linked
        if (user.getStrOauthProvider() == null) {
            user.setStrOauthProvider("google");
            user.setStrOauthId(googleId);
            user.setBEmailVerified(true);
        }
        assertUserCanAuthenticate(user);
        user.setIntLoginCount(user.getIntLoginCount() + 1);
        user.setDtLastLogin(LocalDateTime.now());

        return buildAuthResponse(userRepository.save(user));
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

    private String normalizeBaseUrl(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String extensionFor(String contentType, String originalFilename) {
        return switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> {
                if (originalFilename != null && originalFilename.contains(".")) {
                    yield originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
                }
                throw new IllegalArgumentException("Unsupported image format");
            }
        };
    }

    private void deleteStoredProfileImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        try {
            String path = URI.create(imageUrl).getPath();
            if (path == null || !path.contains(PROFILE_IMAGE_PATH)) return;
            String filename = path.substring(path.lastIndexOf('/') + 1);
            Path target = Paths.get("uploads", "profile-images", filename).toAbsolutePath().normalize();
            Files.deleteIfExists(target);
        } catch (Exception ignored) {
            // Ignore cleanup failures — they must not block profile updates.
        }
    }

    private void assertUserCanAuthenticate(User user) {
        if (user.getEmStatus() == UserStatus.SUSPENDED) {
            throw new LockedException("Your account has been suspended");
        }
        if (user.getEmStatus() == UserStatus.DELETED) {
            throw new DisabledException("Your account has been banned");
        }
    }
}
