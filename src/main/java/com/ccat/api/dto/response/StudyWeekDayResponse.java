package com.ccat.api.dto.response;

public record StudyWeekDayResponse(
    String strDay,
    int intDate,
    int intSessions,
    int intDone,
    boolean bIsToday,
    boolean bIsWeekend
) {}
