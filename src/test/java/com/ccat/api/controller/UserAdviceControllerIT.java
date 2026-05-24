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
 * Tests d'intégration pour UserAdviceController.
 * Vérifie la récupération, la lecture et le feedback des conseils post-test.
 *
 * Endpoints couverts :
 *   GET   /api/v1/sessions/{sessionId}/advices
 *   PATCH /api/v1/advices/{id}/read
 *   PATCH /api/v1/advices/feedback
 */
@DisplayName("UserAdviceController — Tests d'intégration")
class UserAdviceControllerIT extends IntegrationTestBase {

    private User   user;
    private String userToken;
    private Long   submittedSessionId;

    @BeforeEach
    void setUpData() throws Exception {
        user      = createUser("alice@test.com", "Str0ng!Pass");
        userToken = bearerToken("alice@test.com");

        Domain domain = createDomain("VERBAL", "Verbal Reasoning");

        // 50 questions nécessaires pour démarrer une session
        var questions = new ArrayList<Question>();
        for (int i = 0; i < 50; i++) {
            Question q = createQuestion(domain, user);
            createAnswer(q, "A", "Bonne réponse",    true,  1);
            createAnswer(q, "B", "Mauvaise réponse", false, 2);
            questions.add(q);
        }

        // Démarrer puis soumettre une session pour générer des conseils
        submittedSessionId = startAndFinishSession();
    }

    // =========================================================================
    // GET /sessions/{sessionId}/advices
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/{id}/advices — Récupération des conseils")
    class GetAdvicesTests {

        @Test
        @DisplayName("200 — Retourne la liste (potentiellement vide) des conseils")
        void getBySession_submittedSession_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/sessions/" + submittedSessionId + "/advices")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("401 — Non authentifié")
        void getBySession_noToken_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/sessions/" + submittedSessionId + "/advices"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("404 — Session inexistante")
        void getBySession_nonExistentSession_returns404() throws Exception {
            mockMvc.perform(get("/api/v1/sessions/99999/advices")
                            .header("Authorization", userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("404 — Session d'un autre utilisateur inaccessible")
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
    @DisplayName("PATCH /advices/{id}/read — Marquage comme lu")
    class MarkReadTests {

        @Test
        @DisplayName("404 — Conseil inexistant")
        void markRead_nonExistentAdvice_returns404() throws Exception {
            mockMvc.perform(patch("/api/v1/advices/99999/read")
                            .header("Authorization", userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("401 — Non authentifié")
        void markRead_noToken_returns403() throws Exception {
            mockMvc.perform(patch("/api/v1/advices/1/read"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("404 — Conseil d'un autre utilisateur non accessible")
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
    @DisplayName("PATCH /advices/feedback — Soumission du feedback")
    class FeedbackTests {

        @Test
        @DisplayName("404 — Conseil inexistant")
        void submitFeedback_nonExistentAdvice_returns404() throws Exception {
            var request = new UserAdviceFeedbackRequest(99999L, true, "Utile !", 2000);

            mockMvc.perform(patch("/api/v1/advices/feedback")
                            .header("Authorization", userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("401 — Non authentifié")
        void submitFeedback_noToken_returns403() throws Exception {
            var request = new UserAdviceFeedbackRequest(1L, true, "Note", 1000);

            mockMvc.perform(patch("/api/v1/advices/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 — Body manquant")
        void submitFeedback_emptyBody_returns400() throws Exception {
            mockMvc.perform(patch("/api/v1/advices/feedback")
                            .header("Authorization", userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // Helper privé
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
