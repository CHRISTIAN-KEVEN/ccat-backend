package com.ccat.api.dto.request;

public record UserUpdateRequest(
        String strFirstName,
        String strLastName,
        String strTimezone,
        String strLocale
) {}
