package com.ccat.api.dto.response;

public record StudyDrillResponse(
    String strDrillKey,
    String strType,
    String strTitle,
    String strDuration,
    String emPriority,
    int intXp,
    boolean bDone
) {}
