package com.ccat.api.mapper;

import com.ccat.api.dto.request.AnswerCreateRequest;
import com.ccat.api.dto.response.AnswerResponse;
import com.ccat.api.model.entity.Answer;
import com.ccat.api.model.entity.Question;
import org.springframework.stereotype.Component;

@Component
public class AnswerMapper {

    public AnswerResponse toResponse(Answer answer) {
        return new AnswerResponse(
                answer.getLgId(),
                answer.getStrAnswerText(),
                answer.getStrAnswerLabel(),
                answer.getBIsCorrect(),
                answer.getStrExplanation(),
                answer.getIntSortOrder()
        );
    }

    public Answer toEntity(AnswerCreateRequest request, Question question) {
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setStrAnswerText(request.strAnswerText());
        answer.setStrAnswerLabel(request.strAnswerLabel());
        answer.setBIsCorrect(request.bIsCorrect() != null ? request.bIsCorrect() : false);
        answer.setStrExplanation(request.strExplanation());
        answer.setIntSortOrder(request.intSortOrder() != null ? request.intSortOrder() : 0);
        answer.setIntChosenCount(0);
        return answer;
    }
}
