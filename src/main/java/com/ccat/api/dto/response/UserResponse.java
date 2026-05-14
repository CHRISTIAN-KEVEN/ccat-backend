package com.ccat.api.dto.response;

import com.ccat.api.model.enums.UserRole;
import com.ccat.api.model.enums.UserStatus;
import java.time.LocalDateTime;

public record UserResponse(
        Long lgId,
        String strUuid,
        String strEmail,
        String strFirstName,
        String strLastName,
        UserRole emRole,
        UserStatus emStatus,
        Boolean bEmailVerified,
        String strTimezone,
        String strLocale,
        Integer intLoginCount,
        LocalDateTime dtLastLogin,
        LocalDateTime dtCreated
) {}
