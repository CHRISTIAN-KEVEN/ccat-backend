package com.ccat.api.dto.response;

import com.ccat.api.model.enums.SessionStatus;
import com.ccat.api.model.enums.SessionType;
import java.time.LocalDateTime;
import java.util.List;

public record TestSessionResponse(
        Long lgId,
        String strUuid,
        Long lgUserId,
        SessionType emSessionType,
        SessionStatus emStatus,
        Boolean bIsFreeTest,
        Integer intQuestionCount,
        Integer intDurationSeconds,
        String emDomainRatio,
        String emDifficultyMix,
        Integer intQuestionsAnswered,
        Integer intQuestionsSkipped,
        LocalDateTime dtStarted,
        LocalDateTime dtExpires,
        LocalDateTime dtSubmitted,
        List<Long> questionOrder
) {}
