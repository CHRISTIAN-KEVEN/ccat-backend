package com.ccat.api.model.entity;

import com.ccat.api.model.enums.SessionStatus;
import com.ccat.api.model.enums.SessionType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_test_session")
public class TestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @Column(name = "str_uuid", unique = true, nullable = false, length = 36)
    private String strUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_session_type", nullable = false, length = 20)
    private SessionType emSessionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_status", nullable = false, length = 20)
    private SessionStatus emStatus = SessionStatus.ACTIVE;

    @Column(name = "b_is_free_test", nullable = false)
    private Boolean bIsFreeTest = false;

    @Column(name = "str_eligibility_version", nullable = false, length = 50)
    private String strEligibilityVersion;

    @Column(name = "int_question_count", nullable = false)
    private Integer intQuestionCount = 50;

    @Column(name = "int_duration_seconds", nullable = false)
    private Integer intDurationSeconds = 900;

    // Stored as plain JSON string "[42,7,18,...]" — deserialized in the mapper layer, not here
    @Column(name = "str_question_order", nullable = false)
    private String strQuestionOrder;

    @Column(name = "em_domain_ratio", nullable = false, length = 50)
    private String emDomainRatio = "40-40-20";

    @Column(name = "em_difficulty_mix", nullable = false, length = 50)
    private String emDifficultyMix = "30-50-20";

    @Column(name = "b_allow_backtrack", nullable = false)
    private Boolean bAllowBacktrack = false;

    @Column(name = "b_submitted_by_timer", nullable = false)
    private Boolean bSubmittedByTimer = false;

    @Column(name = "int_questions_answered", nullable = false)
    private Integer intQuestionsAnswered = 0;

    @Column(name = "int_questions_skipped", nullable = false)
    private Integer intQuestionsSkipped = 0;

    @Column(name = "str_client_ip", length = 45)
    private String strClientIp;

    @Column(name = "str_user_agent")
    private String strUserAgent;

    @Column(name = "dt_started", nullable = false)
    private LocalDateTime dtStarted;

    // Mirror of dtStarted + intDurationSeconds for SQL queries — Redis TTL is the actual authority
    @Column(name = "dt_expires", nullable = false)
    private LocalDateTime dtExpires;

    @Column(name = "dt_submitted")
    private LocalDateTime dtSubmitted;

    @Column(name = "dt_created", nullable = false)
    private LocalDateTime dtCreated;

    @PrePersist
    protected void onCreate() {
        dtCreated = LocalDateTime.now();
        if (dtStarted == null) dtStarted = LocalDateTime.now();
    }
}
