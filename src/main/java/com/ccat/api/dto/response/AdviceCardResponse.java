package com.ccat.api.dto.response;

import com.ccat.api.model.enums.AdviceCategory;
import com.ccat.api.model.enums.AdvicePriority;

public record AdviceCardResponse(
        Long lgId,
        String strCode,
        String emTriggerRule,
        AdviceCategory emCategory,
        String strTargetDomainCode,
        AdvicePriority emPriority,
        String strTitle,
        String strContent,
        String strActionLabel,
        String strActionUrl,
        Boolean bActive
) {}
