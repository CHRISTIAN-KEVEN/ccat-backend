package com.ccat.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token used to obtain a new access token")
public record RefreshTokenRequest(

        @Schema(description = "Refresh token received at login or register", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String strRefreshToken
) {}
