package com.ccat.api.dto.response;

import com.ccat.api.model.enums.ConfigGroup;
import com.ccat.api.model.enums.ConfigValueType;
import java.time.LocalDateTime;

public record AppConfigResponse(
        Long lgId,
        String strKey,
        String strValue,
        ConfigValueType emValueType,
        String strDescription,
        String strDefaultValue,
        ConfigGroup emGroup,
        Boolean bIsSensitive,
        Boolean bRequiresRestart,
        LocalDateTime dtUpdated
) {}
