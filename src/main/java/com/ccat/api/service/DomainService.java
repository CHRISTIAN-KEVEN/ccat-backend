package com.ccat.api.service;

import com.ccat.api.dto.request.DomainCreateRequest;
import com.ccat.api.dto.request.DomainUpdateRequest;
import com.ccat.api.dto.response.DomainResponse;

import java.util.List;

public interface DomainService {

    List<DomainResponse> findAll();

    List<DomainResponse> findAllActive();

    DomainResponse findByCode(String domainCode);

    DomainResponse create(DomainCreateRequest request, String adminEmail);

    DomainResponse update(String domainCode, DomainUpdateRequest request);

    void delete(String domainCode);
}
