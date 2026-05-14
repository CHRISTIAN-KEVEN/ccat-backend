package com.ccat.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to create a new user account")
public record UserRegisterRequest(

        @Schema(description = "User email address", example = "alice@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Email
        String strEmail,

        @Schema(description = "Plain-text password (min 8 characters — hashed with BCrypt before storage)", example = "Str0ng!Pass", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(min = 8)
        String strPassword,

        @Schema(description = "First name", example = "Alice")
        String strFirstName,

        @Schema(description = "Last name", example = "Dupont")
        String strLastName,

        @Schema(description = "BCP-47 locale code", example = "fr", defaultValue = "en")
        String strLocale
) {}
