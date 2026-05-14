package com.ccat.api.dto.response;

import com.ccat.api.model.enums.SubscriptionStatus;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long lgId,
        Long lgUserId,
        String strStripeSubscriptionId,
        String strPlanName,
        SubscriptionStatus emStatus,
        Double dbAmountPaid,
        String strCurrency,
        Boolean bAutoRenew,
        Integer intTrialDays,
        LocalDateTime dtTrialEnd,
        LocalDateTime dtStart,
        LocalDateTime dtEnd,
        LocalDateTime dtCanceled,
        LocalDateTime dtCreated
) {}
