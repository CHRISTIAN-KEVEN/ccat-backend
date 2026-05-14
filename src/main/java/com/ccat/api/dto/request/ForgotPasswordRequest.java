package com.ccat.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Email address to send the OTP to")
public record ForgotPasswordRequest(

        @Schema(description = "Registered email address", example = "alice@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Email
        String strEmail
) {}
