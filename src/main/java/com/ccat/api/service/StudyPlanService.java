package com.ccat.api.service;

import com.ccat.api.dto.response.StudyPlanResponse;

public interface StudyPlanService {
    StudyPlanResponse getPlan(String userEmail);
    void completeDrill(String drillKey, String userEmail);
}
