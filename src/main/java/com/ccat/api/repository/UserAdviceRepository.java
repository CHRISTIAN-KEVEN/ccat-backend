package com.ccat.api.repository;

import com.ccat.api.model.entity.UserAdvice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAdviceRepository extends JpaRepository<UserAdvice, Long> {
    List<UserAdvice> findByUserLgIdOrderByDtShownDesc(Long userId);
    List<UserAdvice> findByResultLgIdOrderByIntDisplayRankAsc(Long resultId);
    boolean existsByUserLgIdAndAdviceCardLgIdAndResultLgId(Long userId, Long adviceCardId, Long resultId);
    long countByUserLgIdAndBWasReadTrue(Long userId);
}
