package com.ccat.api.service.impl;

import com.ccat.api.dto.response.UserResponse;
import com.ccat.api.mapper.UserMapper;
import com.ccat.api.model.entity.User;
import com.ccat.api.model.enums.UserStatus;
import com.ccat.api.repository.UserRepository;
import com.ccat.api.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getDtCreated, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(userMapper::toResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Status management
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public UserResponse activateUser(Long userId, String adminEmail) {
        User user = getUserOrThrow(userId);
        validateAdminAction(user, adminEmail);
        user.setEmStatus(UserStatus.ACTIVE);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse suspendUser(Long userId, String adminEmail) {
        User user = getUserOrThrow(userId);
        validateAdminAction(user, adminEmail);
        user.setEmStatus(UserStatus.SUSPENDED);
        user.setStrRefreshToken(null);
        user.setDtRefreshTokenExpires(null);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse banUser(Long userId, String adminEmail) {
        User user = getUserOrThrow(userId);
        validateAdminAction(user, adminEmail);
        user.setEmStatus(UserStatus.DELETED);
        user.setStrRefreshToken(null);
        user.setDtRefreshTokenExpires(null);
        return userMapper.toResponse(userRepository.save(user));
    }

    // -------------------------------------------------------------------------

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: " + userId));
    }

    private void validateAdminAction(User targetUser, String adminEmail) {
        if (targetUser.getStrEmail().equals(adminEmail)) {
            throw new IllegalArgumentException("You cannot change the status of your own admin account");
        }
    }
}
