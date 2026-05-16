package com.ccat.api.service.impl;

import com.ccat.api.dto.request.UserAdviceFeedbackRequest;
import com.ccat.api.dto.response.UserAdviceResponse;
import com.ccat.api.mapper.UserAdviceMapper;
import com.ccat.api.model.entity.AdviceCard;
import com.ccat.api.model.entity.DomainPerformance;
import com.ccat.api.model.entity.TestResult;
import com.ccat.api.model.entity.UserAdvice;
import com.ccat.api.model.enums.AdvicePriority;
import com.ccat.api.model.enums.PaceRating;
import com.ccat.api.repository.AdviceCardRepository;
import com.ccat.api.repository.TestResultRepository;
import com.ccat.api.repository.TestSessionRepository;
import com.ccat.api.repository.UserAdviceRepository;
import com.ccat.api.service.UserAdviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAdviceServiceImpl implements UserAdviceService {

    private final UserAdviceRepository    userAdviceRepository;
    private final AdviceCardRepository    adviceCardRepository;
    private final TestResultRepository    testResultRepository;
    private final TestSessionRepository   sessionRepository;
    private final UserAdviceMapper        userAdviceMapper;

    // -------------------------------------------------------------------------
    // Generation
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void generateForResult(TestResult result, List<DomainPerformance> perfs) {
        List<AdviceCard> all = adviceCardRepository.findAllActive();

        List<AdviceCard> matched = all.stream()
                .filter(card -> matchesTrigger(card.getEmTriggerRule(), result, perfs))
                .sorted(Comparator.comparingInt(card -> priorityOrder(card.getEmPriority())))
                .toList();

        if (matched.isEmpty()) return;

        String triggerCodes = matched.stream()
                .map(AdviceCard::getEmTriggerRule)
                .distinct()
                .collect(Collectors.joining(","));
        result.setStrAdviceTriggerCodes(triggerCodes);
        testResultRepository.save(result);

        for (int i = 0; i < matched.size(); i++) {
            AdviceCard card = matched.get(i);
            if (userAdviceRepository.existsByUserLgIdAndAdviceCardLgIdAndResultLgId(
                    result.getUser().getLgId(), card.getLgId(), result.getLgId())) {
                continue;
            }
            UserAdvice advice = new UserAdvice();
            advice.setUser(result.getUser());
            advice.setResult(result);
            advice.setAdviceCard(card);
            advice.setEmTriggerMatched(card.getEmTriggerRule());
            advice.setIntDisplayRank(i + 1);
            userAdviceRepository.save(advice);

            card.setIntDisplayCount(card.getIntDisplayCount() + 1);
            adviceCardRepository.save(card);
        }
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<UserAdviceResponse> getBySession(Long sessionId, String userEmail) {
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        if (!session.getUser().getStrEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        TestResult result = testResultRepository.findBySessionLgId(sessionId)
                .orElseThrow(() -> new IllegalStateException("Results not available yet for session " + sessionId));

        return userAdviceRepository.findByResultLgIdOrderByIntDisplayRankAsc(result.getLgId())
                .stream().map(userAdviceMapper::toResponse).toList();
    }

    // -------------------------------------------------------------------------
    // User interactions
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public UserAdviceResponse markRead(Long userAdviceId, String userEmail) {
        UserAdvice advice = getOwnedAdvice(userAdviceId, userEmail);
        if (!advice.getBWasRead()) {
            advice.setBWasRead(true);
            advice.setDtRead(LocalDateTime.now());
        }
        return userAdviceMapper.toResponse(userAdviceRepository.save(advice));
    }

    @Override
    @Transactional
    public UserAdviceResponse submitFeedback(UserAdviceFeedbackRequest request, String userEmail) {
        UserAdvice advice = getOwnedAdvice(request.lgUserAdviceId(), userEmail);
        if (request.bWasHelpful()    != null) advice.setBWasHelpful(request.bWasHelpful());
        if (request.strUserNote()    != null) advice.setStrUserNote(request.strUserNote());
        if (request.intTimeSpentMs() != null) advice.setIntTimeSpentMs(request.intTimeSpentMs());
        if (!advice.getBWasRead()) {
            advice.setBWasRead(true);
            advice.setDtRead(LocalDateTime.now());
        }
        return userAdviceMapper.toResponse(userAdviceRepository.save(advice));
    }

    // -------------------------------------------------------------------------
    // Trigger rule engine
    // -------------------------------------------------------------------------

    /**
     * Supported trigger rules:
     *   ACCURACY_BELOW_50  — accuracy < 50 %
     *   ACCURACY_BELOW_70  — 50 % ≤ accuracy < 70 %
     *   PACE_TOO_SLOW      — PaceRating == TOO_SLOW
     *   PACE_VERY_FAST     — PaceRating == VERY_FAST (possible random guessing)
     *   SKIP_HEAVY         — skipped > 10 questions
     *   TIME_EXPIRED       — session was auto-submitted by the timer
     *   LOW_SCORE          — score below passing threshold (24)
     *   HIGH_ACHIEVER      — score ≥ 37
     *   LOW_COMPLETION     — completion < 80 %
     *   SLOW_ON_CORRECT    — avg time on correct answers > 20 s
     *   WEAK_DOMAIN:{code} — the given domain is the user's weakest
     */
    private boolean matchesTrigger(String rule, TestResult result, List<DomainPerformance> perfs) {
        return switch (rule) {
            case "ACCURACY_BELOW_50" -> result.getDbAccuracyPercent() < 50.0;
            case "ACCURACY_BELOW_70" -> result.getDbAccuracyPercent() >= 50.0 && result.getDbAccuracyPercent() < 70.0;
            case "PACE_TOO_SLOW"     -> result.getEmPaceRating() == PaceRating.TOO_SLOW;
            case "PACE_VERY_FAST"    -> result.getEmPaceRating() == PaceRating.VERY_FAST;
            case "SKIP_HEAVY"        -> result.getIntSkippedCount() > 10;
            case "TIME_EXPIRED"      -> Boolean.TRUE.equals(result.getSession().getBSubmittedByTimer());
            case "LOW_SCORE"         -> !result.getBPassedThreshold();
            case "HIGH_ACHIEVER"     -> result.getIntTotalScore() >= 37;
            case "LOW_COMPLETION"    -> result.getDbCompletionPercent() < 80.0;
            case "SLOW_ON_CORRECT"   -> result.getDbAvgTimeCorrectMs() > 20_000;
            default -> {
                if (rule.startsWith("WEAK_DOMAIN:")) {
                    String code = rule.substring("WEAK_DOMAIN:".length());
                    yield code.equals(result.getEmWeakestDomain());
                }
                yield false;
            }
        };
    }

    private int priorityOrder(AdvicePriority priority) {
        return switch (priority) {
            case CRITICAL -> 0;
            case HIGH     -> 1;
            case MEDIUM   -> 2;
            case LOW      -> 3;
        };
    }

    private UserAdvice getOwnedAdvice(Long userAdviceId, String userEmail) {
        UserAdvice advice = userAdviceRepository.findById(userAdviceId)
                .orElseThrow(() -> new IllegalArgumentException("Advice not found: " + userAdviceId));
        if (!advice.getUser().getStrEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Advice not found: " + userAdviceId);
        }
        return advice;
    }
}
