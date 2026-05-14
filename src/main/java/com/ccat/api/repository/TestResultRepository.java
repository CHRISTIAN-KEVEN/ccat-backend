package com.ccat.api.repository;

import com.ccat.api.model.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    Optional<TestResult> findBySessionLgId(Long sessionId);
    List<TestResult> findByUserLgIdOrderByDtCalculatedDesc(Long userId);
    Optional<TestResult> findTopByUserLgIdOrderByDtCalculatedDesc(Long userId);
    boolean existsBySessionLgId(Long sessionId);
}
