package com.ccat.api.model.entity;

import com.ccat.api.model.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_subscription")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lg_id")
    private Long lgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lg_user_id", nullable = false)
    private User user;

    @Column(name = "str_stripe_customer_id", nullable = false, length = 255)
    private String strStripeCustomerId;

    @Column(name = "str_stripe_subscription_id", unique = true, nullable = false, length = 255)
    private String strStripeSubscriptionId;

    @Column(name = "str_stripe_price_id", nullable = false, length = 255)
    private String strStripePriceId;

    @Column(name = "str_plan_name", nullable = false, length = 100)
    private String strPlanName;

    @Enumerated(EnumType.STRING)
    @Column(name = "em_status", nullable = false, length = 30)
    private SubscriptionStatus emStatus;

    @Column(name = "db_amount_paid", nullable = false)
    private Double dbAmountPaid;

    @Column(name = "str_currency", nullable = false, length = 10)
    private String strCurrency = "usd";

    @Column(name = "b_auto_renew", nullable = false)
    private Boolean bAutoRenew = true;

    @Column(name = "int_trial_days", nullable = false)
    private Integer intTrialDays = 0;

    @Column(name = "dt_trial_end")
    private LocalDateTime dtTrialEnd;

    @Column(name = "dt_start", nullable = false)
    private LocalDateTime dtStart;

    @Column(name = "dt_end", nullable = false)
    private LocalDateTime dtEnd;

    @Column(name = "dt_canceled")
    private LocalDateTime dtCanceled;

    @Column(name = "str_cancel_reason")
    private String strCancelReason;

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
