package com.ccat.api.dto.response;

public record DomainPerformanceResponse(
        Long lgId,
        String strDomainCode,
        String strDomainLabel,
        Integer intCorrectCount,
        Integer intWrongCount,
        Integer intSkippedCount,
        Integer intTotalCount,
        Double dbAccuracyPercent,
        Double dbAvgResponseMs,
        Double dbAvgCorrectMs,
        Double dbAvgWrongMs,
        String emDifficultyBreakdown,
        Integer intRankInSession
) {}
