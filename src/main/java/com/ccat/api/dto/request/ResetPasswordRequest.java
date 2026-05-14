package com.ccat.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to reset the password using the OTP received by email")
public record ResetPasswordRequest(

        @Schema(description = "Email address that requested the reset", example = "alice@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Email
        String strEmail,

        @Schema(description = "6-digit OTP received by email", example = "482916", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(min = 6, max = 6)
        String strOtp,

        @Schema(description = "New password (min 8 characters)", example = "N3wStr0ng!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(min = 8)
        String strNewPassword
) {}
