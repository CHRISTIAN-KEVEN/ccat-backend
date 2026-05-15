package com.ccat.api.service.impl;

import com.ccat.api.dto.request.DomainCreateRequest;
import com.ccat.api.dto.request.DomainUpdateRequest;
import com.ccat.api.dto.response.DomainResponse;
import com.ccat.api.exception.DomainNotFoundException;
import com.ccat.api.mapper.DomainMapper;
import com.ccat.api.model.entity.Domain;
import com.ccat.api.model.enums.DomainStatus;
import com.ccat.api.repository.DomainRepository;
import com.ccat.api.repository.UserRepository;
import com.ccat.api.service.DomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomainServiceImpl implements DomainService {

    private final DomainRepository domainRepository;
    private final UserRepository   userRepository;
    private final DomainMapper     domainMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DomainResponse> findAll() {
        return domainRepository.findAllByOrderByIntSortOrderAsc()
                .stream().map(domainMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainResponse> findAllActive() {
        return domainRepository.findByEmStatusOrderByIntSortOrderAsc(DomainStatus.ACTIVE)
                .stream().map(domainMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DomainResponse findByCode(String domainCode) {
        return domainMapper.toResponse(getOrThrow(domainCode));
    }

    @Override
    @Transactional
    public DomainResponse create(DomainCreateRequest request, String adminEmail) {
        Domain domain = domainMapper.toEntity(request);
        userRepository.findByStrEmail(adminEmail).ifPresent(domain::setCreatedBy);
        return domainMapper.toResponse(domainRepository.save(domain));
    }

    @Override
    @Transactional
    public DomainResponse update(String domainCode, DomainUpdateRequest request) {
        Domain domain = getOrThrow(domainCode);

        if (request.strLabel()        != null) domain.setStrLabel(request.strLabel());
        if (request.strDescription()  != null) domain.setStrDescription(request.strDescription());
        if (request.strIconUrl()      != null) domain.setStrIconUrl(request.strIconUrl());
        if (request.emStatus()        != null) domain.setEmStatus(request.emStatus());
        if (request.intDefaultRatio() != null) domain.setIntDefaultRatio(request.intDefaultRatio());
        if (request.intSortOrder()    != null) domain.setIntSortOrder(request.intSortOrder());
        if (request.bShowInResults()  != null) domain.setBShowInResults(request.bShowInResults());

        return domainMapper.toResponse(domainRepository.save(domain));
    }

    @Override
    @Transactional
    public void delete(String domainCode) {
        Domain domain = getOrThrow(domainCode);
        domain.setEmStatus(DomainStatus.INACTIVE);
        domainRepository.save(domain);
    }

    // -------------------------------------------------------------------------

    private Domain getOrThrow(String domainCode) {
        return domainRepository.findById(domainCode)
                .orElseThrow(() -> new DomainNotFoundException(domainCode));
    }
}
