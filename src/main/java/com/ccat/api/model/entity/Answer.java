package com.ccat.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_answer")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_question_id", nullable = false)
    private Question question;

    @Column(name = "str_answer_text", nullable = false)
    private String strAnswerText;

    @Column(name = "str_answer_label", nullable = false, length = 5)
    private String strAnswerLabel;

    @Column(name = "b_is_correct", nullable = false)
    private Boolean bIsCorrect = false;

    @Column(name = "str_explanation")
    private String strExplanation;

    @Column(name = "int_sort_order", nullable = false)
    private Integer intSortOrder = 0;

    @Column(name = "int_chosen_count", nullable = false)
    private Integer intChosenCount = 0;

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
