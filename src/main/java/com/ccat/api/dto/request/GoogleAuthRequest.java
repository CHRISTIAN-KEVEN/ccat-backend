package com.ccat.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(@NotBlank String strAccessToken) {}
