package com.ccat.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank @Email String strEmail,
        @NotBlank String strPassword
) {}
