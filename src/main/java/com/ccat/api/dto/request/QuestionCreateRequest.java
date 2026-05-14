package com.ccat.api.dto.request;

import com.ccat.api.model.enums.ContentType;
import com.ccat.api.model.enums.QuestionDifficulty;
import com.ccat.api.model.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionCreateRequest(
        @NotBlank String strDomainCode,
        @NotNull QuestionDifficulty emDifficulty,
        @NotNull QuestionType emQuestionType,
        @NotNull ContentType emContentType,
        String strQuestionText,
        String strImageUrl,
        String strImageAlt,
        String strExplanation,
        String strHint,
        Integer intPointValue,
        Integer intTimeLimitMs,
        String strSource,
        String strTags
) {}
