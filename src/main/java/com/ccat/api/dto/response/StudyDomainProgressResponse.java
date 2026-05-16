package com.ccat.api.dto.response;

public record StudyDomainProgressResponse(
    String strName,
    int intPct,
    String strColor
) {}
