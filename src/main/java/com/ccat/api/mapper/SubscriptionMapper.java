package com.ccat.api.mapper;

import com.ccat.api.dto.request.SubscriptionCreateRequest;
import com.ccat.api.dto.response.SubscriptionResponse;
import com.ccat.api.model.entity.Subscription;
import com.ccat.api.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public SubscriptionResponse toResponse(Subscription sub) {
        return new SubscriptionResponse(
                sub.getLgId(),
                sub.getUser().getLgId(),
                sub.getStrStripeSubscriptionId(),
                sub.getStrPlanName(),
                sub.getEmStatus(),
                sub.getDbAmountPaid(),
                sub.getStrCurrency(),
                sub.getBAutoRenew(),
                sub.getIntTrialDays(),
                sub.getDtTrialEnd(),
                sub.getDtStart(),
                sub.getDtEnd(),
                sub.getDtCanceled(),
                sub.getDtCreated()
        );
    }

    public Subscription toEntity(SubscriptionCreateRequest request, User user) {
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setStrStripeCustomerId(request.strStripeCustomerId());
        sub.setStrStripeSubscriptionId(request.strStripeSubscriptionId());
        sub.setStrStripePriceId(request.strStripePriceId());
        sub.setStrPlanName(request.strPlanName());
        sub.setDbAmountPaid(request.dbAmountPaid());
        sub.setStrCurrency(request.strCurrency() != null ? request.strCurrency() : "usd");
        sub.setIntTrialDays(request.intTrialDays() != null ? request.intTrialDays() : 0);
        sub.setBAutoRenew(true);
        return sub;
    }
}
