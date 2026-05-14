package com.ccat.api.dto.request;

import com.ccat.api.model.enums.ConfigGroup;
import com.ccat.api.model.enums.ConfigValueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppConfigUpdateRequest(
        @NotBlank String strKey,
        @NotBlank String strValue,
        @NotNull ConfigValueType emValueType,
        @NotNull ConfigGroup emGroup,
        String strDescription,
        String strDefaultValue,
        Boolean bIsSensitive,
        Boolean bRequiresRestart
) {}
