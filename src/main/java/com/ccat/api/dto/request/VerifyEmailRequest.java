package com.ccat.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to verify email address using the OTP received after registration")
public record VerifyEmailRequest(

        @Schema(description = "Email address used during registration", example = "alice@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Email
        String strEmail,

        @Schema(description = "6-digit OTP received by email", example = "382910",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(min = 6, max = 6)
        String strOtp
) {}
