package com.ccat.api.service;

import com.ccat.api.dto.request.ResponseSubmitRequest;
import com.ccat.api.dto.request.TestSessionCreateRequest;
import com.ccat.api.dto.response.QuestionResponse;
import com.ccat.api.dto.response.ResponseSubmitResponse;
import com.ccat.api.dto.response.TestResultResponse;
import com.ccat.api.dto.response.TestSessionResponse;

import java.util.List;

public interface TestSessionService {
    TestSessionResponse start(TestSessionCreateRequest request, String userEmail);
    ResponseSubmitResponse submitResponse(Long sessionId, ResponseSubmitRequest request, String userEmail);
    TestResultResponse finish(Long sessionId, boolean submittedByTimer, String userEmail);
    TestResultResponse getResult(Long sessionId, String userEmail);
    TestSessionResponse getById(Long sessionId, String userEmail);
    List<TestSessionResponse> getMyHistory(String userEmail);
    List<QuestionResponse> getSessionQuestions(Long sessionId, String userEmail);
}
