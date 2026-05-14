package com.ccat.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @NotBlank @Email String strEmail,
        @NotBlank @Size(min = 8) String strPassword,
        String strFirstName,
        String strLastName,
        String strLocale
) {}
