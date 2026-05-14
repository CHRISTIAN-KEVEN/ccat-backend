package com.ccat.api.model.entity;

import com.ccat.api.model.enums.AdviceCategory;
import com.ccat.api.model.enums.AdvicePriority;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_advice_card")
public class AdviceCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @Column(name = "str_code", unique = true, nullable = false, length = 50)
    private String strCode;

    @Column(name = "em_trigger_rule", nullable = false, length = 100)
    private String emTriggerRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_category", nullable = false, length = 50)
    private AdviceCategory emCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "str_target_domain_code")
    private Domain targetDomain;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_priority", nullable = false, length = 20)
    private AdvicePriority emPriority = AdvicePriority.MEDIUM;

    @Column(name = "str_title", nullable = false, length = 200)
    private String strTitle;

    @Column(name = "str_content", nullable = false)
    private String strContent;

    @Column(name = "str_action_label", length = 100)
    private String strActionLabel;

    @Column(name = "str_action_url", length = 255)
    private String strActionUrl;

    @Column(name = "b_active", nullable = false)
    private Boolean bActive = true;

    @Column(name = "int_display_count", nullable = false)
    private Integer intDisplayCount = 0;

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
