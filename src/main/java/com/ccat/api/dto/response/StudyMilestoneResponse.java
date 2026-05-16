package com.ccat.api.dto.response;

public record StudyMilestoneResponse(
    String strLabel,
    double dbTarget,
    double dbCurrent,
    String strUnit,
    String emStatus
) {}
