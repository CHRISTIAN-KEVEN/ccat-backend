package com.ccat.api.service;

import com.ccat.api.exception.FreeTestAlreadyUsedException;
import com.ccat.api.exception.SessionAlreadySubmittedException;
import com.ccat.api.exception.SessionExpiredException;
import com.ccat.api.mapper.*;
import com.ccat.api.model.entity.*;
import com.ccat.api.model.enums.*;
import com.ccat.api.repository.*;
import com.ccat.api.service.impl.TestSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour TestSessionServiceImpl.
 * Teste la logique de calcul des scores, la distribution des questions
 * et la gestion du cycle de vie des sessions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TestSessionServiceImpl — Tests Unitaires")
class TestSessionServiceImplTest {

    // ── Mocks ────────────────────────────────────────────────────────────────

    @Mock TestSessionRepository    sessionRepository;
    @Mock TestResultRepository     resultRepository;
    @Mock ResponseRepository       responseRepository;
    @Mock QuestionRepository       questionRepository;
    @Mock AnswerRepository         answerRepository;
    @Mock DomainRepository         domainRepository;
    @Mock DomainPerformanceRepository domainPerfRepository;
    @Mock UserRepository           userRepository;
    @Mock TestSessionMapper        sessionMapper;
    @Mock TestResultMapper         resultMapper;
    @Mock ResponseMapper           responseMapper;
    @Mock DomainPerformanceMapper  domainPerfMapper;
    @Mock QuestionMapper           questionMapper;
    @Mock AnswerMapper             answerMapper;
    @Mock UserAdviceService        userAdviceService;

    @InjectMocks
    TestSessionServiceImpl service;

    // ── Fixtures ─────────────────────────────────────────────────────────────

    private User user;
    private TestSession session;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLgId(1L);
        user.setStrEmail("alice@test.com");

