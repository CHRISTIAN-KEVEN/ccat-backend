package com.ccat.api.mapper;

import com.ccat.api.dto.response.UserAdviceResponse;
import com.ccat.api.model.entity.UserAdvice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAdviceMapper {

    private final AdviceCardMapper adviceCardMapper;

    public UserAdviceResponse toResponse(UserAdvice userAdvice) {
        return new UserAdviceResponse(
                userAdvice.getLgId(),
                userAdvice.getUser().getLgId(),
                userAdvice.getResult().getLgId(),
                adviceCardMapper.toResponse(userAdvice.getAdviceCard()),
                userAdvice.getEmTriggerMatched(),
                userAdvice.getIntDisplayRank(),
                userAdvice.getBWasRead(),
                userAdvice.getBWasHelpful(),
                userAdvice.getIntTimeSpentMs(),
                userAdvice.getStrUserNote(),
                userAdvice.getDtShown(),
                userAdvice.getDtRead()
        );
    }
}
