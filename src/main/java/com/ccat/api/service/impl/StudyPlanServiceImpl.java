package com.ccat.api.service.impl;

import com.ccat.api.dto.response.*;
import com.ccat.api.model.entity.*;
import com.ccat.api.repository.*;
import com.ccat.api.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyPlanServiceImpl implements StudyPlanService {

    private static final int DRILLS_PER_DAY    = 5;
    private static final int EXAM_DAYS_DEFAULT = 18;

    private final UserRepository              userRepository;
    private final TestResultRepository        testResultRepository;
    private final DomainPerformanceRepository domainPerfRepository;
    private final StudyDrillLogRepository     drillLogRepository;

    @Override
    @Transactional
    public StudyPlanResponse getPlan(String userEmail) {
        User user = userRepository.findByStrEmail(userEmail).orElseThrow();
        LocalDate today = LocalDate.now();

        Optional<TestResult> lastResult = testResultRepository
                .findTopByUserLgIdOrderByDtCalculatedDesc(user.getLgId());

        String weakestDomain = lastResult.map(TestResult::getEmWeakestDomain).orElse("NUMERICAL");
        String paceRating    = lastResult.map(r -> r.getEmPaceRating().name()).orElse("ON_PACE");
        int    lastScore     = lastResult.map(TestResult::getIntTotalScore).orElse(0);

        List<StudyDrillResponse> todayDrills = buildTodayDrills(user, today, weakestDomain, paceRating);
        List<StudyWeekDayResponse> weekPlan  = buildWeekPlan(user, today);
        List<StudyMilestoneResponse> milestones = buildMilestones(user, lastScore);
        List<StudyDomainProgressResponse> domainProgress = buildDomainProgress(user);

        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd   = weekStart.plusDays(6);
        int weekDone  = (int) drillLogRepository.countDoneByUserAndDateRange(
                user.getLgId(), weekStart, weekEnd);
        int weekTotal = (int) drillLogRepository
                .findByUserLgIdAndDtDateBetween(user.getLgId(), weekStart, weekEnd).size();
        if (weekTotal == 0) weekTotal = DRILLS_PER_DAY * 5; // default 5 weekdays

        int streak = computeStreak(user, today);

        return new StudyPlanResponse(
                todayDrills, weekPlan, milestones, domainProgress,
                EXAM_DAYS_DEFAULT, streak, weekDone, weekTotal
        );
    }

    @Override
    @Transactional
    public void completeDrill(String drillKey, String userEmail) {
        User user = userRepository.findByStrEmail(userEmail).orElseThrow();
        LocalDate today = LocalDate.now();

        StudyDrillLog log = drillLogRepository
                .findByUserLgIdAndStrDrillKeyAndDtDate(user.getLgId(), drillKey, today)
                .orElseGet(() -> {
                    StudyDrillLog newLog = new StudyDrillLog();
                    newLog.setUser(user);
                    newLog.setStrDrillKey(drillKey);
                    newLog.setDtDate(today);
                    return newLog;
                });
        log.setBDone(!Boolean.TRUE.equals(log.getBDone()));
        drillLogRepository.save(log);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<StudyDrillResponse> buildTodayDrills(User user, LocalDate today,
                                                       String weakestDomain, String paceRating) {
        record DrillTemplate(String type, String titlePattern, String duration, String priority, int xp) {}

        String domainLabel = domainLabel(weakestDomain);

        List<DrillTemplate> templates = new ArrayList<>(List.of(
            new DrillTemplate("HIGH PRIORITY", domainLabel + ": Pattern Recognition", "15 min", "Critical", 50),
            new DrillTemplate("ACCURACY DRILL", "Abstract Logic: Matrix Rotation",   "20 min", "High",     40),
            new DrillTemplate("THEORY REVIEW",  "Sentence Completion Logic",          "12 min", "Medium",   20),
            new DrillTemplate("MINI MOCK",       "Mixed 10-Question Sprint",           "5 min",  "Review",   60)
        ));

        // Inject speed drill if pace is slow
        if ("TOO_SLOW".equals(paceRating) || "ON_PACE".equals(paceRating)) {
            templates.add(1, new DrillTemplate("SPEED DRILL", "Basic Algebra Flashcards", "10 min", "High", 30));
        } else {
            templates.add(1, new DrillTemplate("SPEED DRILL", domainLabel + " Speed Sprint", "8 min", "High", 30));
        }

        Map<String, Boolean> doneMap = drillLogRepository
                .findByUserLgIdAndDtDate(user.getLgId(), today)
                .stream()
                .collect(Collectors.toMap(StudyDrillLog::getStrDrillKey, StudyDrillLog::getBDone));

        List<StudyDrillResponse> result = new ArrayList<>();
        for (int i = 0; i < templates.size(); i++) {
            DrillTemplate t  = templates.get(i);
            String key       = today + "_" + t.type().replace(" ", "_") + "_" + i;
            boolean done     = Boolean.TRUE.equals(doneMap.get(key));
            result.add(new StudyDrillResponse(key, t.type(), t.titlePattern(), t.duration(), t.priority(), t.xp(), done));
        }
        return result;
    }

    private List<StudyWeekDayResponse> buildWeekPlan(User user, LocalDate today) {
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        List<StudyWeekDayResponse> week = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate day   = monday.plusDays(i);
            boolean isToday = day.equals(today);
            boolean weekend = day.getDayOfWeek().getValue() >= 6;
            int sessions    = weekend ? 0 : DRILLS_PER_DAY;

            int done = (int) drillLogRepository.countDoneByUserAndDate(user.getLgId(), day);
            if (!day.isBefore(today) && !isToday) done = 0;

            week.add(new StudyWeekDayResponse(
                day.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH),
                day.getDayOfMonth(), sessions, Math.min(done, sessions), isToday, weekend
            ));
        }
        return week;
    }

    private List<StudyMilestoneResponse> buildMilestones(User user, int lastScore) {
        List<TestResult> results = testResultRepository.findByUserLgIdOrderByDtCalculatedDesc(user.getLgId());
        int sessionsDone = results.size() * DRILLS_PER_DAY;

        double numAccuracy = results.stream()
                .filter(r -> "NUMERICAL".equals(r.getEmWeakestDomain()) || r.getEmStrongestDomain() != null)
                .mapToDouble(TestResult::getDbAccuracyPercent)
                .average().orElse(0.0);

        double avgSpeedS = results.stream()
                .mapToDouble(r -> r.getDbAvgTimePerQMs() / 1000.0)
                .average().orElse(17.2);

        List<StudyMilestoneResponse> ms = new ArrayList<>();
        ms.add(milestone("Score 40+ on Full Mock", 40, lastScore, "pts",
                lastScore >= 40 ? "done" : "active"));
        ms.add(milestone("Number Series Accuracy", 70, Math.round(numAccuracy * 100.0) / 100.0, "%",
                numAccuracy >= 70 ? "done" : "active"));
        ms.add(milestone("Avg. Speed under 15s", 15, Math.round(avgSpeedS * 10.0) / 10.0, "s",
                avgSpeedS <= 15 ? "done" : "active"));
        ms.add(milestone("Complete 20 Drills", 20, Math.min(sessionsDone, 20), "drills",
                sessionsDone >= 20 ? "done" : "active"));
        ms.add(milestone("Score 45+ on Full Mock", 45, lastScore, "pts",
                lastScore >= 45 ? "done" : lastScore >= 40 ? "active" : "locked"));
        return ms;
    }

    private List<StudyDomainProgressResponse> buildDomainProgress(User user) {
        List<DomainPerformance> perfs = domainPerfRepository
                .findByUserLgIdOrderByDtCreatedDesc(user.getLgId());

        Map<String, DoubleSummaryStatistics> byDomain = perfs.stream()
                .collect(Collectors.groupingBy(
                    dp -> dp.getDomain().getStrLabel(),
                    Collectors.summarizingDouble(DomainPerformance::getDbAccuracyPercent)
                ));

        String[] colors = {"bg-green-500", "bg-blue-500", "bg-orange-400", "bg-purple-500"};
        int idx = 0;
        List<StudyDomainProgressResponse> result = new ArrayList<>();
        for (Map.Entry<String, DoubleSummaryStatistics> e : byDomain.entrySet()) {
            int pct = (int) Math.round(e.getValue().getAverage());
            result.add(new StudyDomainProgressResponse(e.getKey(), pct, colors[idx % colors.length]));
            idx++;
        }

        if (result.isEmpty()) {
            result.add(new StudyDomainProgressResponse("Verbal Reasoning",    0, "bg-green-500"));
            result.add(new StudyDomainProgressResponse("Spatial Reasoning",   0, "bg-blue-500"));
            result.add(new StudyDomainProgressResponse("Numerical Reasoning", 0, "bg-orange-400"));
        }
        return result;
    }

    private int computeStreak(User user, LocalDate today) {
        int streak = 0;
        LocalDate day = today;
        while (true) {
            long done = drillLogRepository.countDoneByUserAndDate(user.getLgId(), day);
            if (done == 0) break;
            streak++;
            day = day.minusDays(1);
        }
        return streak;
    }

    private StudyMilestoneResponse milestone(String label, double target, double current,
                                              String unit, String status) {
        return new StudyMilestoneResponse(label, target, current, unit, status);
    }

    private String domainLabel(String code) {
        if (code == null) return "Number Series";
        return switch (code.toUpperCase()) {
            case "VERBAL"    -> "Verbal Analogies";
            case "SPATIAL"   -> "Spatial Rotation";
            case "NUMERICAL" -> "Number Series";
            default          -> code;
        };
    }
}
