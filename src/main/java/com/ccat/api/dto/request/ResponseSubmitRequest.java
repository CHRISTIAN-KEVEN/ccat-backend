package com.ccat.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record ResponseSubmitRequest(
        @NotNull Long lgSessionId,
        @NotNull Long lgQuestionId,
        Long lgAnswerId,
        @NotNull Integer intResponseTimeMs,
        @NotNull Integer intDisplayOrder,
        @NotNull String strAnswerOptionsOrder,
        Boolean bWasChanged,
        Integer intChangedCount
) {}
