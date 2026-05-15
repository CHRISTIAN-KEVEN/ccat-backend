package com.ccat.api.service;

import com.ccat.api.dto.request.AnswerCreateRequest;
import com.ccat.api.dto.response.AnswerResponse;

import java.util.List;

public interface AnswerService {

    List<AnswerResponse> findByQuestion(String questionUuid);

    AnswerResponse create(AnswerCreateRequest request);

    AnswerResponse update(Long answerId, AnswerCreateRequest request);

    void delete(Long answerId);
}
