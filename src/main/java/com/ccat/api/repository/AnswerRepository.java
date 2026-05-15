package com.ccat.api.repository;

import com.ccat.api.model.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @Query("SELECT a FROM Answer a WHERE a.question.lgId = :questionId ORDER BY a.intSortOrder ASC")
    List<Answer> findByQuestionLgIdOrderByIntSortOrderAsc(@Param("questionId") Long questionId);

    @Query("SELECT a FROM Answer a WHERE a.question.lgId = :questionId AND a.bIsCorrect = true")
    Optional<Answer> findByQuestionLgIdAndBIsCorrectTrue(@Param("questionId") Long questionId);

    @Query("SELECT COUNT(a) FROM Answer a WHERE a.question.lgId = :questionId")
    long countByQuestionLgId(@Param("questionId") Long questionId);
}
