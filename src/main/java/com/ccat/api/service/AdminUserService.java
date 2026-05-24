package com.ccat.api.service;

import com.ccat.api.dto.response.UserResponse;

import java.util.List;

public interface AdminUserService {

    List<UserResponse> findAllUsers();

    UserResponse activateUser(Long userId, String adminEmail);

    UserResponse suspendUser(Long userId, String adminEmail);

    UserResponse banUser(Long userId, String adminEmail);
}