        session = new TestSession();
        session.setLgId(10L);
        session.setStrUuid("uuid-test-123");
        session.setUser(user);
        session.setEmStatus(SessionStatus.ACTIVE);
        session.setIntQuestionCount(50);
        session.setIntDurationSeconds(900);
        session.setBAllowBacktrack(false);
        session.setBSubmittedByTimer(false);
        session.setDtStarted(LocalDateTime.now().minusMinutes(5));
        session.setDtExpires(LocalDateTime.now().plusMinutes(10));
        session.setStrQuestionOrder("[1,2,3]");
        session.setEmDomainRatio("40-40-20");
        session.setEmDifficultyMix("30-50-20");
    }

    // =========================================================================
    // Démarrage d'une session (start)
    // =========================================================================

    @Nested
    @DisplayName("start() — Démarrage d'une session")
    class StartTests {

        @Test
        @DisplayName("Lève FreeTestAlreadyUsedException si le test gratuit est déjà utilisé")
        void start_freeDiagnostic_alreadyUsed_throwsException() {
            // Arrange
            var request = new com.ccat.api.dto.request.TestSessionCreateRequest(
                    SessionType.FREE_DIAGNOSTIC, "v1", null, null, null);
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));
            when(sessionRepository.countFreeTestsByUser(1L)).thenReturn(1L);

            // Act + Assert
            assertThatThrownBy(() -> service.start(request, "alice@test.com"))
                    .isInstanceOf(FreeTestAlreadyUsedException.class);
        }

        @Test
        @DisplayName("Crée la session avec les valeurs par défaut si aucun ratio fourni")
        void start_noRatioProvided_usesDefaults() {
            // Arrange
            var request = new com.ccat.api.dto.request.TestSessionCreateRequest(
                    SessionType.PREMIUM_FULL, "v1", null, null, null);
            when(userRepository.findByStrEmail("alice@test.com")).thenReturn(Optional.of(user));

            Domain d1 = buildDomain("VERBAL");
            Domain d2 = buildDomain("NUMERICAL");
            when(domainRepository.findByEmStatusOrderByIntSortOrderAsc(DomainStatus.ACTIVE))
                    .thenReturn(List.of(d1, d2));
            when(questionRepository.findRandomByDomainCode(anyString(), anyInt()))
                    .thenReturn(List.of());
            when(questionRepository.findRandom(anyInt())).thenReturn(List.of());
            when(sessionMapper.toEntity(any(), any())).thenReturn(session);
            when(sessionRepository.save(any())).thenReturn(session);
            when(sessionMapper.toResponse(any())).thenReturn(
                    new com.ccat.api.dto.response.TestSessionResponse(
                            10L, "uuid-test-123", 1L, SessionType.PREMIUM_FULL,
                            SessionStatus.ACTIVE, false, false, 50, 900,
                            "40-40-20", "30-50-20", 0, 0,
                            null, null, null, null));

            // Act
            var response = service.start(request, "alice@test.com");

            // Assert
            assertThat(response).isNotNull();
            verify(sessionRepository).save(any(TestSession.class));
        }
    }

    // =========================================================================
    // Soumission d'une réponse (submitResponse)
    // =========================================================================

    @Nested
    @DisplayName("submitResponse() — Soumission des réponses")
    class SubmitResponseTests {

        @Test
        @DisplayName("Lève SessionAlreadySubmittedException si la session est déjà soumise")
        void submitResponse_sessionAlreadySubmitted_throwsException() {
            // Arrange
            session.setEmStatus(SessionStatus.SUBMITTED);
            when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

            var request = new com.ccat.api.dto.request.ResponseSubmitRequest(
                    10L, 1L, 1L, 5000, 1, "[]", false, 0);

            // Act + Assert
            assertThatThrownBy(() -> service.submitResponse(10L, request, "alice@test.com"))
                    .isInstanceOf(SessionAlreadySubmittedException.class);
        }

        @Test
        @DisplayName("Lève SessionExpiredException si la session est expirée")
        void submitResponse_sessionExpired_throwsException() {
            // Arrange
            session.setDtExpires(LocalDateTime.now().minusMinutes(1)); // expirée
            when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
            when(resultRepository.findBySessionLgId(10L)).thenReturn(Optional.empty());

            // Pour finalizeSession lors de l'expiration
            when(responseRepository.findBySessionLgId(10L)).thenReturn(List.of());
            when(resultRepository.save(any())).thenReturn(new TestResult());
            when(domainPerfRepository.saveAll(any())).thenReturn(List.of());
            doNothing().when(userAdviceService).generateForResult(any(), any());
            when(resultMapper.toResponse(any(), any())).thenReturn(null);

            var request = new com.ccat.api.dto.request.ResponseSubmitRequest(
                    10L, 1L, 1L, 5000, 1, "[]", false, 0);

            // Act + Assert
            assertThatThrownBy(() -> service.submitResponse(10L, request, "alice@test.com"))
                    .isInstanceOf(SessionExpiredException.class);
        }

        @Test
        @DisplayName("Refuse le renvoi d'une réponse si le backtrack est désactivé")
        void submitResponse_backtrackDisabled_existingResponse_throwsException() {
            // Arrange
            session.setBAllowBacktrack(false);
            when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

            Question q = buildQuestion(1L, "VERBAL");
            when(questionRepository.findById(1L)).thenReturn(Optional.of(q));

            Answer a = buildAnswer(1L, true);
            when(answerRepository.findById(1L)).thenReturn(Optional.of(a));

            Response existingResponse = new Response();
            existingResponse.setBWasSkipped(false);
            when(responseRepository.findBySessionLgIdAndQuestionLgId(10L, 1L))
                    .thenReturn(Optional.of(existingResponse));

            var request = new com.ccat.api.dto.request.ResponseSubmitRequest(
                    10L, 1L, 1L, 5000, 1, "[]", false, 0);

            // Act + Assert
            assertThatThrownBy(() -> service.submitResponse(10L, request, "alice@test.com"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Backtracking is not allowed");
        }
    }

    // =========================================================================
    // Calcul du percentile (estimatePercentile)
    // =========================================================================

    @Nested
    @DisplayName("estimatePercentile() — Estimation du rang")
    class PercentileTests {

        /**
         * On teste via finish() qui appelle buildResult() → estimatePercentile().
         * On vérifie indirectement en inspectant le TestResult sauvegardé.
         */
        @ParameterizedTest(name = "score={0} → percentile≥{1}")
        @CsvSource({
            "45, 95",
            "42, 95",
            "37, 85",
            "32, 75",
            "28, 65",
            "24, 50",
            "18, 35",
            "12, 20",
            "5,  10"
        })
        @DisplayName("Retourne le bon percentile selon le score")
        void estimatePercentile_variousScores(int score, int expectedPercentile) {
            // On accède à la méthode privée via reflection pour tester directement
            try {
                var method = TestSessionServiceImpl.class
                        .getDeclaredMethod("estimatePercentile", int.class);
                method.setAccessible(true);
                int result = (int) method.invoke(service, score);
                assertThat(result).isEqualTo(expectedPercentile);
            } catch (Exception e) {
                fail("Impossible d'accéder à estimatePercentile: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // Distribution des questions (distributeQuestions)
    // =========================================================================

    @Nested
    @DisplayName("distributeQuestions() — Distribution par ratio")
    class DistributeQuestionsTests {

        @Test
        @DisplayName("40-40-20 sur 50 questions → [20, 20, 10]")
        void distributeQuestions_40_40_20_returns_20_20_10() throws Exception {
            var method = TestSessionServiceImpl.class
                    .getDeclaredMethod("distributeQuestions", int.class, int[].class);
            method.setAccessible(true);

            int[] counts = (int[]) method.invoke(service, 50, new int[]{40, 40, 20});

            assertThat(counts).hasSize(3);
            int total = counts[0] + counts[1] + counts[2];
            assertThat(total).isEqualTo(50);
            assertThat(counts[0]).isEqualTo(20);
            assertThat(counts[1]).isEqualTo(20);
            assertThat(counts[2]).isEqualTo(10);
        }

        @Test
        @DisplayName("Ratios à zéro → fallback équitable")
        void distributeQuestions_zeroSum_fallsBackToEvenSplit() throws Exception {
            var method = TestSessionServiceImpl.class
                    .getDeclaredMethod("distributeQuestions", int.class, int[].class);
            method.setAccessible(true);

            int[] counts = (int[]) method.invoke(service, 50, new int[]{0, 0, 0});

            assertThat(counts).hasSize(3);
            // chaque domaine reçoit 50/3 ≈ 16 questions
            for (int c : counts) {
                assertThat(c).isGreaterThanOrEqualTo(16).isLessThanOrEqualTo(18);
            }
        }

        @Test
        @DisplayName("Le total des questions distribuées doit toujours être égal à 50")
        void distributeQuestions_totalAlways50() throws Exception {
            var method = TestSessionServiceImpl.class
                    .getDeclaredMethod("distributeQuestions", int.class, int[].class);
            method.setAccessible(true);

            int[] counts = (int[]) method.invoke(service, 50, new int[]{33, 33, 34});
            int sum = 0;
            for (int c : counts) sum += c;
            assertThat(sum).isEqualTo(50);
        }
    }

    // =========================================================================
    // Parsing du ratio (parseRatios)
    // =========================================================================

    @Nested
    @DisplayName("parseRatios() — Parsing de la chaîne de ratio")
    class ParseRatiosTests {

        @Test
        @DisplayName("Ratio valide '40-40-20' retourne [40, 40, 20]")
        void parseRatios_validString_returnsArray() throws Exception {
            var method = TestSessionServiceImpl.class
                    .getDeclaredMethod("parseRatios", String.class, int.class);
            method.setAccessible(true);

            int[] ratios = (int[]) method.invoke(service, "40-40-20", 3);

            assertThat(ratios).containsExactly(40, 40, 20);
        }

        @Test
        @DisplayName("Ratio invalide → fallback avec split équitable")
        void parseRatios_invalidString_fallsBack() throws Exception {
            var method = TestSessionServiceImpl.class
                    .getDeclaredMethod("parseRatios", String.class, int.class);
            method.setAccessible(true);

            int[] ratios = (int[]) method.invoke(service, "invalid-ratio!!", 3);

            assertThat(ratios).hasSize(3);
            // fallback: 100/3 = 33
            for (int r : ratios) {
                assertThat(r).isEqualTo(33);
            }
        }
    }

    // =========================================================================
    // Parsing de l'ordre des questions (parseQuestionOrder)
    // =========================================================================

    @Nested
    @DisplayName("parseQuestionOrder() — Parsing du JSON des IDs")
    class ParseQuestionOrderTests {

        @Test
        @DisplayName("JSON valide '[1,2,3]' retourne la liste [1, 2, 3]")
        void parseQuestionOrder_validJson_returnsList() throws Exception {
            var method = TestSessionServiceImpl.class
                    .getDeclaredMethod("parseQuestionOrder", String.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            var result = (java.util.List<Long>) method.invoke(service, "[1,2,3]");

            assertThat(result).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("JSON null retourne une liste vide")
        void parseQuestionOrder_null_returnsEmptyList() throws Exception {
            var method = TestSessionServiceImpl.class
                    .getDeclaredMethod("parseQuestionOrder", String.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            var result = (java.util.List<Long>) method.invoke(service, (Object) null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("JSON vide '[]' retourne une liste vide")
        void parseQuestionOrder_emptyArray_returnsEmptyList() throws Exception {
            var method = TestSessionServiceImpl.class
                    .getDeclaredMethod("parseQuestionOrder", String.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            var result = (java.util.List<Long>) method.invoke(service, "[]");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("JSON malformé retourne une liste vide sans exception")
        void parseQuestionOrder_malformed_returnsEmptyList() throws Exception {
            var method = TestSessionServiceImpl.class
                    .getDeclaredMethod("parseQuestionOrder", String.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            var result = (java.util.List<Long>) method.invoke(service, "[abc, def]");

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // Détection d'expiration (isExpired)
    // =========================================================================

    @Nested
    @DisplayName("isExpired() — Détection de session expirée")
    class IsExpiredTests {

        @Test
        @DisplayName("Session non expirée → false")
        void isExpired_futureExpiry_returnsFalse() throws Exception {
            var method = TestSessionServiceImpl.class
                    .getDeclaredMethod("isExpired", TestSession.class, LocalDateTime.class);
            method.setAccessible(true);

            session.setDtExpires(LocalDateTime.now().plusMinutes(5));
            boolean result = (boolean) method.invoke(service, session, LocalDateTime.now());

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Session expirée → true")
        void isExpired_pastExpiry_returnsTrue() throws Exception {
            var method = TestSessionServiceImpl.class
                    .getDeclaredMethod("isExpired", TestSession.class, LocalDateTime.class);
            method.setAccessible(true);

            session.setDtExpires(LocalDateTime.now().minusMinutes(1));
            boolean result = (boolean) method.invoke(service, session, LocalDateTime.now());

            assertThat(result).isTrue();
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Domain buildDomain(String code) {
        Domain d = new Domain();
        d.setStrDomainCode(code);
        d.setStrLabel(code);
        d.setEmStatus(DomainStatus.ACTIVE);
        return d;
    }

    private Question buildQuestion(Long id, String domainCode) {
        Question q = new Question();
        q.setLgId(id);
        q.setStrUuid("uuid-" + id);
        q.setEmDifficulty(QuestionDifficulty.MEDIUM);
        q.setEmQuestionType(QuestionType.MULTIPLE_CHOICE);
        q.setEmContentType(ContentType.TEXT);
        q.setBActive(true);
        Domain d = buildDomain(domainCode);
        q.setDomain(d);
        return q;
    }

    private Answer buildAnswer(Long id, boolean isCorrect) {
        Answer a = new Answer();
        a.setLgId(id);
        a.setBIsCorrect(isCorrect);
        a.setStrAnswerLabel("Answer " + id);
        return a;
    }
}
