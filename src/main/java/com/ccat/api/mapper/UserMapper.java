package com.ccat.api.mapper;

import com.ccat.api.dto.response.UserResponse;
import com.ccat.api.model.entity.User;
import com.ccat.api.model.enums.UserRole;
import com.ccat.api.model.enums.UserStatus;
import com.ccat.api.dto.request.UserRegisterRequest;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getLgId(),
                user.getStrUuid(),
                user.getStrEmail(),
                user.getStrFirstName(),
                user.getStrLastName(),
                user.getStrProfileImageUrl(),
                user.getEmRole(),
                user.getEmStatus(),
                user.getBEmailVerified(),
                user.getStrTimezone(),
                user.getStrLocale(),
                user.getIntLoginCount(),
                user.getDtLastLogin(),
                user.getDtCreated()
        );
    }

    public User toEntity(UserRegisterRequest request) {
        User user = new User();
        user.setStrEmail(request.strEmail());
        user.setStrFirstName(request.strFirstName());
        user.setStrLastName(request.strLastName());
        user.setStrLocale(request.strLocale() != null ? request.strLocale() : "en");
        user.setEmRole(UserRole.USER);
        user.setEmStatus(UserStatus.ACTIVE);
        user.setBEmailVerified(false);
        user.setIntLoginCount(0);
        return user;
    }
}
