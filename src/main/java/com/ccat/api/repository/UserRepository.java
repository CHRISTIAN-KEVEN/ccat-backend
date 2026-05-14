package com.ccat.api.repository;

import com.ccat.api.model.entity.User;
import com.ccat.api.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStrEmail(String strEmail);
    Optional<User> findByStrUuid(String strUuid);
    Optional<User> findByStrVerificationToken(String token);
    Optional<User> findByStrResetToken(String token);
    Optional<User> findByStrRefreshToken(String token);
    boolean existsByStrEmail(String strEmail);
    long countByEmStatus(UserStatus emStatus);
}
