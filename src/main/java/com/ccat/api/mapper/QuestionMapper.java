package com.ccat.api.mapper;

import com.ccat.api.dto.request.QuestionCreateRequest;
import com.ccat.api.dto.response.QuestionResponse;
import com.ccat.api.dto.response.AnswerResponse;
import com.ccat.api.model.entity.Domain;
import com.ccat.api.model.entity.Question;
import com.ccat.api.model.entity.User;
import com.ccat.api.model.enums.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestionMapper {

    private final AnswerMapper answerMapper;

    public QuestionResponse toResponse(Question question) {
        return toResponseWithAnswers(question, List.of());
    }

    public QuestionResponse toResponseWithAnswers(Question question, List<AnswerResponse> answers) {
        return new QuestionResponse(
                question.getLgId(),
                question.getStrUuid(),
                question.getDomain().getStrDomainCode(),
                question.getEmDifficulty(),
                question.getEmQuestionType(),
                question.getEmContentType(),
                question.getStrQuestionText(),
                question.getStrImageUrl(),
                question.getStrImageAlt(),
                question.getStrExplanation(),
                question.getStrHint(),
                question.getIntPointValue(),
                question.getIntTimeLimitMs(),
                question.getBActive(),
                question.getBVerified(),
                answers
        );
    }

    public Question toEntity(QuestionCreateRequest request, Domain domain, User createdBy) {
        Question question = new Question();
        question.setDomain(domain);
        question.setEmDifficulty(request.emDifficulty());
        question.setEmQuestionType(request.emQuestionType());
        question.setEmContentType(request.emContentType() != null ? request.emContentType() : ContentType.TEXT);
        question.setStrQuestionText(request.strQuestionText());
        question.setStrImageUrl(request.strImageUrl());
        question.setStrImageAlt(request.strImageAlt());
        question.setStrExplanation(request.strExplanation());
        question.setStrHint(request.strHint());
        question.setIntPointValue(request.intPointValue() != null ? request.intPointValue() : 1);
        question.setIntTimeLimitMs(request.intTimeLimitMs() != null ? request.intTimeLimitMs() : 18000);
        question.setStrSource(request.strSource());
        question.setStrTags(request.strTags());
        question.setBActive(true);
        question.setBVerified(false);
        question.setIntReportCount(0);
        question.setDbAvgCorrectRate(0.0);
        question.setDbAvgResponseMs(0.0);
        question.setCreatedBy(createdBy);
        return question;
    }
}
