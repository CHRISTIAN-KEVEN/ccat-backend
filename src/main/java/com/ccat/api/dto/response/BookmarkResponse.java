package com.ccat.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record BookmarkResponse(
    Long lgId,
    Long lgQuestionId,
    String strQuestionText,
    String strImageUrl,
    String strDomainCode,
    String emDifficulty,
    String emQuestionType,
    String strExplanation,
    String strNote,
    boolean bBookmarked,
    LocalDateTime dtCreated,
    List<AnswerResponse> answers
) {}
