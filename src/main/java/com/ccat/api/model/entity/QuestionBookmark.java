package com.ccat.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_question_bookmark", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"lg_user_id", "lg_question_id"})
})
public class QuestionBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_question_id", nullable = false)
    private Question question;

    @Column(name = "str_note", length = 500)
    private String strNote;

    @Column(name = "dt_created", nullable = false)
    private LocalDateTime dtCreated;

    @PrePersist
    protected void onCreate() {
        dtCreated = LocalDateTime.now();
    }
}
