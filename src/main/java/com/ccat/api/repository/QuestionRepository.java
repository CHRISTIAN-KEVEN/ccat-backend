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

    @Query("SELECT q FROM Question q WHERE q.strUuid = :uuid")
    Optional<Question> findByStrUuid(@Param("uuid") String uuid);

    @Query("SELECT q FROM Question q WHERE q.bActive = true ORDER BY q.dtCreated DESC")
    List<Question> findByBActiveTrueOrderByDtCreatedDesc();

    @Query("SELECT q FROM Question q WHERE q.domain.strDomainCode = :domainCode AND q.bActive = true")
    List<Question> findByDomainStrDomainCodeAndBActiveTrue(@Param("domainCode") String domainCode);

    @Query("SELECT q FROM Question q WHERE q.domain.strDomainCode = :domainCode AND q.emDifficulty = :difficulty AND q.bActive = true")
    List<Question> findByDomainStrDomainCodeAndEmDifficultyAndBActiveTrue(
            @Param("domainCode") String domainCode,
            @Param("difficulty") QuestionDifficulty difficulty);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.domain.strDomainCode = :domainCode AND q.bActive = true")
    long countByDomainStrDomainCodeAndBActiveTrue(@Param("domainCode") String domainCode);

    @Query("SELECT CASE WHEN COUNT(q) > 0 THEN true ELSE false END FROM Question q WHERE q.strUuid = :uuid")
    boolean existsByStrUuid(@Param("uuid") String uuid);

    // Native query — RAND() is MySQL specific
    @Query(value = "SELECT * FROM t_question WHERE str_domain_code = :domainCode AND b_active = true ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomByDomainCode(@Param("domainCode") String domainCode, @Param("limit") int limit);

    @Query(value = "SELECT * FROM t_question WHERE b_active = true ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandom(@Param("limit") int limit);
}
