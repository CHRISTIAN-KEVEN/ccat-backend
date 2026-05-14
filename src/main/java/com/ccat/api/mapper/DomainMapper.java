package com.ccat.api.mapper;

import com.ccat.api.dto.request.DomainCreateRequest;
import com.ccat.api.dto.response.DomainResponse;
import com.ccat.api.model.entity.Domain;
import com.ccat.api.model.enums.DomainStatus;
import org.springframework.stereotype.Component;

@Component
public class DomainMapper {

    public DomainResponse toResponse(Domain domain) {
        return new DomainResponse(
                domain.getStrDomainCode(),
                domain.getStrLabel(),
                domain.getStrDescription(),
                domain.getStrIconUrl(),
                domain.getEmStatus(),
                domain.getIntDefaultRatio(),
                domain.getIntQuestionCount(),
                domain.getIntSessionCount(),
                domain.getDbAvgAccuracy(),
                domain.getBShowInResults(),
                domain.getIntSortOrder(),
                domain.getDtCreated(),
                domain.getDtUpdated()
        );
    }

    public Domain toEntity(DomainCreateRequest request) {
        Domain domain = new Domain();
        domain.setStrDomainCode(request.strDomainCode());
        domain.setStrLabel(request.strLabel());
        domain.setStrDescription(request.strDescription());
        domain.setStrIconUrl(request.strIconUrl());
        domain.setEmStatus(DomainStatus.ACTIVE);
        domain.setIntDefaultRatio(request.intDefaultRatio() != null ? request.intDefaultRatio() : 33);
        domain.setIntSortOrder(request.intSortOrder() != null ? request.intSortOrder() : 0);
        domain.setIntQuestionCount(0);
        domain.setIntSessionCount(0);
        domain.setDbAvgAccuracy(0.0);
        domain.setBShowInResults(true);
        return domain;
    }
}
