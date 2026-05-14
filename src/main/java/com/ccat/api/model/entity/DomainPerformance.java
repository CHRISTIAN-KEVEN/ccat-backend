package com.ccat.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_domain_performance")
public class DomainPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_result_id", nullable = false)
    private TestResult result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_session_id", nullable = false)
    private TestSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "str_domain_code", nullable = false)
    private Domain domain;

    @Column(name = "int_correct_count", nullable = false)
    private Integer intCorrectCount;

    @Column(name = "int_wrong_count", nullable = false)
    private Integer intWrongCount;

    @Column(name = "int_skipped_count", nullable = false)
    private Integer intSkippedCount = 0;

    @Column(name = "int_total_count", nullable = false)
    private Integer intTotalCount;

    @Column(name = "db_accuracy_percent", nullable = false)
    private Double dbAccuracyPercent;

    @Column(name = "db_avg_response_ms", nullable = false)
    private Double dbAvgResponseMs;

    @Column(name = "db_avg_correct_ms", nullable = false)
    private Double dbAvgCorrectMs = 0.0;

    @Column(name = "db_avg_wrong_ms", nullable = false)
    private Double dbAvgWrongMs = 0.0;

    @Column(name = "em_difficulty_breakdown", nullable = false, length = 100)
    private String emDifficultyBreakdown;

    @Column(name = "int_rank_in_session", nullable = false)
    private Integer intRankInSession;

    @Column(name = "dt_created", nullable = false)
    private LocalDateTime dtCreated;

    @PrePersist
    protected void onCreate() {
        dtCreated = LocalDateTime.now();
    }
}
