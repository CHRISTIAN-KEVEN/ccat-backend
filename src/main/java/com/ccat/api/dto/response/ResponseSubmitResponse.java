package com.ccat.api.dto.response;

import java.time.LocalDateTime;

public record ResponseSubmitResponse(
        Long lgId,
        Long lgSessionId,
        Long lgQuestionId,
        Long lgAnswerId,
        Boolean bIsCorrect,
        Boolean bWasSkipped,
        Integer intResponseTimeMs,
        Integer intDisplayOrder,
        LocalDateTime dtAnswered
) {}
