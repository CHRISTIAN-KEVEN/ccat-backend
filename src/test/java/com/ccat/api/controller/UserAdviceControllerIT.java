package com.ccat.api.controller;

import com.ccat.api.dto.request.TestSessionCreateRequest;
import com.ccat.api.dto.request.UserAdviceFeedbackRequest;
import com.ccat.api.model.entity.*;
import com.ccat.api.model.enums.SessionType;
import com.ccat.api.util.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserAdviceController.
 * Verifies retrieval, read status, and feedback for post-test advice.
 *
 * Covered endpoints:
 *   GET   /api/v1/sessions/{sessionId}/advices
 *   PATCH /api/v1/advices/{id}/read
 *   PATCH /api/v1/advices/feedback
 */
@DisplayName("UserAdviceController - Integration Tests")
class UserAdviceControllerIT extends IntegrationTestBase {

    private User   user;
    private String userToken;
    private Long   submittedSessionId;

    @BeforeEach
    void setUpData() throws Exception {
        user      = createUser("alice@test.com", "Str0ng!Pass");
        userToken = bearerToken("alice@test.com");

        Domain domain = createDomain("VERBAL", "Verbal Reasoning");

        // 50 questions are required to start a session
        var questions = new ArrayList<Question>();
        for (int i = 0; i < 50; i++) {
            Question q = createQuestion(domain, user);
            createAnswer(q, "A", "Correct answer", true, 1);
            createAnswer(q, "B", "Wrong answer", false, 2);
            questions.add(q);
        }

        // Start and then submit a session to generate advice
        submittedSessionId = startAndFinishSession();
    }

    // =========================================================================
    // GET /sessions/{sessionId}/advices
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/{id}/advices - Advice retrieval")
    class GetAdvicesTests {

        @Test
        @DisplayName("200 - Returns the advice list, which may be empty")
        void getBySession_submittedSession_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/sessions/" + submittedSessionId + "/advices")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("401 - Unauthenticated")
        void getBySession_noToken_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/sessions/" + submittedSessionId + "/advices"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("404 - Non-existent session")
        void getBySession_nonExistentSession_returns404() throws Exception {
            mockMvc.perform(get("/api/v1/sessions/99999/advices")
                            .header("Authorization", userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("404 - Another user's session is inaccessible")
        void getBySession_sessionOfAnotherUser_returns404() throws Exception {
            createUser("eve@test.com", "Str0ng!Pass");
            String eveToken = bearerToken("eve@test.com");

            mockMvc.perform(get("/api/v1/sessions/" + submittedSessionId + "/advices")
                            .header("Authorization", eveToken))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // PATCH /advices/{id}/read
    // =========================================================================

    @Nested
    @DisplayName("PATCH /advices/{id}/read - Mark as read")
    class MarkReadTests {

        @Test
        @DisplayName("404 - Non-existent advice")
        void markRead_nonExistentAdvice_returns404() throws Exception {
            mockMvc.perform(patch("/api/v1/advices/99999/read")
                            .header("Authorization", userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("401 - Unauthenticated")
        void markRead_noToken_returns403() throws Exception {
            mockMvc.perform(patch("/api/v1/advices/1/read"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("404 - Another user's advice is inaccessible")
        void markRead_adviceOfAnotherUser_returns404() throws Exception {
            createUser("eve@test.com", "Str0ng!Pass");
            String eveToken = bearerToken("eve@test.com");

            mockMvc.perform(patch("/api/v1/advices/1/read")
                            .header("Authorization", eveToken))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // PATCH /advices/feedback
    // =========================================================================

    @Nested
    @DisplayName("PATCH /advices/feedback - Submit feedback")
    class FeedbackTests {

        @Test
        @DisplayName("404 - Non-existent advice")
        void submitFeedback_nonExistentAdvice_returns404() throws Exception {
            var request = new UserAdviceFeedbackRequest(99999L, true, "Helpful!", 2000);

            mockMvc.perform(patch("/api/v1/advices/feedback")
                            .header("Authorization", userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("401 - Unauthenticated")
        void submitFeedback_noToken_returns403() throws Exception {
            var request = new UserAdviceFeedbackRequest(1L, true, "Note", 1000);

            mockMvc.perform(patch("/api/v1/advices/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 - Missing body")
        void submitFeedback_emptyBody_returns400() throws Exception {
            mockMvc.perform(patch("/api/v1/advices/feedback")
                            .header("Authorization", userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // Private helper
    // =========================================================================

    private Long startAndFinishSession() throws Exception {
        var request = new TestSessionCreateRequest(
                SessionType.PREMIUM_FULL, "v1", "100", "50-50-0", false);

        String startBody = mockMvc.perform(post("/api/v1/sessions")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andReturn().getResponse().getContentAsString();

        Long sessionId = objectMapper.readTree(startBody).get("lgId").asLong();

        mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/finish")
                .header("Authorization", userToken));

        return sessionId;
    }
}
