package com.ccat.api.model.entity;

import com.ccat.api.model.enums.PaceRating;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_test_result")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_session_id", unique = true, nullable = false)
    private TestSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_user_id", nullable = false)
    private User user;

    @Column(name = "int_total_score", nullable = false)
    private Integer intTotalScore;

    @Column(name = "int_question_count", nullable = false)
    private Integer intQuestionCount = 50;

    @Column(name = "int_answered_count", nullable = false)
    private Integer intAnsweredCount;

    @Column(name = "int_skipped_count", nullable = false)
    private Integer intSkippedCount = 0;

    @Column(name = "db_accuracy_percent", nullable = false)
    private Double dbAccuracyPercent;

    @Column(name = "db_completion_percent", nullable = false)
    private Double dbCompletionPercent;

    @Column(name = "int_time_used_ms", nullable = false)
    private Integer intTimeUsedMs;

    @Column(name = "int_time_available_ms", nullable = false)
    private Integer intTimeAvailableMs = 900000;

    @Column(name = "db_avg_time_per_q_ms", nullable = false)
    private Double dbAvgTimePerQMs;

    @Column(name = "db_avg_time_correct_ms", nullable = false)
    private Double dbAvgTimeCorrectMs = 0.0;

    @Column(name = "db_avg_time_wrong_ms", nullable = false)
    private Double dbAvgTimeWrongMs = 0.0;

    @Column(name = "em_weakest_domain", length = 50)
    private String emWeakestDomain;

    @Column(name = "em_strongest_domain", length = 50)
    private String emStrongestDomain;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_pace_rating", nullable = false, length = 20)
    private PaceRating emPaceRating;

    @Column(name = "int_percentile_estimate")
    private Integer intPercentileEstimate;

    @Column(name = "b_passed_threshold", nullable = false)
    private Boolean bPassedThreshold = false;

    @Column(name = "str_advice_trigger_codes")
    private String strAdviceTriggerCodes;

    @Column(name = "dt_calculated", nullable = false)
    private LocalDateTime dtCalculated;

    @PrePersist
    protected void onCreate() {
        dtCalculated = LocalDateTime.now();
    }
}
