package com.ccat.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_study_drill_log", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"lg_user_id", "str_drill_key", "dt_date"})
})
public class StudyDrillLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_user_id", nullable = false)
    private User user;

    @Column(name = "str_drill_key", nullable = false, length = 80)
    private String strDrillKey;

    @Column(name = "dt_date", nullable = false)
    private LocalDate dtDate;

    @Column(name = "b_done", nullable = false)
    private Boolean bDone = false;

    @Column(name = "dt_created", nullable = false)
    private LocalDateTime dtCreated;

    @PrePersist
    protected void onCreate() {
        dtCreated = LocalDateTime.now();
    }
}
