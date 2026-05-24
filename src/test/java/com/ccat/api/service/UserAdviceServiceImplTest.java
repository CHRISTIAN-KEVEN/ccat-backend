package com.ccat.api.service;

import com.ccat.api.dto.request.UserAdviceFeedbackRequest;
import com.ccat.api.mapper.UserAdviceMapper;
import com.ccat.api.model.entity.*;
import com.ccat.api.model.enums.*;
import com.ccat.api.repository.*;
import com.ccat.api.service.impl.UserAdviceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserAdviceServiceImpl.
 * Couvre les 11 règles de déclenchement (trigger rules),
 * la génération de conseils et les interactions utilisateur.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAdviceServiceImpl — Tests Unitaires")
class UserAdviceServiceImplTest {

    @Mock UserAdviceRepository  userAdviceRepository;
    @Mock AdviceCardRepository  adviceCardRepository;
    @Mock TestResultRepository  testResultRepository;
    @Mock TestSessionRepository sessionRepository;
    @Mock UserAdviceMapper      userAdviceMapper;

    @InjectMocks
    UserAdviceServiceImpl service;

    private User user;
    private TestResult result;
    private TestSession session;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLgId(1L);
        user.setStrEmail("alice@test.com");

        session = new TestSession();
        session.setLgId(10L);
        session.setUser(user);
        session.setBSubmittedByTimer(false);

