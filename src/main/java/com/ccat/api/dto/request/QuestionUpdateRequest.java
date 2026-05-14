package com.ccat.api.dto.request;

import com.ccat.api.model.enums.ContentType;
import com.ccat.api.model.enums.QuestionDifficulty;
import com.ccat.api.model.enums.QuestionType;

public record QuestionUpdateRequest(
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
        String strSource,
        String strTags
) {}
