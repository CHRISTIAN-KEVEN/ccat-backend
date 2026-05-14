package com.ccat.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_user_advice",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lg_user_id", "lg_advice_card_id", "lg_result_id"}))
public class UserAdvice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_result_id", nullable = false)
    private TestResult result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_advice_card_id", nullable = false)
    private AdviceCard adviceCard;

    @Column(name = "em_trigger_matched", nullable = false, length = 100)
    private String emTriggerMatched;

    @Column(name = "int_display_rank", nullable = false)
    private Integer intDisplayRank;

    @Column(name = "b_was_read", nullable = false)
    private Boolean bWasRead = false;

    @Column(name = "b_was_helpful")
    private Boolean bWasHelpful;

    @Column(name = "int_time_spent_ms", nullable = false)
    private Integer intTimeSpentMs = 0;

    @Column(name = "str_user_note")
    private String strUserNote;

    @Column(name = "dt_shown", nullable = false)
    private LocalDateTime dtShown;

    @Column(name = "dt_read")
    private LocalDateTime dtRead;

    @PrePersist
    protected void onCreate() {
        dtShown = LocalDateTime.now();
    }
}
