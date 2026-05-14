package com.ccat.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_response",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lg_session_id", "lg_question_id"}))
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_session_id", nullable = false)
    private TestSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_question_id", nullable = false)
    private Question question;

    // NULL when question was skipped — bWasSkipped is true in that case
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_answer_id")
    private Answer answer;

    // Denormalized from Answer.bIsCorrect at write time — avoids joins on analytics queries
    @Column(name = "b_is_correct", nullable = false)
    private Boolean bIsCorrect;

    @Column(name = "b_was_skipped", nullable = false)
    private Boolean bWasSkipped = false;

    @Column(name = "b_was_changed", nullable = false)
    private Boolean bWasChanged = false;

    @Column(name = "int_response_time_ms", nullable = false)
    private Integer intResponseTimeMs = 0;

    @Column(name = "int_display_order", nullable = false)
    private Integer intDisplayOrder;

    @Column(name = "str_answer_options_order", nullable = false)
    private String strAnswerOptionsOrder;

    @Column(name = "int_changed_count", nullable = false)
    private Integer intChangedCount = 0;

    @Column(name = "dt_answered", nullable = false)
    private LocalDateTime dtAnswered;

    @PrePersist
    protected void onCreate() {
        dtAnswered = LocalDateTime.now();
    }
}
