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
 * Unit tests for UserAdviceServiceImpl.
 * Covers the 11 trigger rules,
 * advice generation, and user interactions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAdviceServiceImpl - Unit Tests")
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
    // Trigger rules - matchesTrigger
    // =========================================================================

    @Nested
    @DisplayName("matchesTrigger() - The 11 trigger rules")
    class MatchesTriggerTests {

        @Test
        @DisplayName("ACCURACY_BELOW_50 triggers when accuracy < 50%")
        void trigger_accuracyBelow50_matches() throws Exception {
            result.setDbAccuracyPercent(45.0);
            assertThat(invokeTrigger("ACCURACY_BELOW_50")).isTrue();
        }

        @Test
        @DisplayName("ACCURACY_BELOW_50 does not trigger when accuracy >= 50%")
        void trigger_accuracyBelow50_doesNotMatch() throws Exception {
            result.setDbAccuracyPercent(55.0);
            assertThat(invokeTrigger("ACCURACY_BELOW_50")).isFalse();
        }

        @Test
        @DisplayName("ACCURACY_BELOW_70 triggers for 50% <= accuracy < 70%")
        void trigger_accuracyBelow70_matches() throws Exception {
            result.setDbAccuracyPercent(65.0);
            assertThat(invokeTrigger("ACCURACY_BELOW_70")).isTrue();
        }

        @Test
        @DisplayName("ACCURACY_BELOW_70 does not trigger when accuracy >= 70%")
        void trigger_accuracyBelow70_doesNotMatchAbove70() throws Exception {
            result.setDbAccuracyPercent(75.0);
            assertThat(invokeTrigger("ACCURACY_BELOW_70")).isFalse();
        }

        @Test
        @DisplayName("ACCURACY_BELOW_70 does not trigger when accuracy < 50% (overlap handled)")
        void trigger_accuracyBelow70_doesNotMatchBelow50() throws Exception {
            result.setDbAccuracyPercent(40.0);
            assertThat(invokeTrigger("ACCURACY_BELOW_70")).isFalse();
        }

        @Test
        @DisplayName("PACE_TOO_SLOW triggers when pace == TOO_SLOW")
        void trigger_paceTooSlow_matches() throws Exception {
            result.setEmPaceRating(PaceRating.TOO_SLOW);
            assertThat(invokeTrigger("PACE_TOO_SLOW")).isTrue();
        }

        @Test
        @DisplayName("PACE_VERY_FAST triggers when pace == VERY_FAST")
        void trigger_paceVeryFast_matches() throws Exception {
            result.setEmPaceRating(PaceRating.VERY_FAST);
            assertThat(invokeTrigger("PACE_VERY_FAST")).isTrue();
        }

        @Test
        @DisplayName("SKIP_HEAVY triggers when skipped questions > 10")
        void trigger_skipHeavy_matches() throws Exception {
            result.setIntSkippedCount(11);
            assertThat(invokeTrigger("SKIP_HEAVY")).isTrue();
        }

        @Test
        @DisplayName("SKIP_HEAVY does not trigger when skipped questions <= 10")
        void trigger_skipHeavy_doesNotMatch() throws Exception {
            result.setIntSkippedCount(10);
            assertThat(invokeTrigger("SKIP_HEAVY")).isFalse();
        }

        @Test
        @DisplayName("TIME_EXPIRED triggers when the session is submitted by the timer")
        void trigger_timeExpired_matches() throws Exception {
            session.setBSubmittedByTimer(true);
            assertThat(invokeTrigger("TIME_EXPIRED")).isTrue();
        }

        @Test
        @DisplayName("LOW_SCORE triggers when the score is below the threshold")
        void trigger_lowScore_matches() throws Exception {
            result.setBPassedThreshold(false);
            assertThat(invokeTrigger("LOW_SCORE")).isTrue();
        }

        @Test
        @DisplayName("HIGH_ACHIEVER triggers when score >= 37")
        void trigger_highAchiever_matches() throws Exception {
            result.setIntTotalScore(37);
            assertThat(invokeTrigger("HIGH_ACHIEVER")).isTrue();
        }

        @Test
        @DisplayName("HIGH_ACHIEVER does not trigger when score < 37")
        void trigger_highAchiever_doesNotMatch() throws Exception {
            result.setIntTotalScore(36);
            assertThat(invokeTrigger("HIGH_ACHIEVER")).isFalse();
        }

        @Test
        @DisplayName("LOW_COMPLETION triggers when completion < 80%")
        void trigger_lowCompletion_matches() throws Exception {
            result.setDbCompletionPercent(75.0);
            assertThat(invokeTrigger("LOW_COMPLETION")).isTrue();
        }

        @Test
        @DisplayName("SLOW_ON_CORRECT triggers when average correct time > 20s")
        void trigger_slowOnCorrect_matches() throws Exception {
            result.setDbAvgTimeCorrectMs(21_000.0);
            assertThat(invokeTrigger("SLOW_ON_CORRECT")).isTrue();
        }

        @Test
        @DisplayName("WEAK_DOMAIN:NUMERICAL triggers when NUMERICAL is the weakest domain")
        void trigger_weakDomainNumerical_matches() throws Exception {
            result.setEmWeakestDomain("NUMERICAL");
            assertThat(invokeTrigger("WEAK_DOMAIN:NUMERICAL")).isTrue();
        }

        @Test
        @DisplayName("WEAK_DOMAIN:VERBAL does not trigger when NUMERICAL is the weakest domain")
        void trigger_weakDomainVerbal_doesNotMatch() throws Exception {
            result.setEmWeakestDomain("NUMERICAL");
            assertThat(invokeTrigger("WEAK_DOMAIN:VERBAL")).isFalse();
        }

        @Test
        @DisplayName("Unknown rule returns false without exception")
        void trigger_unknownRule_returnsFalse() throws Exception {
            assertThat(invokeTrigger("UNKNOWN_RULE_XYZ")).isFalse();
        }

        // Reflection helper
        private boolean invokeTrigger(String rule) throws Exception {
            var method = UserAdviceServiceImpl.class.getDeclaredMethod(
                    "matchesTrigger", String.class, TestResult.class, List.class);
            method.setAccessible(true);
            return (boolean) method.invoke(service, rule, result, List.of());
        }
    }

    // =========================================================================
    // Advice generation
    // =========================================================================

    @Nested
    @DisplayName("generateForResult() - Generates advice after a test")
    class GenerateForResultTests {

        @Test
        @DisplayName("Generates nothing when no advice card matches")
        void generateForResult_noMatchingCards_savesNothing() {
            when(adviceCardRepository.findAllActive()).thenReturn(List.of());

            service.generateForResult(result, List.of());

            verify(userAdviceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Generates one advice entry for each matching card")
        void generateForResult_matchingCards_savesAdvice() {
            result.setDbAccuracyPercent(45.0); // triggers ACCURACY_BELOW_50
            result.setBPassedThreshold(false); // triggers LOW_SCORE

            AdviceCard card1 = buildAdviceCard(1L, "ACCURACY_BELOW_50", AdvicePriority.HIGH);
            AdviceCard card2 = buildAdviceCard(2L, "LOW_SCORE", AdvicePriority.CRITICAL);

            when(adviceCardRepository.findAllActive()).thenReturn(List.of(card1, card2));
            when(testResultRepository.save(any())).thenReturn(result);
            when(userAdviceRepository.existsByUserLgIdAndAdviceCardLgIdAndResultLgId(
                    anyLong(), anyLong(), anyLong())).thenReturn(false);
            when(userAdviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(adviceCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.generateForResult(result, List.of());

            // 2 matching cards -> 2 saves
            verify(userAdviceRepository, times(2)).save(any(UserAdvice.class));
        }

        @Test
        @DisplayName("Does not duplicate advice that already exists for this result")
        void generateForResult_alreadyExists_skipsAdvice() {
            result.setDbAccuracyPercent(45.0);

            AdviceCard card = buildAdviceCard(1L, "ACCURACY_BELOW_50", AdvicePriority.HIGH);
            when(adviceCardRepository.findAllActive()).thenReturn(List.of(card));
            when(testResultRepository.save(any())).thenReturn(result);
            when(userAdviceRepository.existsByUserLgIdAndAdviceCardLgIdAndResultLgId(
                    anyLong(), anyLong(), anyLong())).thenReturn(true); // already exists

            service.generateForResult(result, List.of());

            verify(userAdviceRepository, never()).save(any(UserAdvice.class));
        }

        @Test
        @DisplayName("Sorts advice by priority: CRITICAL before HIGH before MEDIUM before LOW")
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
            // ACCURACY_BELOW_70 does not match (accuracy < 50, so it should not match ACCURACY_BELOW_70)
            // The ranks must reflect priority: CRITICAL=1, HIGH=2, MEDIUM=3
            assertThat(saved.get(0).getAdviceCard().getEmPriority()).isEqualTo(AdvicePriority.CRITICAL);
            assertThat(saved.get(1).getAdviceCard().getEmPriority()).isEqualTo(AdvicePriority.HIGH);
        }
    }

    // =========================================================================
    // Mark as read
    // =========================================================================

    @Nested
    @DisplayName("markRead() - Marks advice as read")
    class MarkReadTests {

        @Test
        @DisplayName("Marks the advice as read if it was not already read")
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
        @DisplayName("Does not modify dtRead if the advice is already read")
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
        @DisplayName("Throws an exception when the advice does not belong to the user")
        void markRead_wrongUser_throwsException() {
            UserAdvice advice = buildUserAdvice(1L, false);
            when(userAdviceRepository.findById(1L)).thenReturn(Optional.of(advice));

            assertThatThrownBy(() -> service.markRead(1L, "other@test.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // =========================================================================
    // User feedback
    // =========================================================================

    @Nested
    @DisplayName("submitFeedback() - Submits feedback")
    class SubmitFeedbackTests {

        @Test
        @DisplayName("Saves the full feedback correctly")
        void submitFeedback_withAllFields_savesAll() {
            UserAdvice advice = buildUserAdvice(1L, false);
            when(userAdviceRepository.findById(1L)).thenReturn(Optional.of(advice));
            when(userAdviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userAdviceMapper.toResponse(any())).thenReturn(null);

            var request = new UserAdviceFeedbackRequest(1L, true, "Very helpful!", 3000);
            service.submitFeedback(request, "alice@test.com");

            assertThat(advice.getBWasHelpful()).isTrue();
            assertThat(advice.getStrUserNote()).isEqualTo("Very helpful!");
            assertThat(advice.getIntTimeSpentMs()).isEqualTo(3000);
            assertThat(advice.getBWasRead()).isTrue();
        }

        @Test
        @DisplayName("Null feedback fields do not replace existing values")
        void submitFeedback_nullFields_keepsExistingValues() {
            UserAdvice advice = buildUserAdvice(1L, true);
            advice.setBWasHelpful(true);
            advice.setStrUserNote("Existing note");

            when(userAdviceRepository.findById(1L)).thenReturn(Optional.of(advice));
            when(userAdviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userAdviceMapper.toResponse(any())).thenReturn(null);

            var request = new UserAdviceFeedbackRequest(1L, null, null, null);
            service.submitFeedback(request, "alice@test.com");

            assertThat(advice.getBWasHelpful()).isTrue();
            assertThat(advice.getStrUserNote()).isEqualTo("Existing note");
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
        card.setStrTitle("Advice " + id);
        card.setStrContent("Advice content " + id);
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
