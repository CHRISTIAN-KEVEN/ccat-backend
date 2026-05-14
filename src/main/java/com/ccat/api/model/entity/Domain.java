package com.ccat.api.model.entity;

import com.ccat.api.model.enums.DomainStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_domain")
public class Domain {

    @Id
    @Column(name = "str_domain_code", length = 50)
    private String strDomainCode;

    @Column(name = "str_label", nullable = false, length = 100)
    private String strLabel;

    @Column(name = "str_description")
    private String strDescription;

    @Column(name = "str_icon_url", length = 255)
    private String strIconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_status", nullable = false, length = 20)
    private DomainStatus emStatus = DomainStatus.ACTIVE;

    @Column(name = "int_default_ratio", nullable = false)
    private Integer intDefaultRatio = 33;

    @Column(name = "int_question_count", nullable = false)
    private Integer intQuestionCount = 0;

    @Column(name = "int_session_count", nullable = false)
    private Integer intSessionCount = 0;

    @Column(name = "db_avg_accuracy", nullable = false)
    private Double dbAvgAccuracy = 0.0;

    @Column(name = "b_show_in_results", nullable = false)
    private Boolean bShowInResults = true;

    @Column(name = "int_sort_order", nullable = false)
    private Integer intSortOrder = 0;

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
