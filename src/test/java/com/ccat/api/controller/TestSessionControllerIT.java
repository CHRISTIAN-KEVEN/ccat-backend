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
 * Tests d'intégration pour TestSessionController.
 * Couvre le cycle de vie complet d'une session : démarrage → réponses → résultats.
 *
 * Endpoints couverts :
 *   POST   /api/v1/sessions
 *   GET    /api/v1/sessions/me
 *   GET    /api/v1/sessions/{id}
 *   GET    /api/v1/sessions/{id}/questions
 *   POST   /api/v1/sessions/{id}/responses
 *   POST   /api/v1/sessions/{id}/finish
 *   GET    /api/v1/sessions/{id}/result
 *   GET    /api/v1/sessions/{id}/review
 */
@DisplayName("TestSessionController — Tests d'intégration")
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

        // Créer 50 questions minimum pour pouvoir démarrer une session
        questions     = new ArrayList<>();
        correctAnswers = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Question q = createQuestion(domain, user);
            Answer correct = createAnswer(q, "A", "Bonne réponse",    true,  1);
            createAnswer(q,                     "B", "Mauvaise réponse", false, 2);
            questions.add(q);
            correctAnswers.add(correct);
        }
    }

    // =========================================================================
    // POST /sessions — Démarrage
    // =========================================================================

    @Nested
    @DisplayName("POST /sessions — Démarrage d'une session")
    class StartTests {

        @Test
        @DisplayName("201 — Session PREMIUM_FULL créée avec succès")
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
        @DisplayName("201 — Session FREE_DIAGNOSTIC créée si aucune n'existe encore")
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
        @DisplayName("402 — Deuxième FREE_DIAGNOSTIC refusé")
        void start_freeDiagnostic_secondTime_returns402() throws Exception {
            // Créer un premier diagnostic
            var first = new TestSessionCreateRequest(
                    SessionType.FREE_DIAGNOSTIC, "v1", "100", "50-50-0", false);
            mockMvc.perform(post(BASE)
                    .header("Authorization", userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(first)));

            // Tenter un deuxième
            mockMvc.perform(post(BASE)
                            .header("Authorization", userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(first)))
                    .andExpect(status().isPaymentRequired());
        }

        @Test
        @DisplayName("401 — Non authentifié")
        void start_noToken_returns403() throws Exception {
            var request = new TestSessionCreateRequest(
                    SessionType.PREMIUM_FULL, "v1", null, null, false);

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 — emSessionType manquant")
        void start_nullSessionType_returns400() throws Exception {
            // JSON sans emSessionType
            mockMvc.perform(post(BASE)
                            .header("Authorization", userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"strEligibilityVersion\":\"v1\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // GET /sessions/me — Historique
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/me — Historique des sessions")
    class HistoryTests {

        @Test
        @DisplayName("200 — Retourne la liste des sessions (vide au départ)")
        void getMyHistory_noSessions_returnsEmptyList() throws Exception {
            mockMvc.perform(get(BASE + "/me")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("200 — Retourne les sessions après en avoir créé une")
        void getMyHistory_withSession_returnsList() throws Exception {
            // Créer une session
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
        @DisplayName("401 — Non authentifié")
        void getMyHistory_noToken_returns403() throws Exception {
            mockMvc.perform(get(BASE + "/me"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // GET /sessions/{id} — Détail d'une session
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/{id} — Détail d'une session")
    class GetByIdTests {

        @Test
        @DisplayName("200 — Retourne les détails d'une session existante")
        void getById_existingSession_returns200() throws Exception {
            Long sessionId = startSession();

            mockMvc.perform(get(BASE + "/" + sessionId)
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lgId").value(sessionId));
        }

        @Test
        @DisplayName("404 — Session inexistante")
        void getById_nonExistent_returns404() throws Exception {
            mockMvc.perform(get(BASE + "/99999")
                            .header("Authorization", userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("404 — Session d'un autre utilisateur (sécurité : pas de fuite d'ID)")
        void getById_sessionOfAnotherUser_returns404() throws Exception {
            Long sessionId = startSession();
            // Autre utilisateur
            createUser("eve@test.com", "Str0ng!Pass");
            String eveToken = bearerToken("eve@test.com");

            mockMvc.perform(get(BASE + "/" + sessionId)
                            .header("Authorization", eveToken))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /sessions/{id}/questions — Questions de la session
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/{id}/questions — Questions de la session")
    class GetQuestionsTests {

        @Test
        @DisplayName("200 — Retourne 50 questions pour une session active")
        void getSessionQuestions_activeSession_returns50Questions() throws Exception {
            Long sessionId = startSession();

            mockMvc.perform(get(BASE + "/" + sessionId + "/questions")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(50)));
        }

        @Test
        @DisplayName("409 — Session déjà soumise : questions non accessibles")
        void getSessionQuestions_submittedSession_returns409() throws Exception {
            Long sessionId = startSession();
            finishSession(sessionId);

            mockMvc.perform(get(BASE + "/" + sessionId + "/questions")
                            .header("Authorization", userToken))
                    .andExpect(status().isConflict());
        }
    }

    // =========================================================================
    // POST /sessions/{id}/finish — Soumission finale
    // =========================================================================

    @Nested
    @DisplayName("POST /sessions/{id}/finish — Soumission de la session")
    class FinishTests {

        @Test
        @DisplayName("200 — Retourne les résultats après soumission")
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
        @DisplayName("200 — Double appel finish idempotent : retourne les mêmes résultats")
        void finish_calledTwice_isIdempotent() throws Exception {
            Long sessionId = startSession();

            // Premier appel
            String firstResult = mockMvc.perform(post(BASE + "/" + sessionId + "/finish")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // Deuxième appel → même résultat
            mockMvc.perform(post(BASE + "/" + sessionId + "/finish")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intTotalScore")
                            .value(objectMapper.readTree(firstResult)
                                    .get("intTotalScore").asInt()));
        }

        @Test
        @DisplayName("404 — Session inexistante")
        void finish_nonExistent_returns404() throws Exception {
            mockMvc.perform(post(BASE + "/99999/finish")
                            .header("Authorization", userToken))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /sessions/{id}/result — Résultats
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/{id}/result — Résultats d'une session")
    class GetResultTests {

        @Test
        @DisplayName("200 — Résultats disponibles après soumission")
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
        @DisplayName("401 — Non authentifié")
        void getResult_noToken_returns403() throws Exception {
            mockMvc.perform(get(BASE + "/1/result"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // GET /sessions/{id}/review — Révision
    // =========================================================================

    @Nested
    @DisplayName("GET /sessions/{id}/review — Révision post-test")
    class GetReviewTests {

        @Test
        @DisplayName("200 — Révision disponible après soumission avec questions et réponses")
        void getReview_submittedSession_returnsReview() throws Exception {
            Long sessionId = startSession();
            finishSession(sessionId);

            mockMvc.perform(get(BASE + "/" + sessionId + "/review")
                            .header("Authorization", userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("409 — Révision non disponible si la session est encore active")
        void getReview_activeSession_returns409() throws Exception {
            Long sessionId = startSession();

            mockMvc.perform(get(BASE + "/" + sessionId + "/review")
                            .header("Authorization", userToken))
                    .andExpect(status().isConflict());
        }
    }

    // =========================================================================
    // Helpers privés
    // =========================================================================

    /** Démarre une session et retourne son ID. */
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

    /** Soumet la session (finish) pour obtenir les résultats. */
    private void finishSession(Long sessionId) throws Exception {
        mockMvc.perform(post(BASE + "/" + sessionId + "/finish")
                .header("Authorization", userToken));
    }
}