        result = new TestResult();
        result.setLgId(100L);
        result.setUser(user);
        result.setSession(session);
        result.setIntTotalScore(30);
        result.setDbAccuracyPercent(72.0);
        result.setDbCompletionPercent(90.0);
        result.setIntSkippedCount(3);
        result.setEmPaceRating(PaceRating.ON_PACE);
        result.setBPassedThreshold(true);
        result.setDbAvgTimeCorrectMs(15_000.0);
        result.setEmWeakestDomain("NUMERICAL");
        result.setStrAdviceTriggerCodes(null);
    }

    // =========================================================================
    // Règles de déclenchement — matchesTrigger
    // =========================================================================

    @Nested
    @DisplayName("matchesTrigger() — Les 11 règles de déclenchement")
    class MatchesTriggerTests {

        @Test
        @DisplayName("ACCURACY_BELOW_50 se déclenche si précision < 50%")
        void trigger_accuracyBelow50_matches() throws Exception {
            result.setDbAccuracyPercent(45.0);
            assertThat(invokeTrigger("ACCURACY_BELOW_50")).isTrue();
        }

        @Test
        @DisplayName("ACCURACY_BELOW_50 ne se déclenche pas si précision ≥ 50%")
        void trigger_accuracyBelow50_doesNotMatch() throws Exception {
            result.setDbAccuracyPercent(55.0);
            assertThat(invokeTrigger("ACCURACY_BELOW_50")).isFalse();
        }

        @Test
        @DisplayName("ACCURACY_BELOW_70 se déclenche pour 50% ≤ précision < 70%")
        void trigger_accuracyBelow70_matches() throws Exception {
            result.setDbAccuracyPercent(65.0);
            assertThat(invokeTrigger("ACCURACY_BELOW_70")).isTrue();
        }

        @Test
        @DisplayName("ACCURACY_BELOW_70 ne se déclenche pas si précision ≥ 70%")
        void trigger_accuracyBelow70_doesNotMatchAbove70() throws Exception {
            result.setDbAccuracyPercent(75.0);
            assertThat(invokeTrigger("ACCURACY_BELOW_70")).isFalse();
        }

        @Test
        @DisplayName("ACCURACY_BELOW_70 ne se déclenche pas si précision < 50% (chevauchement géré)")
        void trigger_accuracyBelow70_doesNotMatchBelow50() throws Exception {
            result.setDbAccuracyPercent(40.0);
            assertThat(invokeTrigger("ACCURACY_BELOW_70")).isFalse();
        }

        @Test
        @DisplayName("PACE_TOO_SLOW se déclenche si rythme == TOO_SLOW")
        void trigger_paceTooSlow_matches() throws Exception {
            result.setEmPaceRating(PaceRating.TOO_SLOW);
            assertThat(invokeTrigger("PACE_TOO_SLOW")).isTrue();
        }

        @Test
        @DisplayName("PACE_VERY_FAST se déclenche si rythme == VERY_FAST")
        void trigger_paceVeryFast_matches() throws Exception {
            result.setEmPaceRating(PaceRating.VERY_FAST);
            assertThat(invokeTrigger("PACE_VERY_FAST")).isTrue();
        }

        @Test
        @DisplayName("SKIP_HEAVY se déclenche si questions sautées > 10")
        void trigger_skipHeavy_matches() throws Exception {
            result.setIntSkippedCount(11);
            assertThat(invokeTrigger("SKIP_HEAVY")).isTrue();
        }

        @Test
        @DisplayName("SKIP_HEAVY ne se déclenche pas si questions sautées ≤ 10")
        void trigger_skipHeavy_doesNotMatch() throws Exception {
            result.setIntSkippedCount(10);
            assertThat(invokeTrigger("SKIP_HEAVY")).isFalse();
        }

        @Test
        @DisplayName("TIME_EXPIRED se déclenche si session soumise par le timer")
        void trigger_timeExpired_matches() throws Exception {
            session.setBSubmittedByTimer(true);
            assertThat(invokeTrigger("TIME_EXPIRED")).isTrue();
        }

        @Test
        @DisplayName("LOW_SCORE se déclenche si score en dessous du seuil")
        void trigger_lowScore_matches() throws Exception {
            result.setBPassedThreshold(false);
            assertThat(invokeTrigger("LOW_SCORE")).isTrue();
        }

        @Test
        @DisplayName("HIGH_ACHIEVER se déclenche si score ≥ 37")
        void trigger_highAchiever_matches() throws Exception {
            result.setIntTotalScore(37);
            assertThat(invokeTrigger("HIGH_ACHIEVER")).isTrue();
        }

        @Test
        @DisplayName("HIGH_ACHIEVER ne se déclenche pas si score < 37")
        void trigger_highAchiever_doesNotMatch() throws Exception {
            result.setIntTotalScore(36);
            assertThat(invokeTrigger("HIGH_ACHIEVER")).isFalse();
        }

        @Test
        @DisplayName("LOW_COMPLETION se déclenche si complétion < 80%")
        void trigger_lowCompletion_matches() throws Exception {
            result.setDbCompletionPercent(75.0);
            assertThat(invokeTrigger("LOW_COMPLETION")).isTrue();
        }

        @Test
        @DisplayName("SLOW_ON_CORRECT se déclenche si temps moyen correct > 20s")
        void trigger_slowOnCorrect_matches() throws Exception {
            result.setDbAvgTimeCorrectMs(21_000.0);
            assertThat(invokeTrigger("SLOW_ON_CORRECT")).isTrue();
        }

        @Test
        @DisplayName("WEAK_DOMAIN:NUMERICAL se déclenche si NUMERICAL est le domaine le plus faible")
        void trigger_weakDomainNumerical_matches() throws Exception {
            result.setEmWeakestDomain("NUMERICAL");
            assertThat(invokeTrigger("WEAK_DOMAIN:NUMERICAL")).isTrue();
        }

        @Test
        @DisplayName("WEAK_DOMAIN:VERBAL ne se déclenche pas si NUMERICAL est le plus faible")
        void trigger_weakDomainVerbal_doesNotMatch() throws Exception {
            result.setEmWeakestDomain("NUMERICAL");
            assertThat(invokeTrigger("WEAK_DOMAIN:VERBAL")).isFalse();
        }

        @Test
        @DisplayName("Règle inconnue retourne false sans exception")
        void trigger_unknownRule_returnsFalse() throws Exception {
            assertThat(invokeTrigger("UNKNOWN_RULE_XYZ")).isFalse();
        }

        // ── Helper reflection ─────────────────────────────────────────────
        private boolean invokeTrigger(String rule) throws Exception {
            var method = UserAdviceServiceImpl.class.getDeclaredMethod(
                    "matchesTrigger", String.class, TestResult.class, List.class);
            method.setAccessible(true);
            return (boolean) method.invoke(service, rule, result, List.of());
        }
    }

    // =========================================================================
    // Génération de conseils (generateForResult)
    // =========================================================================

    @Nested
    @DisplayName("generateForResult() — Génération des conseils après un test")
    class GenerateForResultTests {

        @Test
        @DisplayName("Ne génère rien si aucune carte de conseil ne correspond")
        void generateForResult_noMatchingCards_savesNothing() {
            when(adviceCardRepository.findAllActive()).thenReturn(List.of());

            service.generateForResult(result, List.of());

            verify(userAdviceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Génère un conseil pour chaque carte correspondante")
        void generateForResult_matchingCards_savesAdvice() {
            result.setDbAccuracyPercent(45.0); // déclenche ACCURACY_BELOW_50
            result.setBPassedThreshold(false); // déclenche LOW_SCORE

            AdviceCard card1 = buildAdviceCard(1L, "ACCURACY_BELOW_50", AdvicePriority.HIGH);
            AdviceCard card2 = buildAdviceCard(2L, "LOW_SCORE", AdvicePriority.CRITICAL);

            when(adviceCardRepository.findAllActive()).thenReturn(List.of(card1, card2));
            when(testResultRepository.save(any())).thenReturn(result);
            when(userAdviceRepository.existsByUserLgIdAndAdviceCardLgIdAndResultLgId(
                    anyLong(), anyLong(), anyLong())).thenReturn(false);
            when(userAdviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(adviceCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.generateForResult(result, List.of());

            // 2 cartes correspondantes → 2 sauvegardes
            verify(userAdviceRepository, times(2)).save(any(UserAdvice.class));
        }

        @Test
        @DisplayName("Ne duplique pas un conseil déjà existant pour ce résultat")
        void generateForResult_alreadyExists_skipsAdvice() {
            result.setDbAccuracyPercent(45.0);

            AdviceCard card = buildAdviceCard(1L, "ACCURACY_BELOW_50", AdvicePriority.HIGH);
            when(adviceCardRepository.findAllActive()).thenReturn(List.of(card));
            when(testResultRepository.save(any())).thenReturn(result);
            when(userAdviceRepository.existsByUserLgIdAndAdviceCardLgIdAndResultLgId(
                    anyLong(), anyLong(), anyLong())).thenReturn(true); // déjà existant

            service.generateForResult(result, List.of());

            verify(userAdviceRepository, never()).save(any(UserAdvice.class));
        }

        @Test
        @DisplayName("Trie les conseils par priorité : CRITICAL avant HIGH avant MEDIUM avant LOW")
        void generateForResult_sortsByPriority() {
            result.setDbAccuracyPercent(45.0); // ACCURACY_BELOW_50
            result.setBPassedThreshold(false); // LOW_SCORE
            result.setEmPaceRating(PaceRating.TOO_SLOW); // PACE_TOO_SLOW

            AdviceCard lowCard    = buildAdviceCard(1L, "ACCURACY_BELOW_70", AdvicePriority.LOW);
            AdviceCard critCard   = buildAdviceCard(2L, "LOW_SCORE",          AdvicePriority.CRITICAL);
            AdviceCard highCard   = buildAdviceCard(3L, "ACCURACY_BELOW_50",  AdvicePriority.HIGH);
            AdviceCard medCard    = buildAdviceCard(4L, "PACE_TOO_SLOW",      AdvicePriority.MEDIUM);

            when(adviceCardRepository.findAllActive())
                    .thenReturn(List.of(lowCard, critCard, highCard, medCard));
            when(testResultRepository.save(any())).thenReturn(result);
            when(userAdviceRepository.existsByUserLgIdAndAdviceCardLgIdAndResultLgId(
                    anyLong(), anyLong(), anyLong())).thenReturn(false);

            ArgumentCaptor<UserAdvice> captor = ArgumentCaptor.forClass(UserAdvice.class);
            when(userAdviceRepository.save(captor.capture()))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(adviceCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.generateForResult(result, List.of());

            List<UserAdvice> saved = captor.getAllValues();
            // ACCURACY_BELOW_70 ne correspond pas (précision < 50 → ne correspond pas à ACCURACY_BELOW_70)
            // Les rangs doivent refléter la priorité : CRITICAL=1, HIGH=2, MEDIUM=3
            assertThat(saved.get(0).getAdviceCard().getEmPriority()).isEqualTo(AdvicePriority.CRITICAL);
            assertThat(saved.get(1).getAdviceCard().getEmPriority()).isEqualTo(AdvicePriority.HIGH);
        }
    }

    // =========================================================================
    // Marquer comme lu (markRead)
    // =========================================================================

    @Nested
    @DisplayName("markRead() — Marquage d'un conseil comme lu")
    class MarkReadTests {

        @Test
        @DisplayName("Marque le conseil comme lu si ce n'est pas déjà fait")
        void markRead_notYetRead_setsReadFlag() {
            UserAdvice advice = buildUserAdvice(1L, false);
            when(userAdviceRepository.findById(1L)).thenReturn(Optional.of(advice));
            when(userAdviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userAdviceMapper.toResponse(any())).thenReturn(null);

            service.markRead(1L, "alice@test.com");

            assertThat(advice.getBWasRead()).isTrue();
            assertThat(advice.getDtRead()).isNotNull();
        }

        @Test
        @DisplayName("Ne modifie pas dtRead si le conseil est déjà lu")
        void markRead_alreadyRead_doesNotUpdateReadDate() {
            UserAdvice advice = buildUserAdvice(1L, true);
            var originalDate = java.time.LocalDateTime.now().minusDays(1);
            advice.setDtRead(originalDate);

            when(userAdviceRepository.findById(1L)).thenReturn(Optional.of(advice));
            when(userAdviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userAdviceMapper.toResponse(any())).thenReturn(null);

            service.markRead(1L, "alice@test.com");

            assertThat(advice.getDtRead()).isEqualTo(originalDate);
        }

        @Test
        @DisplayName("Lève une exception si le conseil n'appartient pas à l'utilisateur")
        void markRead_wrongUser_throwsException() {
            UserAdvice advice = buildUserAdvice(1L, false);
            when(userAdviceRepository.findById(1L)).thenReturn(Optional.of(advice));

            assertThatThrownBy(() -> service.markRead(1L, "autre@test.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // =========================================================================
    // Feedback utilisateur (submitFeedback)
    // =========================================================================

    @Nested
    @DisplayName("submitFeedback() — Soumission du feedback")
    class SubmitFeedbackTests {

        @Test
        @DisplayName("Enregistre le feedback complet correctement")
        void submitFeedback_withAllFields_savesAll() {
            UserAdvice advice = buildUserAdvice(1L, false);
            when(userAdviceRepository.findById(1L)).thenReturn(Optional.of(advice));
            when(userAdviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userAdviceMapper.toResponse(any())).thenReturn(null);

            var request = new UserAdviceFeedbackRequest(1L, true, "Très utile !", 3000);
            service.submitFeedback(request, "alice@test.com");

            assertThat(advice.getBWasHelpful()).isTrue();
            assertThat(advice.getStrUserNote()).isEqualTo("Très utile !");
            assertThat(advice.getIntTimeSpentMs()).isEqualTo(3000);
            assertThat(advice.getBWasRead()).isTrue();
        }

        @Test
        @DisplayName("Les champs null dans le feedback ne remplacent pas les valeurs existantes")
        void submitFeedback_nullFields_keepsExistingValues() {
            UserAdvice advice = buildUserAdvice(1L, true);
            advice.setBWasHelpful(true);
            advice.setStrUserNote("Note existante");

            when(userAdviceRepository.findById(1L)).thenReturn(Optional.of(advice));
            when(userAdviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userAdviceMapper.toResponse(any())).thenReturn(null);

            var request = new UserAdviceFeedbackRequest(1L, null, null, null);
            service.submitFeedback(request, "alice@test.com");

            assertThat(advice.getBWasHelpful()).isTrue();
            assertThat(advice.getStrUserNote()).isEqualTo("Note existante");
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private AdviceCard buildAdviceCard(Long id, String rule, AdvicePriority priority) {
        AdviceCard card = new AdviceCard();
        card.setLgId(id);
        card.setStrCode("CARD_" + id);
        card.setEmTriggerRule(rule);
        card.setEmPriority(priority);
        card.setEmCategory(com.ccat.api.model.enums.AdviceCategory.GENERAL);
        card.setStrTitle("Conseil " + id);
        card.setStrContent("Contenu du conseil " + id);
        card.setBActive(true);
        card.setIntDisplayCount(0);
        return card;
    }

    private UserAdvice buildUserAdvice(Long id, boolean wasRead) {
        UserAdvice advice = new UserAdvice();
        advice.setLgId(id);
        advice.setUser(user);
        advice.setResult(result);
        advice.setBWasRead(wasRead);
        advice.setIntDisplayRank(1);
        AdviceCard card = buildAdviceCard(id, "ACCURACY_BELOW_50", AdvicePriority.HIGH);
        advice.setAdviceCard(card);
        return advice;
    }
}
