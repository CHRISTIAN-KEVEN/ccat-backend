package com.ccat.api.service.impl;

import com.ccat.api.dto.request.ResponseSubmitRequest;
import com.ccat.api.dto.request.TestSessionCreateRequest;
import com.ccat.api.dto.response.DomainPerformanceResponse;
import com.ccat.api.dto.response.QuestionResponse;
import com.ccat.api.dto.response.QuestionReviewResponse;
import com.ccat.api.dto.response.ResponseSubmitResponse;
import com.ccat.api.dto.response.TestResultResponse;
import com.ccat.api.dto.response.TestSessionResponse;
import com.ccat.api.exception.FreeTestAlreadyUsedException;
import com.ccat.api.exception.QuestionNotFoundException;
import com.ccat.api.exception.SessionAlreadySubmittedException;
import com.ccat.api.exception.SessionExpiredException;
import com.ccat.api.exception.TestSessionNotFoundException;
import com.ccat.api.mapper.AnswerMapper;
import com.ccat.api.mapper.DomainPerformanceMapper;
import com.ccat.api.mapper.QuestionMapper;
import com.ccat.api.mapper.ResponseMapper;
import com.ccat.api.mapper.TestResultMapper;
import com.ccat.api.mapper.TestSessionMapper;
import com.ccat.api.model.entity.*;
import com.ccat.api.model.enums.*;
import com.ccat.api.repository.*;
import com.ccat.api.service.TestSessionService;
import com.ccat.api.service.UserAdviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestSessionServiceImpl implements TestSessionService {

    // CCAT defaults.
    private static final int QUESTION_COUNT   = 50;
    private static final int DURATION_SECONDS = 900;
    private static final int PASS_THRESHOLD   = 24;

    private final TestSessionRepository       sessionRepository;
    private final TestResultRepository        resultRepository;
    private final ResponseRepository          responseRepository;
    private final QuestionRepository          questionRepository;
    private final AnswerRepository            answerRepository;
    private final DomainRepository            domainRepository;
    private final DomainPerformanceRepository domainPerfRepository;
    private final UserRepository              userRepository;
    private final TestSessionMapper           sessionMapper;
    private final TestResultMapper            resultMapper;
    private final ResponseMapper              responseMapper;
    private final DomainPerformanceMapper     domainPerfMapper;
    private final QuestionMapper              questionMapper;
    private final AnswerMapper                answerMapper;
    private final UserAdviceService           userAdviceService;

    // Test lifecycle.

    /**
     * Starts a new test session for the current user.
     */
    @Override
    @Transactional
    public TestSessionResponse start(TestSessionCreateRequest request, String userEmail) {
        User user = userRepository.findByStrEmail(userEmail).orElseThrow();

        // Allow only one free diagnostic per user.
        if (request.emSessionType() == SessionType.FREE_DIAGNOSTIC) {
            if (sessionRepository.countFreeTestsByUser(user.getLgId()) > 0) {
                throw new FreeTestAlreadyUsedException();
            }
        }

        // Use defaults when the client does not provide a ratio.
        String domainRatio   = request.emDomainRatio()   != null ? request.emDomainRatio()   : "40-40-20";
        String difficultyMix = request.emDifficultyMix() != null ? request.emDifficultyMix() : "30-50-20";

        // Store question ids as a JSON array string.
        String questionOrder = pickQuestions(domainRatio, difficultyMix);

        TestSession session = sessionMapper.toEntity(request, user);
        session.setStrUuid(UUID.randomUUID().toString());
        session.setStrQuestionOrder(questionOrder);

        LocalDateTime now = LocalDateTime.now();
        session.setDtStarted(now);
        // Persist the server-side deadline.
        session.setDtExpires(now.plusSeconds(DURATION_SECONDS));

        return sessionMapper.toResponse(sessionRepository.save(session));
    }

    /**
     * Creates or updates a response for a session question.
     */
    @Override
    @Transactional
    public ResponseSubmitResponse submitResponse(Long sessionId, ResponseSubmitRequest request, String userEmail) {
        TestSession session = getSessionOwnedBy(sessionId, userEmail);
        if (session.getEmStatus() != SessionStatus.ACTIVE) {
            throw new SessionAlreadySubmittedException(sessionId);
        }

        if (expireIfNeeded(session, LocalDateTime.now())) {
            throw new SessionExpiredException(sessionId);
        }

        Question question = questionRepository.findById(request.lgQuestionId())
                .orElseThrow(() -> new QuestionNotFoundException(String.valueOf(request.lgQuestionId())));

        // A null answer means the question was skipped.
        Answer answer = null;
        if (request.lgAnswerId() != null) {
            answer = answerRepository.findById(request.lgAnswerId())
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + request.lgAnswerId()));
        }

        // One response per session/question pair.
        Optional<Response> existing = responseRepository.findBySessionLgIdAndQuestionLgId(sessionId, question.getLgId());
        Response response;

        if (existing.isPresent()) {
            // Backtracking disabled — reject re-submission of already answered questions.
            if (!Boolean.TRUE.equals(session.getBAllowBacktrack())) {
                throw new IllegalStateException("Backtracking is not allowed for this session");
            }
            // Update an existing answer.
            response = existing.get();
            boolean wasSkipped = response.getBWasSkipped();

            response.setAnswer(answer);
            response.setBIsCorrect(answer != null && answer.getBIsCorrect());
            response.setBWasSkipped(answer == null);
            response.setBWasChanged(true);
            response.setIntChangedCount(response.getIntChangedCount() + 1);
            response.setIntResponseTimeMs(request.intResponseTimeMs());

            // Convert a previous skip into an answered question.
            if (wasSkipped && answer != null) {
                session.setIntQuestionsAnswered(session.getIntQuestionsAnswered() + 1);
                session.setIntQuestionsSkipped(Math.max(0, session.getIntQuestionsSkipped() - 1));
            }
        } else {
            // Create the first response for this question.
            response = responseMapper.toEntity(request, session, question, answer);
            if (answer == null) {
                session.setIntQuestionsSkipped(session.getIntQuestionsSkipped() + 1);
            } else {
                session.setIntQuestionsAnswered(session.getIntQuestionsAnswered() + 1);
            }
        }

        sessionRepository.save(session);
        return responseMapper.toResponse(responseRepository.save(response));
    }

    /**
     * Closes the session and computes its results.
     */
    @Override
    @Transactional
    public TestResultResponse finish(Long sessionId, boolean submittedByTimer, String userEmail) {
        TestSession session = getSessionOwnedBy(sessionId, userEmail);
        LocalDateTime now = LocalDateTime.now();

        if (session.getEmStatus() == SessionStatus.ACTIVE) {
            boolean timerTriggered = submittedByTimer || isExpired(session, now);
            LocalDateTime submittedAt = timerTriggered ? minDateTime(now, session.getDtExpires()) : now;
            return finalizeSession(session, timerTriggered, submittedAt);
        }

        return getStoredResult(sessionId);
    }

    /**
     * Returns the result of a submitted session.
     */
    @Override
    @Transactional
    public TestResultResponse getResult(Long sessionId, String userEmail) {
        TestSession session = getSessionOwnedBy(sessionId, userEmail);
        if (session.getEmStatus() == SessionStatus.ACTIVE && expireIfNeeded(session, LocalDateTime.now())) {
            return getStoredResult(sessionId);
        }
        return getStoredResult(sessionId);
    }

    /** Returns session details. */
    @Override
    @Transactional
    public TestSessionResponse getById(Long sessionId, String userEmail) {
        TestSession session = getSessionOwnedBy(sessionId, userEmail);
        expireIfNeeded(session, LocalDateTime.now());
        return sessionMapper.toResponse(session);
    }

    /** Returns the user's sessions from newest to oldest. */
    @Override
    @Transactional
    public List<TestSessionResponse> getMyHistory(String userEmail) {
        User user = userRepository.findByStrEmail(userEmail).orElseThrow();
        List<TestSession> sessions = sessionRepository.findByUserLgIdOrderByDtStartedDesc(user.getLgId());
        LocalDateTime now = LocalDateTime.now();
        sessions.forEach(session -> expireIfNeeded(session, now));
        return sessions.stream().map(sessionMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public List<QuestionResponse> getSessionQuestions(Long sessionId, String userEmail) {
        TestSession session = getSessionOwnedBy(sessionId, userEmail);
        if (expireIfNeeded(session, LocalDateTime.now()) || session.getEmStatus() != SessionStatus.ACTIVE) {
            throw new SessionExpiredException(sessionId);
        }
        List<Long> ids = parseQuestionOrder(session.getStrQuestionOrder());
        List<Question> questions = questionRepository.findAllById(ids);
        Map<Long, Question> byId = questions.stream()
                .collect(Collectors.toMap(Question::getLgId, q -> q));
        return ids.stream()
                .filter(byId::containsKey)
                .map(id -> {
                    Question q = byId.get(id);
                    var answers = answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(q.getLgId())
                            .stream().map(answerMapper::toResponse).toList();
                    return questionMapper.toResponseWithAnswers(q, answers);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionReviewResponse> getReview(Long sessionId, String userEmail) {
        TestSession session = getSessionOwnedBy(sessionId, userEmail);
        if (session.getEmStatus() != SessionStatus.SUBMITTED) {
            throw new IllegalStateException("Review is only available after the session has been submitted");
        }

        List<Response> responses = responseRepository.findBySessionLgIdForReview(sessionId);

        return responses.stream().map(r -> {
            Question q = r.getQuestion();
            var answers = answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(q.getLgId())
                    .stream().map(answerMapper::toResponse).toList();
            Answer chosen = r.getAnswer();
            return new QuestionReviewResponse(
                    r.getIntDisplayOrder(),
                    q.getLgId(),
                    q.getStrQuestionText(),
                    q.getStrImageUrl(),
                    q.getStrImageAlt(),
                    q.getEmContentType(),
                    q.getEmDifficulty(),
                    q.getEmQuestionType(),
                    q.getDomain().getStrDomainCode(),
                    q.getStrExplanation(),
                    q.getStrHint(),
                    answers,
                    chosen != null ? chosen.getLgId() : null,
                    chosen != null ? chosen.getStrAnswerLabel() : null,
                    r.getBIsCorrect(),
                    r.getBWasSkipped(),
                    r.getBWasChanged(),
                    r.getIntChangedCount(),
                    r.getIntResponseTimeMs()
            );
        }).toList();
    }

    private List<Long> parseQuestionOrder(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            String cleaned = json.replaceAll("[\\[\\]\\s]", "");
            if (cleaned.isEmpty()) return Collections.emptyList();
            return Arrays.stream(cleaned.split(",")).map(Long::parseLong).toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Picks and shuffles questions using the requested domain ratio.
     */
    private String pickQuestions(String domainRatioStr, String difficultyMixStr) {
        List<Domain> domains = domainRepository.findByEmStatusOrderByIntSortOrderAsc(DomainStatus.ACTIVE);
        if (domains.isEmpty()) throw new IllegalStateException("No active domains available");

        int[] ratios = parseRatios(domainRatioStr, domains.size());
        int[] counts = distributeQuestions(QUESTION_COUNT, ratios);

        List<Long> questionIds = new ArrayList<>();

        for (int i = 0; i < domains.size(); i++) {
            int needed = counts[i];
            if (needed <= 0) continue;
            List<Question> picked = questionRepository.findRandomByDomainCode(
                    domains.get(i).getStrDomainCode(), needed);
            picked.forEach(q -> questionIds.add(q.getLgId()));
        }

        // Fill gaps if some domains do not have enough questions.
        if (questionIds.size() < QUESTION_COUNT) {
            Set<Long> existing = new HashSet<>(questionIds);
            List<Question> extras = questionRepository.findRandom(QUESTION_COUNT * 2);
            for (Question q : extras) {
                if (!existing.contains(q.getLgId())) {
                    questionIds.add(q.getLgId());
                    existing.add(q.getLgId());
                }
                if (questionIds.size() >= QUESTION_COUNT) break;
            }
        }

        Collections.shuffle(questionIds);
        List<Long> finalIds = questionIds.stream().limit(QUESTION_COUNT).toList();

        // Expected by TestSessionMapper.parseJsonLongList().
        return finalIds.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * Parses a ratio string such as "40-40-20".
     */
    private int[] parseRatios(String ratioStr, int domainCount) {
        try {
            String[] parts = ratioStr.split("-");
            int[] ratios = new int[Math.min(parts.length, domainCount)];
            for (int i = 0; i < ratios.length; i++) {
                ratios[i] = Integer.parseInt(parts[i].trim());
            }
            return ratios;
        } catch (Exception e) {
            // Fall back to an even split.
            int[] ratios = new int[domainCount];
            Arrays.fill(ratios, 100 / domainCount);
            return ratios;
        }
    }

    /** Converts percentages into question counts. */
    private int[] distributeQuestions(int total, int[] ratios) {
        int sum = Arrays.stream(ratios).sum();
        if (sum == 0) {
            // Fall back to an even split.
            int[] counts = new int[ratios.length];
            Arrays.fill(counts, total / ratios.length);
            return counts;
        }
        int[] counts = new int[ratios.length];
        int assigned = 0;
        for (int i = 0; i < ratios.length; i++) {
            counts[i] = (int) Math.round((double) ratios[i] / sum * total);
            assigned += counts[i];
        }
        // Absorb rounding drift in the last bucket.
        if (assigned != total && counts.length > 0) {
            counts[counts.length - 1] += (total - assigned);
        }
        return counts;
    }

    /**
     * Builds the aggregate result for a submitted session.
     */
    private TestResult buildResult(TestSession session, List<Response> responses, LocalDateTime submittedAt) {
        long correct  = responses.stream().filter(r -> !r.getBWasSkipped() && r.getBIsCorrect()).count();
        long answered = responses.stream().filter(r -> !r.getBWasSkipped()).count();
        long skipped  = responses.stream().filter(Response::getBWasSkipped).count();

        // Cap runtime at the configured session duration.
        long timeUsedMs = Duration.between(session.getDtStarted(), submittedAt).toMillis();
        timeUsedMs = Math.min(timeUsedMs, (long) session.getIntDurationSeconds() * 1000);

        double accuracy    = answered > 0 ? correct * 100.0 / answered : 0.0;
        double completion  = responses.size() * 100.0 / session.getIntQuestionCount();
        double avgTimePerQ = answered > 0 ? timeUsedMs * 1.0 / answered : 0.0;

        // Keep correct and wrong timing separate for reporting.
        double avgTimeCorrect = responses.stream()
                .filter(r -> !r.getBWasSkipped() && r.getBIsCorrect())
                .mapToInt(Response::getIntResponseTimeMs).average().orElse(0.0);
        double avgTimeWrong = responses.stream()
                .filter(r -> !r.getBWasSkipped() && !r.getBIsCorrect())
                .mapToInt(Response::getIntResponseTimeMs).average().orElse(0.0);

        PaceRating pace;
        if      (timeUsedMs < 300_000)  pace = PaceRating.VERY_FAST;
        else if (timeUsedMs < 600_000)  pace = PaceRating.FAST;
        else if (timeUsedMs <= 900_000) pace = PaceRating.ON_PACE;
        else                            pace = PaceRating.TOO_SLOW;

        TestResult result = new TestResult();
        result.setSession(session);
        result.setUser(session.getUser());
        result.setIntTotalScore((int) correct);
        result.setIntQuestionCount(session.getIntQuestionCount());
        result.setIntAnsweredCount((int) answered);
        result.setIntSkippedCount((int) skipped);
        result.setDbAccuracyPercent(accuracy);
        result.setDbCompletionPercent(completion);
        result.setIntTimeUsedMs((int) Math.min(timeUsedMs, Integer.MAX_VALUE));
        result.setIntTimeAvailableMs(session.getIntDurationSeconds() * 1000);
        result.setDbAvgTimePerQMs(avgTimePerQ);
        result.setDbAvgTimeCorrectMs(avgTimeCorrect);
        result.setDbAvgTimeWrongMs(avgTimeWrong);
        result.setEmPaceRating(pace);
        result.setIntPercentileEstimate(estimatePercentile((int) correct));
        result.setBPassedThreshold(correct >= PASS_THRESHOLD);
        // Set by buildDomainPerformances().
        return result;
    }

    /**
     * Builds per-domain performance metrics and rankings.
     */
    private List<DomainPerformance> buildDomainPerformances(TestSession session, TestResult result, List<Response> responses) {
        Map<Domain, List<Response>> byDomain = responses.stream()
                .collect(Collectors.groupingBy(r -> r.getQuestion().getDomain()));

        List<DomainPerformance> performances = new ArrayList<>();

        for (Map.Entry<Domain, List<Response>> entry : byDomain.entrySet()) {
            Domain domain = entry.getKey();
            List<Response> dr = entry.getValue();

            long correct  = dr.stream().filter(r -> !r.getBWasSkipped() && r.getBIsCorrect()).count();
            long wrong    = dr.stream().filter(r -> !r.getBWasSkipped() && !r.getBIsCorrect()).count();
            long skipped  = dr.stream().filter(Response::getBWasSkipped).count();
            long answered = correct + wrong;

            double accuracy    = answered > 0 ? correct * 100.0 / answered : 0.0;
            double avgResponse = dr.stream().mapToInt(Response::getIntResponseTimeMs).average().orElse(0.0);
            double avgCorrect  = dr.stream().filter(r -> r.getBIsCorrect() && !r.getBWasSkipped())
                    .mapToInt(Response::getIntResponseTimeMs).average().orElse(0.0);
            double avgWrong    = dr.stream().filter(r -> !r.getBIsCorrect() && !r.getBWasSkipped())
                    .mapToInt(Response::getIntResponseTimeMs).average().orElse(0.0);

            // Build a compact difficulty summary string.
            String diffBreakdown = dr.stream()
                    .collect(Collectors.groupingBy(r -> r.getQuestion().getEmDifficulty(), Collectors.counting()))
                    .entrySet().stream()
                    .map(e -> e.getKey().name() + ":" + e.getValue())
                    .collect(Collectors.joining(","));

            DomainPerformance dp = new DomainPerformance();
            dp.setResult(result);
            dp.setSession(session);
            dp.setUser(session.getUser());
            dp.setDomain(domain);
            dp.setIntCorrectCount((int) correct);
            dp.setIntWrongCount((int) wrong);
            dp.setIntSkippedCount((int) skipped);
            dp.setIntTotalCount(dr.size());
            dp.setDbAccuracyPercent(accuracy);
            dp.setDbAvgResponseMs(avgResponse);
            dp.setDbAvgCorrectMs(avgCorrect);
            dp.setDbAvgWrongMs(avgWrong);
            dp.setEmDifficultyBreakdown(diffBreakdown.isEmpty() ? "N/A" : diffBreakdown);
            dp.setIntRankInSession(0);
            performances.add(dp);
        }

        // Rank domains by descending accuracy.
        performances.sort(Comparator.comparingDouble(DomainPerformance::getDbAccuracyPercent).reversed());
        for (int i = 0; i < performances.size(); i++) {
            performances.get(i).setIntRankInSession(i + 1);
        }

        // Mirror the strongest and weakest domains on the global result.
        if (!performances.isEmpty()) {
            result.setEmStrongestDomain(performances.get(0).getDomain().getStrDomainCode());
            result.setEmWeakestDomain(performances.get(performances.size() - 1).getDomain().getStrDomainCode());
        }

        return performances;
    }

    /** Returns a rough percentile estimate from the total score. */
    private int estimatePercentile(int score) {
        if (score >= 42) return 95;
        if (score >= 37) return 85;
        if (score >= 32) return 75;
        if (score >= 28) return 65;
        if (score >= 24) return 50;
        if (score >= 18) return 35;
        if (score >= 12) return 20;
        return 10;
    }

    /**
     * Returns an active session owned by the current user.
     */
    private TestSession getActiveSession(Long sessionId, String userEmail) {
        TestSession session = getSessionOwnedBy(sessionId, userEmail);
        if (session.getEmStatus() != SessionStatus.ACTIVE) {
            throw new SessionAlreadySubmittedException(sessionId);
        }
        return session;
    }

    private boolean isExpired(TestSession session, LocalDateTime now) {
        return session.getDtExpires() != null && !session.getDtExpires().isAfter(now);
    }

    private boolean expireIfNeeded(TestSession session, LocalDateTime now) {
        if (session.getEmStatus() != SessionStatus.ACTIVE || !isExpired(session, now)) {
            return false;
        }
        finalizeSession(session, true, minDateTime(now, session.getDtExpires()));
        return true;
    }

    private LocalDateTime minDateTime(LocalDateTime left, LocalDateTime right) {
        if (left == null) return right;
        if (right == null) return left;
        return left.isBefore(right) ? left : right;
    }

    private TestResultResponse finalizeSession(TestSession session, boolean submittedByTimer, LocalDateTime submittedAt) {
        if (session.getEmStatus() != SessionStatus.ACTIVE) {
            return getStoredResult(session.getLgId());
        }

        session.setEmStatus(SessionStatus.SUBMITTED);
        session.setDtSubmitted(submittedAt != null ? submittedAt : LocalDateTime.now());
        session.setBSubmittedByTimer(submittedByTimer);
        sessionRepository.save(session);

        Optional<TestResult> existing = resultRepository.findBySessionLgId(session.getLgId());
        if (existing.isPresent()) {
            return toResultResponse(existing.get());
        }

        List<Response> responses = responseRepository.findBySessionLgId(session.getLgId());

        TestResult result = buildResult(session, responses, session.getDtSubmitted());
        TestResult savedResult = resultRepository.save(result);

        List<DomainPerformance> performances = buildDomainPerformances(session, savedResult, responses);
        List<DomainPerformance> savedPerfs = domainPerfRepository.saveAll(performances);

        resultRepository.save(savedResult);
        userAdviceService.generateForResult(savedResult, savedPerfs);

        List<DomainPerformanceResponse> perfResponses = savedPerfs.stream()
                .map(domainPerfMapper::toResponse).toList();
        return resultMapper.toResponse(savedResult, perfResponses);
    }

    private TestResultResponse getStoredResult(Long sessionId) {
        TestResult result = resultRepository.findBySessionLgId(sessionId)
                .orElseThrow(() -> new IllegalStateException("Results not yet available for session " + sessionId));
        return toResultResponse(result);
    }

    private TestResultResponse toResultResponse(TestResult result) {
        List<DomainPerformanceResponse> perfs = domainPerfRepository.findByResultLgId(result.getLgId())
                .stream().map(domainPerfMapper::toResponse).toList();
        return resultMapper.toResponse(result, perfs);
    }

    /**
     * Returns a session owned by the current user.
     * Uses 404 instead of 403 to avoid leaking valid ids.
     */
    private TestSession getSessionOwnedBy(Long sessionId, String userEmail) {
        TestSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new TestSessionNotFoundException(sessionId));
        if (!session.getUser().getStrEmail().equals(userEmail)) {
            throw new TestSessionNotFoundException(sessionId);
        }
        return session;
    }
}
