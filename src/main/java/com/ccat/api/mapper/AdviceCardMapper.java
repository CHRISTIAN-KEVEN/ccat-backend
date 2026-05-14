package com.ccat.api.mapper;

import com.ccat.api.dto.request.AdviceCardCreateRequest;
import com.ccat.api.dto.response.AdviceCardResponse;
import com.ccat.api.model.entity.AdviceCard;
import com.ccat.api.model.entity.Domain;
import com.ccat.api.model.enums.AdvicePriority;
import org.springframework.stereotype.Component;

@Component
public class AdviceCardMapper {

    public AdviceCardResponse toResponse(AdviceCard card) {
        return new AdviceCardResponse(
                card.getLgId(),
                card.getStrCode(),
                card.getEmTriggerRule(),
                card.getEmCategory(),
                card.getTargetDomain() != null ? card.getTargetDomain().getStrDomainCode() : null,
                card.getEmPriority(),
                card.getStrTitle(),
                card.getStrContent(),
                card.getStrActionLabel(),
                card.getStrActionUrl(),
                card.getBActive()
        );
    }

    public AdviceCard toEntity(AdviceCardCreateRequest request, Domain targetDomain) {
        AdviceCard card = new AdviceCard();
        card.setStrCode(request.strCode());
        card.setEmTriggerRule(request.emTriggerRule());
        card.setEmCategory(request.emCategory());
        card.setTargetDomain(targetDomain);
        card.setEmPriority(request.emPriority() != null ? request.emPriority() : AdvicePriority.MEDIUM);
        card.setStrTitle(request.strTitle());
        card.setStrContent(request.strContent());
        card.setStrActionLabel(request.strActionLabel());
        card.setStrActionUrl(request.strActionUrl());
        card.setBActive(true);
        card.setIntDisplayCount(0);
        return card;
    }
}
