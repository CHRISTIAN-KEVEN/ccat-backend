package com.ccat.api.service;

import com.ccat.api.dto.request.UserAdviceFeedbackRequest;
import com.ccat.api.dto.response.UserAdviceResponse;
import com.ccat.api.model.entity.DomainPerformance;
import com.ccat.api.model.entity.TestResult;

import java.util.List;

public interface UserAdviceService {

    /** Called internally right after a session is finalized. Idempotent. */
    void generateForResult(TestResult result, List<DomainPerformance> perfs);

    /** Returns advices for a submitted session, ordered by display rank. */
    List<UserAdviceResponse> getBySession(Long sessionId, String userEmail);

    /** Marks an advice card as read by the user. */
    UserAdviceResponse markRead(Long userAdviceId, String userEmail);

    /** Records whether the advice was helpful and any notes from the user. */
    UserAdviceResponse submitFeedback(UserAdviceFeedbackRequest request, String userEmail);
}
