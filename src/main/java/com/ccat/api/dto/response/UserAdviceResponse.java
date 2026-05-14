package com.ccat.api.dto.response;

import java.time.LocalDateTime;

public record UserAdviceResponse(
        Long lgId,
        Long lgUserId,
        Long lgResultId,
        AdviceCardResponse adviceCard,
        String emTriggerMatched,
        Integer intDisplayRank,
        Boolean bWasRead,
        Boolean bWasHelpful,
        Integer intTimeSpentMs,
        String strUserNote,
        LocalDateTime dtShown,
        LocalDateTime dtRead
) {}
