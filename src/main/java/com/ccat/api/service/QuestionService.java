package com.ccat.api.service;

import com.ccat.api.dto.request.QuestionCreateRequest;
import com.ccat.api.dto.request.QuestionImportRequest;
import com.ccat.api.dto.request.QuestionUpdateRequest;
import com.ccat.api.dto.response.QuestionResponse;

import java.util.List;

public interface QuestionService {

    List<QuestionResponse> findAll();

    List<QuestionResponse> findByDomain(String domainCode);

    List<QuestionResponse> findByDomainAndDifficulty(String domainCode, String difficulty);

    QuestionResponse findByUuid(String uuid);

    QuestionResponse create(QuestionCreateRequest request, String adminEmail);

    QuestionResponse update(String uuid, QuestionUpdateRequest request);

    void delete(String uuid);

    List<QuestionResponse> bulkImport(List<QuestionImportRequest> requests, String adminEmail);
}
