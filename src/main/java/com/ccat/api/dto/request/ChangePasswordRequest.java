package com.ccat.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to change the password of the authenticated user")
public record ChangePasswordRequest(

        @Schema(description = "Current password", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String strCurrentPassword,

        @Schema(description = "New password (min 8 characters)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(min = 8)
        String strNewPassword
) {}
