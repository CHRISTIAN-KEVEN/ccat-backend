package com.ccat.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserAdviceFeedbackRequest(
        @NotNull Long lgUserAdviceId,
        Boolean bWasHelpful,
        String strUserNote,
        Integer intTimeSpentMs
) {}
