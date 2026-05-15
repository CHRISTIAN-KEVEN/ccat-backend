package com.ccat.api.service.impl;

import com.ccat.api.dto.request.AnswerCreateRequest;
import com.ccat.api.dto.response.AnswerResponse;
import com.ccat.api.exception.QuestionNotFoundException;
import com.ccat.api.mapper.AnswerMapper;
import com.ccat.api.model.entity.Answer;
import com.ccat.api.repository.AnswerRepository;
import com.ccat.api.repository.QuestionRepository;
import com.ccat.api.service.AnswerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository   answerRepository;
    private final QuestionRepository questionRepository;
    private final AnswerMapper       answerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AnswerResponse> findByQuestion(String questionUuid) {
        var question = questionRepository.findByStrUuid(questionUuid)
                .orElseThrow(() -> new QuestionNotFoundException(questionUuid));
        return answerRepository.findByQuestionLgIdOrderByIntSortOrderAsc(question.getLgId())
                .stream().map(answerMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public AnswerResponse create(AnswerCreateRequest request) {
        var question = questionRepository.findById(request.lgQuestionId())
                .orElseThrow(() -> new QuestionNotFoundException(String.valueOf(request.lgQuestionId())));
        return answerMapper.toResponse(answerRepository.save(answerMapper.toEntity(request, question)));
    }

    @Override
    @Transactional
    public AnswerResponse update(Long answerId, AnswerCreateRequest request) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("Answer not found: " + answerId));

        answer.setStrAnswerText(request.strAnswerText());
        answer.setStrAnswerLabel(request.strAnswerLabel());
        if (request.bIsCorrect()    != null) answer.setBIsCorrect(request.bIsCorrect());
        if (request.strExplanation() != null) answer.setStrExplanation(request.strExplanation());
        if (request.intSortOrder()  != null) answer.setIntSortOrder(request.intSortOrder());

        return answerMapper.toResponse(answerRepository.save(answer));
    }

    @Override
    @Transactional
    public void delete(Long answerId) {
        if (!answerRepository.existsById(answerId))
            throw new EntityNotFoundException("Answer not found: " + answerId);
        answerRepository.deleteById(answerId);
    }
}
