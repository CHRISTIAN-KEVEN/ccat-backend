package com.ccat.api.repository;

import com.ccat.api.model.entity.User;
import com.ccat.api.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * Users who: are ACTIVE + email verified + have at least one session
     * + their most recent session started before the given cutoff date.
     * Used by the inactivity reminder scheduler.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.emStatus = 'ACTIVE'
          AND u.bEmailVerified = true
          AND EXISTS (SELECT s FROM TestSession s WHERE s.user = u)
          AND (SELECT MAX(s2.dtStarted) FROM TestSession s2 WHERE s2.user = u) < :cutoff
    """)
    List<User> findInactiveUsersSince(@Param("cutoff") LocalDateTime cutoff);
}
