package com.ccat.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscriptionCreateRequest(
        @NotBlank String strStripeCustomerId,
        @NotBlank String strStripeSubscriptionId,
        @NotBlank String strStripePriceId,
        @NotBlank String strPlanName,
        @NotNull Double dbAmountPaid,
        String strCurrency,
        Integer intTrialDays
) {}
