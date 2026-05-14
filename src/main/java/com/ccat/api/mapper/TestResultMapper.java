package com.ccat.api.mapper;

import com.ccat.api.dto.response.DomainPerformanceResponse;
import com.ccat.api.dto.response.TestResultResponse;
import com.ccat.api.model.entity.TestResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestResultMapper {

    public TestResultResponse toResponse(TestResult result, List<DomainPerformanceResponse> domainPerformances) {
        return new TestResultResponse(
                result.getLgId(),
                result.getSession().getLgId(),
                result.getUser().getLgId(),
                result.getIntTotalScore(),
                result.getIntQuestionCount(),
                result.getIntAnsweredCount(),
                result.getIntSkippedCount(),
                result.getDbAccuracyPercent(),
                result.getDbCompletionPercent(),
                result.getIntTimeUsedMs(),
                result.getIntTimeAvailableMs(),
                result.getDbAvgTimePerQMs(),
                result.getDbAvgTimeCorrectMs(),
                result.getDbAvgTimeWrongMs(),
                result.getEmWeakestDomain(),
                result.getEmStrongestDomain(),
                result.getEmPaceRating(),
                result.getIntPercentileEstimate(),
                result.getBPassedThreshold(),
                result.getStrAdviceTriggerCodes(),
                result.getDtCalculated(),
                domainPerformances
        );
    }

    public TestResultResponse toResponse(TestResult result) {
        return toResponse(result, List.of());
    }
}
