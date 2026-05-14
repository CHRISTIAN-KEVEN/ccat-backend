package com.ccat.api.dto.response;

import com.ccat.api.model.enums.ContentType;
import com.ccat.api.model.enums.QuestionDifficulty;
import com.ccat.api.model.enums.QuestionType;
import java.util.List;

public record QuestionResponse(
        Long lgId,
        String strUuid,
        String strDomainCode,
        QuestionDifficulty emDifficulty,
        QuestionType emQuestionType,
        ContentType emContentType,
        String strQuestionText,
        String strImageUrl,
        String strImageAlt,
        String strExplanation,
        String strHint,
        Integer intPointValue,
        Integer intTimeLimitMs,
        Boolean bActive,
        Boolean bVerified,
        List<AnswerResponse> answers
) {}
