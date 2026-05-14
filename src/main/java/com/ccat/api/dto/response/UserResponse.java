package com.ccat.api.dto.response;

import com.ccat.api.model.enums.UserRole;
import com.ccat.api.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Public user profile (internal lg_id excluded from API surface)")
public record UserResponse(

        @Schema(description = "Internal database ID — use strUuid for external references", example = "42")
        Long lgId,

        @Schema(description = "Public UUID — safe to expose in URLs and tokens", example = "550e8400-e29b-41d4-a716-446655440000")
        String strUuid,

        @Schema(description = "Email address", example = "alice@example.com")
        String strEmail,

        @Schema(description = "First name", example = "Alice")
        String strFirstName,

        @Schema(description = "Last name", example = "Dupont")
        String strLastName,

        @Schema(description = "Role assigned to this account")
        UserRole emRole,

        @Schema(description = "Current account status")
        UserStatus emStatus,

        @Schema(description = "Whether the email address has been verified", example = "false")
        Boolean bEmailVerified,

        @Schema(description = "IANA timezone identifier", example = "Europe/Paris")
        String strTimezone,

        @Schema(description = "BCP-47 locale code", example = "fr")
        String strLocale,

        @Schema(description = "Total number of successful logins", example = "7")
        Integer intLoginCount,

        @Schema(description = "Timestamp of the last successful login")
        LocalDateTime dtLastLogin,

        @Schema(description = "Account creation timestamp")
        LocalDateTime dtCreated
) {}
