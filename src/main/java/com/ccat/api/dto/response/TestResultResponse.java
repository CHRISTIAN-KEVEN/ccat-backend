package com.ccat.api.dto.response;

import com.ccat.api.model.enums.PaceRating;
import java.time.LocalDateTime;
import java.util.List;

public record TestResultResponse(
        Long lgId,
        Long lgSessionId,
        Long lgUserId,
        Integer intTotalScore,
        Integer intQuestionCount,
        Integer intAnsweredCount,
        Integer intSkippedCount,
        Double dbAccuracyPercent,
        Double dbCompletionPercent,
        Integer intTimeUsedMs,
        Integer intTimeAvailableMs,
        Double dbAvgTimePerQMs,
        Double dbAvgTimeCorrectMs,
        Double dbAvgTimeWrongMs,
        String emWeakestDomain,
        String emStrongestDomain,
        PaceRating emPaceRating,
        Integer intPercentileEstimate,
        Boolean bPassedThreshold,
        String strAdviceTriggerCodes,
        LocalDateTime dtCalculated,
        List<DomainPerformanceResponse> domainPerformances
) {}
