package com.ccat.api.dto.request;

import com.ccat.api.model.enums.AdviceCategory;
import com.ccat.api.model.enums.AdvicePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdviceCardCreateRequest(
        @NotBlank String strCode,
        @NotBlank String emTriggerRule,
        @NotNull AdviceCategory emCategory,
        String strTargetDomainCode,
        AdvicePriority emPriority,
        @NotBlank String strTitle,
        @NotBlank String strContent,
        String strActionLabel,
        String strActionUrl
) {}
