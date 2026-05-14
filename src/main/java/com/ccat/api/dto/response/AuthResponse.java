package com.ccat.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT token pair returned after successful authentication")
public record AuthResponse(

        @Schema(description = "Short-lived access token (1h) — send as 'Authorization: Bearer <token>'",
                example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Long-lived refresh token (7 days) — use POST /auth/refresh to get a new access token")
        String refreshToken,

        @Schema(description = "Token type — always 'Bearer'", example = "Bearer")
        String tokenType,

        @Schema(description = "Access token validity in seconds", example = "3600")
        long expiresIn,

        @Schema(description = "Authenticated user profile")
        UserResponse user
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresInSeconds, UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInSeconds, user);
    }
}
