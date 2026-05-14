package com.ccat.api.model.entity;

import com.ccat.api.model.enums.ConfigGroup;
import com.ccat.api.model.enums.ConfigValueType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_app_config")
public class AppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @Column(name = "str_key", unique = true, nullable = false, length = 100)
    private String strKey;

    @Column(name = "str_value", nullable = false)
    private String strValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_value_type", nullable = false, length = 20)
    private ConfigValueType emValueType;

    @Column(name = "str_description")
    private String strDescription;

    @Column(name = "str_default_value")
    private String strDefaultValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_group", nullable = false, length = 50)
    private ConfigGroup emGroup;

    @Column(name = "b_is_sensitive", nullable = false)
    private Boolean bIsSensitive = false;

    @Column(name = "b_requires_restart", nullable = false)
    private Boolean bRequiresRestart = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_updated_by")
    private User updatedBy;

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
