package com.ccat.api.repository;

import com.ccat.api.model.entity.DomainPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DomainPerformanceRepository extends JpaRepository<DomainPerformance, Long> {
    List<DomainPerformance> findByResultLgId(Long resultId);
    List<DomainPerformance> findByUserLgIdOrderByDtCreatedDesc(Long userId);
    List<DomainPerformance> findByUserLgIdAndDomainStrDomainCode(Long userId, String domainCode);
}
