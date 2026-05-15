package com.ccat.api.service.impl;

import com.ccat.api.dto.request.AnswerCreateRequest;
import com.ccat.api.dto.request.QuestionCreateRequest;
import com.ccat.api.dto.request.QuestionImportRequest;
import com.ccat.api.dto.request.QuestionUpdateRequest;
import com.ccat.api.dto.response.QuestionResponse;
import com.ccat.api.exception.DomainNotFoundException;
import com.ccat.api.exception.QuestionNotFoundException;
import com.ccat.api.mapper.AnswerMapper;
import com.ccat.api.mapper.QuestionMapper;
import com.ccat.api.model.entity.Answer;
import com.ccat.api.model.entity.Domain;
import com.ccat.api.model.entity.Question;
import com.ccat.api.model.entity.User;
import com.ccat.api.model.enums.QuestionDifficulty;
import com.ccat.api.repository.AnswerRepository;
import com.ccat.api.repository.DomainRepository;
import com.ccat.api.repository.QuestionRepository;
import com.ccat.api.repository.UserRepository;
import com.ccat.api.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository   answerRepository;
    private final DomainRepository   domainRepository;
    private final UserRepository     userRepository;
    private final QuestionMapper     questionMapper;
    private final AnswerMapper       answerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> findAll() {
        return questionRepository.findByBActiveTrueOrderByDtCreatedDesc()
                .stream().map(this::toResponseWithAnswers).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> findByDomain(String domainCode) {
        return questionRepository.findByDomainStrDomainCodeAndBActiveTrue(domainCode)
                .stream().map(this::toResponseWithAnswers).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> findByDomainAndDifficulty(String domainCode, String difficulty) {
        QuestionDifficulty level = QuestionDifficulty.valueOf(difficulty.toUpperCase());
        return questionRepository.findByDomainStrDomainCodeAndEmDifficultyAndBActiveTrue(domainCode, level)
                .stream().map(this::toResponseWithAnswers).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse findByUuid(String uuid) {
        return toResponseWithAnswers(getOrThrow(uuid));
    }

    @Override
    @Transactional
    public QuestionResponse create(QuestionCreateRequest request, String adminEmail) {
        Domain domain = domainRepository.findById(request.strDomainCode())
                .orElseThrow(() -> new DomainNotFoundException(request.strDomainCode()));
        User admin = userRepository.findByStrEmail(adminEmail).orElseThrow();

        Question question = questionMapper.toEntity(request, domain, admin);
        question.setStrUuid(UUID.randomUUID().toString());

        return toResponseWithAnswers(questionRepository.save(question));
    }

    @Override
    @Transactional
    public QuestionResponse update(String uuid, QuestionUpdateRequest request) {
        Question question = getOrThrow(uuid);

        if (request.emDifficulty()    != null) question.setEmDifficulty(request.emDifficulty());
        if (request.emQuestionType()  != null) question.setEmQuestionType(request.emQuestionType());
        if (request.emContentType()   != null) question.setEmContentType(request.emContentType());
        if (request.strQuestionText() != null) question.setStrQuestionText(request.strQuestionText());
        if (request.strImageUrl()     != null) question.setStrImageUrl(request.strImageUrl());
        if (request.strImageAlt()     != null) question.setStrImageAlt(request.strImageAlt());
        if (request.strExplanation()  != null) question.setStrExplanation(request.strExplanation());
        if (request.strHint()         != null) question.setStrHint(request.strHint());
        if (request.intPointValue()   != null) question.setIntPointValue(request.intPointValue());
        if (request.intTimeLimitMs()  != null) question.setIntTimeLimitMs(request.intTimeLimitMs());
        if (request.bActive()         != null) question.setBActive(request.bActive());
        if (request.bVerified()       != null) question.setBVerified(request.bVerified());
        if (request.strSource()       != null) question.setStrSource(request.strSource());
        if (request.strTags()         != null) question.setStrTags(request.strTags());

        return toResponseWithAnswers(questionRepository.save(question));
    }

    @Override
    @Transactional
    public void delete(String uuid) {
        Question question = getOrThrow(uuid);
        question.setBActive(false);
        questionRepository.save(question);
    }

    @Override
    @Transactional
    public List<QuestionResponse> bulkImport(List<QuestionImportRequest> requests, String adminEmail) {
        User admin = userRepository.findByStrEmail(adminEmail).orElseThrow();

        return requests.stream().map(req -> {
            Domain domain = domainRepository.findById(req.strDomainCode())
                    .orElseThrow(() -> new DomainNotFoundException(req.strDomainCode()));

            Question question = questionMapper.toEntity(toCreateRequest(req), domain, admin);
            question.setStrUuid(UUID.randomUUID().toString());
            Question saved = questionRepository.save(question);

            List<Answer> answers = req.answers().stream()
                    .map(a -> answerMapper.toEntity(a, saved))
                    .toList();
            answerRepository.saveAll(answers);

            return toResponseWithAnswers(saved);
        }).toList();
    }

    // -------------------------------------------------------------------------

    private QuestionResponse toResponseWithAnswers(Question question) {
        var answers = answerRepository
                .findByQuestionLgIdOrderByIntSortOrderAsc(question.getLgId())
                .stream().map(answerMapper::toResponse).toList();
        return questionMapper.toResponseWithAnswers(question, answers);
    }

    private Question getOrThrow(String uuid) {
        return questionRepository.findByStrUuid(uuid)
                .orElseThrow(() -> new QuestionNotFoundException(uuid));
    }

    private QuestionCreateRequest toCreateRequest(QuestionImportRequest r) {
        return new QuestionCreateRequest(
                r.strDomainCode(), r.emDifficulty(), r.emQuestionType(), r.emContentType(),
                r.strQuestionText(), r.strImageUrl(), r.strImageAlt(), r.strExplanation(),
                r.strHint(), r.intPointValue(), r.intTimeLimitMs(), r.strSource(), r.strTags()
        );
    }
}
