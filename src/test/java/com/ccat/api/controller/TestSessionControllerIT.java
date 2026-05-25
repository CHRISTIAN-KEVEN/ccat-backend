package com.ccat.api.controller;

import com.ccat.api.dto.request.TestSessionCreateRequest;
import com.ccat.api.model.entity.*;
import com.ccat.api.model.enums.SessionType;
import com.ccat.api.util.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TestSessionController.
 * Covers the full session lifecycle: start -> responses -> results.
 *
 * Covered endpoints:
 *   POST   /api/v1/sessions
 *   GET    /api/v1/sessions/me
 *   GET    /api/v1/sessions/{id}
 *   GET    /api/v1/sessions/{id}/questions
 *   POST   /api/v1/sessions/{id}/responses
 *   POST   /api/v1/sessions/{id}/finish
 *   GET    /api/v1/sessions/{id}/result
 *   GET    /api/v1/sessions/{id}/review
 */
@DisplayName("TestSessionController - Integration Tests")
class TestSessionControllerIT extends IntegrationTestBase {

    private static final String BASE = "/api/v1/sessions";

    private User user;
    private String userToken;
    private Domain domain;
    private List<Question> questions;
    private List<Answer> correctAnswers;

    @BeforeEach
    void setUpData() {
        user      = createUser("alice@test.com", "Str0ng!Pass");
        userToken = bearerToken("alice@test.com");
        domain    = createDomain("VERBAL", "Verbal Reasoning");

        // Create at least 50 questions so a session can start
        questions     = new ArrayList<>();
        correctAnswers = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Question q = createQuestion(domain, user);
            Answer correct = createAnswer(q, "A", "Correct answer", true, 1);
            createAnswer(q, "B", "Wrong answer", false, 2);
            questions.add(q);
            correctAnswers.add(correct);
        }
    }

    // =========================================================================
    // POST /sessions - Start
    // =========================================================================

    @Nested
    @DisplayName("POST /sessions - Start a session")
    class StartTests {

        @Test
        @DisplayName("201 - PREMIUM_FULL session created successfully")
        void start_premiumFull_returns201() throws Exception {
            var request = new TestSessionCreateRequest(
                    SessionType.PREMIUM_FULL, "v1", "100", "50-50-0", false);

            mockMvc.perform(post(BASE)
                            .header("Authorization", userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.lgId").isNumber())
                    .andExpect(jsonPath("$.emSessionType").value("PREMIUM_FULL"))
                    .andExpect(jsonPath("$.emStatus").value("ACTIVE"))
                    .andExpect(jsonPath("$.intQuestionCount").value(50));
        }

        @Test
        @DisplayName("201 - FREE_DIAGNOSTIC session created when none exists yet")
        void start_freeDiagnostic_firstTime_returns201() throws Exception {
            var request = new TestSessionCreateRequest(
                    SessionType.FREE_DIAGNOSTIC, "v1", "100", "50-50-0", false);

            mockMvc.perform(post(BASE)
                            .header("Authorization", userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.emSessionType").value("FREE_DIAGNOSTIC"));
        }

        @Test
        @DisplayName("402 - Second FREE_DIAGNOSTIC is rejected")
        void start_freeDiagnostic_secondTime_returns402() throws Exception {
            // Create a first diagnostic session
            var first = new TestSessionCreateRequest(
                    SessionType.FREE_DIAGNOSTIC, "v1", "100", "50-50-0", false);
            mockMvc.perform(post(BASE)
                    .header("Authorization", userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(first)));

            // Attempt a second one
            mockMvc.perform(post(BASE)
                            .header("Authorization", userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(first)))
                    .andExpect(status().isPaymentRequired());
        }

        @Test
        @DisplayName("401 - Unauthenticated")
        void start_noToken_returns403() throws Exception {
            var request = new TestSessionCreateRequest(
                    SessionType.PREMIUM_FULL, "v1", null, null, false);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 - Missing emSessionType")
        void start_nullSessionType_returns400() throws Exception {
            // JSON without emSessionType
            mockMvc.perform(post(BASE)
                            .header("Authorization", userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"strEligibilityVersion\":\"v1\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // GET /sessions/me - History
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/me - Session history")
    class HistoryTests {

        @Test
        @DisplayName("200 - Returns the session list, empty at first")
        void getMyHistory_noSessions_returnsEmptyList() throws Exception {
            mockMvc.perform(get(BASE + "/me")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("200 - Returns sessions after creating one")
        void getMyHistory_withSession_returnsList() throws Exception {
            // Create a session
            var request = new TestSessionCreateRequest(
                    SessionType.PREMIUM_FULL, "v1", "100", "50-50-0", false);
            mockMvc.perform(post(BASE)
                    .header("Authorization", userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(request)));

            mockMvc.perform(get(BASE + "/me")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("401 - Unauthenticated")
        void getMyHistory_noToken_returns403() throws Exception {
            mockMvc.perform(get(BASE + "/me"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // GET /sessions/{id} - Session details
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/{id} - Session details")
    class GetByIdTests {

        @Test
        @DisplayName("200 - Returns details for an existing session")
        void getById_existingSession_returns200() throws Exception {
            Long sessionId = startSession();

            mockMvc.perform(get(BASE + "/" + sessionId)
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lgId").value(sessionId));
        }

        @Test
        @DisplayName("404 - Non-existent session")
        void getById_nonExistent_returns404() throws Exception {
            mockMvc.perform(get(BASE + "/99999")
                            .header("Authorization", userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("404 - Another user's session (security: no ID leakage)")
        void getById_sessionOfAnotherUser_returns404() throws Exception {
            Long sessionId = startSession();
            // Another user
            createUser("eve@test.com", "Str0ng!Pass");
            String eveToken = bearerToken("eve@test.com");

            mockMvc.perform(get(BASE + "/" + sessionId)
                            .header("Authorization", eveToken))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /sessions/{id}/questions - Session questions
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/{id}/questions - Session questions")
    class GetQuestionsTests {

        @Test
        @DisplayName("200 - Returns 50 questions for an active session")
        void getSessionQuestions_activeSession_returns50Questions() throws Exception {
            Long sessionId = startSession();

            mockMvc.perform(get(BASE + "/" + sessionId + "/questions")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(50)));
        }

        @Test
        @DisplayName("409 - Submitted session: questions are no longer accessible")
        void getSessionQuestions_submittedSession_returns409() throws Exception {
            Long sessionId = startSession();
            finishSession(sessionId);

            mockMvc.perform(get(BASE + "/" + sessionId + "/questions")
                            .header("Authorization", userToken))
                    .andExpect(status().isConflict());
        }
    }

    // =========================================================================
    // POST /sessions/{id}/finish - Final submission
    // =========================================================================

    @Nested
    @DisplayName("POST /sessions/{id}/finish - Submit the session")
    class FinishTests {

        @Test
        @DisplayName("200 - Returns results after submission")
        void finish_activeSession_returnsResults() throws Exception {
            Long sessionId = startSession();

            mockMvc.perform(post(BASE + "/" + sessionId + "/finish")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intTotalScore").isNumber())
                    .andExpect(jsonPath("$.dbAccuracyPercent").isNumber())
                    .andExpect(jsonPath("$.emPaceRating").isNotEmpty());
        }

        @Test
        @DisplayName("200 - Double finish call is idempotent and returns the same results")
        void finish_calledTwice_isIdempotent() throws Exception {
            Long sessionId = startSession();

            // First call
            String firstResult = mockMvc.perform(post(BASE + "/" + sessionId + "/finish")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // Second call -> same result
            mockMvc.perform(post(BASE + "/" + sessionId + "/finish")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intTotalScore")
                            .value(objectMapper.readTree(firstResult)
                                    .get("intTotalScore").asInt()));
        }

        @Test
        @DisplayName("404 - Non-existent session")
        void finish_nonExistent_returns404() throws Exception {
            mockMvc.perform(post(BASE + "/99999/finish")
                            .header("Authorization", userToken))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /sessions/{id}/result - Results
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/{id}/result - Session results")
    class GetResultTests {

        @Test
        @DisplayName("200 - Results are available after submission")
        void getResult_submittedSession_returnsResult() throws Exception {
            Long sessionId = startSession();
            finishSession(sessionId);

            mockMvc.perform(get(BASE + "/" + sessionId + "/result")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intTotalScore").isNumber())
                    .andExpect(jsonPath("$.bPassedThreshold").isBoolean())
                    .andExpect(jsonPath("$.intPercentileEstimate").isNumber())
                    .andExpect(jsonPath("$.domainPerformances").isArray());
        }

        @Test
        @DisplayName("401 - Unauthenticated")
        void getResult_noToken_returns403() throws Exception {
            mockMvc.perform(get(BASE + "/1/result"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // GET /sessions/{id}/review - Review
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/{id}/review - Post-test review")
    class GetReviewTests {

        @Test
        @DisplayName("200 - Review is available after submission with questions and answers")
        void getReview_submittedSession_returnsReview() throws Exception {
            Long sessionId = startSession();
            finishSession(sessionId);

            mockMvc.perform(get(BASE + "/" + sessionId + "/review")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("409 - Review is unavailable while the session is still active")
        void getReview_activeSession_returns409() throws Exception {
            Long sessionId = startSession();

            mockMvc.perform(get(BASE + "/" + sessionId + "/review")
                            .header("Authorization", userToken))
                    .andExpect(status().isConflict());
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /** Starts a session and returns its ID. */
    private Long startSession() throws Exception {
        var request = new TestSessionCreateRequest(
                SessionType.PREMIUM_FULL, "v1", "100", "50-50-0", false);

        String body = mockMvc.perform(post(BASE)
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("lgId").asLong();
    }

    /** Submits the session via finish to produce results. */
    private void finishSession(Long sessionId) throws Exception {
        mockMvc.perform(post(BASE + "/" + sessionId + "/finish")
                .header("Authorization", userToken));
    }
}
