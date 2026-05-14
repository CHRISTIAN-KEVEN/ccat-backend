package com.ccat.api.mapper;

import com.ccat.api.dto.response.DomainPerformanceResponse;
import com.ccat.api.model.entity.DomainPerformance;
import org.springframework.stereotype.Component;

@Component
public class DomainPerformanceMapper {

    public DomainPerformanceResponse toResponse(DomainPerformance dp) {
        return new DomainPerformanceResponse(
                dp.getLgId(),
                dp.getDomain().getStrDomainCode(),
                dp.getDomain().getStrLabel(),
                dp.getIntCorrectCount(),
                dp.getIntWrongCount(),
                dp.getIntSkippedCount(),
                dp.getIntTotalCount(),
                dp.getDbAccuracyPercent(),
                dp.getDbAvgResponseMs(),
                dp.getDbAvgCorrectMs(),
                dp.getDbAvgWrongMs(),
                dp.getEmDifficultyBreakdown(),
                dp.getIntRankInSession()
        );
    }
}
