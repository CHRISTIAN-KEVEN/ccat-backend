package com.ccat.api.dto.request;

import com.ccat.api.model.enums.SessionType;
import jakarta.validation.constraints.NotNull;

public record TestSessionCreateRequest(
        @NotNull SessionType emSessionType,
        String strEligibilityVersion,
        String emDomainRatio,
        String emDifficultyMix,
        Boolean bAllowBacktrack
) {}
