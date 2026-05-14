package com.ccat.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerCreateRequest(
        @NotNull Long lgQuestionId,
        @NotBlank String strAnswerText,
        @NotBlank String strAnswerLabel,
        Boolean bIsCorrect,
        String strExplanation,
        Integer intSortOrder
) {}
