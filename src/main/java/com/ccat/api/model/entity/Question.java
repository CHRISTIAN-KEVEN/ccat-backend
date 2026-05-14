package com.ccat.api.model.entity;

import com.ccat.api.model.enums.ContentType;
import com.ccat.api.model.enums.QuestionDifficulty;
import com.ccat.api.model.enums.QuestionType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @Column(name = "str_uuid", unique = true, nullable = false, length = 36)
    private String strUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "str_domain_code", nullable = false)
    private Domain domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_difficulty", nullable = false, length = 20)
    private QuestionDifficulty emDifficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_question_type", nullable = false, length = 30)
    private QuestionType emQuestionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_content_type", nullable = false, length = 10)
    private ContentType emContentType = ContentType.TEXT;

    @Column(name = "str_question_text")
    private String strQuestionText;

    @Column(name = "str_image_url", length = 500)
    private String strImageUrl;

    @Column(name = "str_image_alt", length = 255)
    private String strImageAlt;

    @Column(name = "str_explanation")
    private String strExplanation;

    @Column(name = "str_hint")
    private String strHint;

    @Column(name = "int_point_value", nullable = false)
    private Integer intPointValue = 1;

    @Column(name = "int_time_limit_ms", nullable = false)
    private Integer intTimeLimitMs = 18000;

    @Column(name = "db_avg_correct_rate", nullable = false)
    private Double dbAvgCorrectRate = 0.0;

    @Column(name = "db_avg_response_ms", nullable = false)
    private Double dbAvgResponseMs = 0.0;

    @Column(name = "b_active", nullable = false)
    private Boolean bActive = true;

    @Column(name = "b_verified", nullable = false)
    private Boolean bVerified = false;

    @Column(name = "str_source", length = 255)
    private String strSource;

    @Column(name = "str_tags")
    private String strTags;

    @Column(name = "int_report_count", nullable = false)
    private Integer intReportCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_created_by")
    private User createdBy;

    @Column(name = "dt_created", nullable = false)
    private LocalDateTime dtCreated;

    @Column(name = "dt_updated", nullable = false)
    private LocalDateTime dtUpdated;

    @PrePersist
    protected void onCreate() {
        dtCreated = LocalDateTime.now();
        dtUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dtUpdated = LocalDateTime.now();
    }
}
