package com.ccat.api.dto.request;

import com.ccat.api.model.enums.DomainStatus;

public record DomainUpdateRequest(
        String strLabel,
        String strDescription,
        String strIconUrl,
        DomainStatus emStatus,
        Integer intDefaultRatio,
        Integer intSortOrder,
        Boolean bShowInResults
) {}
