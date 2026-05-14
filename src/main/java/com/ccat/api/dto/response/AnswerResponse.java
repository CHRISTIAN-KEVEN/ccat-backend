package com.ccat.api.dto.response;

public record AnswerResponse(
        Long lgId,
        String strAnswerText,
        String strAnswerLabel,
        Boolean bIsCorrect,
        String strExplanation,
        Integer intSortOrder
) {}
