package com.ccat.api.dto.response;

import com.ccat.api.model.enums.ContentType;
import com.ccat.api.model.enums.QuestionDifficulty;
import com.ccat.api.model.enums.QuestionType;

import java.util.List;

public record QuestionReviewResponse(
        Integer intDisplayOrder,
        Long lgQuestionId,
        String strQuestionText,
        String strImageUrl,
        String strImageAlt,
        ContentType emContentType,
        QuestionDifficulty emDifficulty,
        QuestionType emQuestionType,
        String strDomainCode,
        String strExplanation,
        String strHint,
        List<AnswerResponse> answers,
        Long lgChosenAnswerId,
        String strChosenLabel,
        Boolean bIsCorrect,
        Boolean bWasSkipped,
        Boolean bWasChanged,
        Integer intChangedCount,
        Integer intResponseTimeMs
) {}
