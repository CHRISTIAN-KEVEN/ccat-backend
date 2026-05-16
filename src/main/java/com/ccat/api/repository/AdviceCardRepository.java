package com.ccat.api.repository;

import com.ccat.api.model.entity.AdviceCard;
import com.ccat.api.model.enums.AdviceCategory;
import com.ccat.api.model.enums.AdvicePriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdviceCardRepository extends JpaRepository<AdviceCard, Long> {
    Optional<AdviceCard> findByStrCode(String strCode);
    boolean existsByStrCode(String strCode);

    @Query("SELECT a FROM AdviceCard a WHERE a.bActive = true ORDER BY a.emPriority ASC")
    List<AdviceCard> findActiveOrderByPriority();

    @Query("SELECT a FROM AdviceCard a WHERE a.emCategory = :category AND a.bActive = true")
    List<AdviceCard> findByEmCategoryAndActive(@Param("category") AdviceCategory category);

    @Query("SELECT a FROM AdviceCard a WHERE a.targetDomain.strDomainCode = :domainCode AND a.bActive = true")
    List<AdviceCard> findByDomainCodeAndActive(@Param("domainCode") String domainCode);

    @Query("SELECT a FROM AdviceCard a WHERE a.bActive = true")
    List<AdviceCard> findAllActive();
}
