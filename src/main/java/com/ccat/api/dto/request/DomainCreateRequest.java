package com.ccat.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DomainCreateRequest(
        @NotBlank String strDomainCode,
        @NotBlank String strLabel,
        String strDescription,
        String strIconUrl,
        Integer intDefaultRatio,
        Integer intSortOrder
) {}
