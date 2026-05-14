package com.ccat.api.repository;

import com.ccat.api.model.entity.TestSession;
import com.ccat.api.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestSessionRepository extends JpaRepository<TestSession, Long> {
    Optional<TestSession> findByStrUuid(String strUuid);
    List<TestSession> findByUserLgIdOrderByDtStartedDesc(Long userId);
    Optional<TestSession> findTopByUserLgIdAndEmStatusOrderByDtStartedDesc(Long userId, SessionStatus status);
    // Used by EVER policy: COUNT > 0 permanently blocks any new free test
    long countByUserLgIdAndBIsFreeTestTrue(Long userId);
    boolean existsByStrUuid(String strUuid);
    List<TestSession> findByEmStatus(SessionStatus status);
}
