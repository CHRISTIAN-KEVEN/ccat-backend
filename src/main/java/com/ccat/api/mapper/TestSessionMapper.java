package com.ccat.api.mapper;

import com.ccat.api.dto.request.TestSessionCreateRequest;
import com.ccat.api.dto.response.TestSessionResponse;
import com.ccat.api.model.entity.TestSession;
import com.ccat.api.model.entity.User;
import com.ccat.api.model.enums.SessionStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestSessionMapper {

    public TestSessionResponse toResponse(TestSession session) {
        List<Long> questionOrder = parseJsonLongList(session.getStrQuestionOrder());
        return new TestSessionResponse(
                session.getLgId(),
                session.getStrUuid(),
                session.getUser().getLgId(),
                session.getEmSessionType(),
                session.getEmStatus(),
                session.getBIsFreeTest(),
                Boolean.TRUE.equals(session.getBAllowBacktrack()),
                session.getIntQuestionCount(),
                session.getIntDurationSeconds(),
                session.getEmDomainRatio(),
                session.getEmDifficultyMix(),
                session.getIntQuestionsAnswered(),
                session.getIntQuestionsSkipped(),
                session.getDtStarted(),
                session.getDtExpires(),
                session.getDtSubmitted(),
                questionOrder
        );
    }

    public TestSession toEntity(TestSessionCreateRequest request, User user) {
        TestSession session = new TestSession();
        session.setUser(user);
        session.setEmSessionType(request.emSessionType());
        session.setEmStatus(SessionStatus.ACTIVE);
        // Denormalized to speed up FREE_TEST_POLICY count query without joining on em_session_type
        session.setBIsFreeTest(request.emSessionType().name().equals("FREE_DIAGNOSTIC"));
        session.setBAllowBacktrack(Boolean.TRUE.equals(request.bAllowBacktrack()));
        session.setStrEligibilityVersion(request.strEligibilityVersion() != null ? request.strEligibilityVersion() : "EVER_v1");
        session.setIntQuestionCount(50);
        session.setIntDurationSeconds(900);
        session.setEmDomainRatio(request.emDomainRatio() != null ? request.emDomainRatio() : "40-40-20");
        session.setEmDifficultyMix(request.emDifficultyMix() != null ? request.emDifficultyMix() : "30-50-20");
        session.setBSubmittedByTimer(false);
        session.setIntQuestionsAnswered(0);
        session.setIntQuestionsSkipped(0);
        return session;
    }

    // Avoids pulling in Jackson at the mapper layer — format "[n,n,n]" is guaranteed by the service on write
    private List<Long> parseJsonLongList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            String cleaned = json.replaceAll("[\\[\\]\\s]", "");
            if (cleaned.isEmpty()) return Collections.emptyList();
            return Arrays.stream(cleaned.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
