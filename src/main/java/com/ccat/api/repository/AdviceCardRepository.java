package com.ccat.api.repository;

import com.ccat.api.model.entity.AdviceCard;
import com.ccat.api.model.enums.AdviceCategory;
import com.ccat.api.model.enums.AdvicePriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdviceCardRepository extends JpaRepository<AdviceCard, Long> {
    Optional<AdviceCard> findByStrCode(String strCode);
    List<AdviceCard> findByBActiveTrueOrderByEmPriorityAsc(AdvicePriority priority);
    List<AdviceCard> findByEmCategoryAndBActiveTrue(AdviceCategory category);
    List<AdviceCard> findByTargetDomainStrDomainCodeAndBActiveTrue(String domainCode);
    List<AdviceCard> findByBActiveTrue();
    boolean existsByStrCode(String strCode);
}
