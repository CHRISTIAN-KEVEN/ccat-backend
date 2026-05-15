package com.ccat.api.dto.request;

import com.ccat.api.model.enums.ContentType;
import com.ccat.api.model.enums.QuestionDifficulty;
import com.ccat.api.model.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Single question with its answers for bulk import")
public record QuestionImportRequest(

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
        String strTags,

        @NotEmpty @Valid
        List<AnswerCreateRequest> answers
) {}
