package com.ccat.api.dto.response;

import java.util.List;

public record StudyPlanResponse(
    List<StudyDrillResponse> todayDrills,
    List<StudyWeekDayResponse> weekPlan,
    List<StudyMilestoneResponse> milestones,
    List<StudyDomainProgressResponse> domainProgress,
    int intDaysToExam,
    int intStreak,
    int intWeekSessionsDone,
    int intWeekSessionsTotal
) {}
