package com.ccat.api.repository;

import com.ccat.api.model.entity.Question;
import com.ccat.api.model.enums.QuestionDifficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findByStrUuid(String strUuid);
    List<Question> findByDomainStrDomainCodeAndBActiveTrue(String domainCode);
    List<Question> findByDomainStrDomainCodeAndEmDifficultyAndBActiveTrue(String domainCode, QuestionDifficulty difficulty);
    long countByDomainStrDomainCodeAndBActiveTrue(String domainCode);
    boolean existsByStrUuid(String strUuid);

    // RANDOM() is PostgreSQL-specific — do not use with another dialect
    @Query("SELECT q FROM Question q WHERE q.domain.strDomainCode = :domainCode AND q.bActive = true ORDER BY FUNCTION('RANDOM')")
    List<Question> findRandomByDomainCode(@Param("domainCode") String domainCode);
}
