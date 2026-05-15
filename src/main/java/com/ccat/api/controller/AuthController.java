package com.ccat.api.controller;

import com.ccat.api.dto.request.*;
import com.ccat.api.dto.response.AuthResponse;
import com.ccat.api.dto.response.UserResponse;
import com.ccat.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Account creation and session management")
public class AuthController {

    private final AuthService authService;

    // -------------------------------------------------------------------------
    // Public
    // -------------------------------------------------------------------------

    @Operation(summary = "Register", description = "Creates an account and sends a verification email.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Email already registered",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Verify email",
               description = "Validates the 6-digit OTP sent by email after registration. Expires after 24h.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email verified"),
            @ApiResponse(responseCode = "422", description = "Invalid or expired OTP",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirements
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Resend verification email",
               description = "Resends the verification link. Reuses the existing token if still valid, generates a new one otherwise. Always returns 202.")
    @ApiResponse(responseCode = "202", description = "Request accepted")
    @SecurityRequirements
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Login", description = "Returns an access token (1h) and a refresh token (7 days).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Account suspended or deleted",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Sign in with Google", description = "Verifies a Google OAuth access token and returns a CCAT session.")
    @SecurityRequirements
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request.strAccessToken()));
    }

    @Operation(summary = "Refresh access token",
               description = "Exchanges a valid refresh token for a new access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New access token issued",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(summary = "Forgot password", description = "Sends a 6-digit OTP. Always returns 202.")
    @ApiResponse(responseCode = "202", description = "OTP sent if the email exists")
    @SecurityRequirements
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Reset password", description = "Validates the OTP and sets a new password. Invalidates all sessions.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password reset"),
            @ApiResponse(responseCode = "422", description = "Invalid or expired OTP",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirements
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Authenticated
    // -------------------------------------------------------------------------

    @Operation(summary = "Get current user", description = "Returns the profile of the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(authService.getMe(principal.getUsername()));
    }

    @Operation(summary = "Update profile")
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(authService.updateMe(principal.getUsername(), request));
    }

    @Operation(summary = "Change password",
               description = "Changes the password of the authenticated user. Invalidates all active sessions.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "401", description = "Current password incorrect",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getUsername(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Logout", description = "Invalidates the refresh token. Client must discard the access token.")
    @ApiResponse(responseCode = "204", description = "Logged out")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails principal) {
        authService.logout(principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
