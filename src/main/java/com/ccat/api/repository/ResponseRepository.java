package com.ccat.api.repository;

import com.ccat.api.model.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {
    List<Response> findBySessionLgId(Long sessionId);
    Optional<Response> findBySessionLgIdAndQuestionLgId(Long sessionId, Long questionId);
    long countBySessionLgIdAndBIsCorrectTrue(Long sessionId);
    long countBySessionLgIdAndBWasSkippedTrue(Long sessionId);

    // Used by the analytics engine to compute per-domain breakdown after session submission
    @Query("SELECT r FROM Response r WHERE r.session.lgId = :sessionId AND r.question.domain.strDomainCode = :domainCode")
    List<Response> findBySessionIdAndDomainCode(@Param("sessionId") Long sessionId, @Param("domainCode") String domainCode);
}
