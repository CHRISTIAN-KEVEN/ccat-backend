package com.ccat.api.mapper;

import com.ccat.api.dto.request.ResponseSubmitRequest;
import com.ccat.api.dto.response.ResponseSubmitResponse;
import com.ccat.api.model.entity.Answer;
import com.ccat.api.model.entity.Question;
import com.ccat.api.model.entity.Response;
import com.ccat.api.model.entity.TestSession;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {

    public ResponseSubmitResponse toResponse(Response response) {
        return new ResponseSubmitResponse(
                response.getLgId(),
                response.getSession().getLgId(),
                response.getQuestion().getLgId(),
                response.getAnswer() != null ? response.getAnswer().getLgId() : null,
                response.getBIsCorrect(),
                response.getBWasSkipped(),
                response.getIntResponseTimeMs(),
                response.getIntDisplayOrder(),
                response.getDtAnswered()
        );
    }

    public Response toEntity(ResponseSubmitRequest request, TestSession session, Question question, Answer answer) {
        Response response = new Response();
        response.setSession(session);
        response.setQuestion(question);
        response.setAnswer(answer);
        // bIsCorrect denormalized once at write time — single source of truth for scoring and analytics
        response.setBIsCorrect(answer != null && answer.getBIsCorrect());
        response.setBWasSkipped(answer == null);
        response.setBWasChanged(request.bWasChanged() != null ? request.bWasChanged() : false);
        response.setIntResponseTimeMs(request.intResponseTimeMs());
        response.setIntDisplayOrder(request.intDisplayOrder());
        response.setStrAnswerOptionsOrder(request.strAnswerOptionsOrder());
        response.setIntChangedCount(request.intChangedCount() != null ? request.intChangedCount() : 0);
        return response;
    }
}
