package com.ccat.api.dto.response;

import com.ccat.api.model.enums.DomainStatus;
import java.time.LocalDateTime;

public record DomainResponse(
        String strDomainCode,
        String strLabel,
        String strDescription,
        String strIconUrl,
        DomainStatus emStatus,
        Integer intDefaultRatio,
        Integer intQuestionCount,
        Integer intSessionCount,
        Double dbAvgAccuracy,
        Boolean bShowInResults,
        Integer intSortOrder,
        LocalDateTime dtCreated,
        LocalDateTime dtUpdated
) {}
