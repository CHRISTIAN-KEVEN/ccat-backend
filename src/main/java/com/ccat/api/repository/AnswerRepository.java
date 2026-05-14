package com.ccat.api.repository;

import com.ccat.api.model.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionLgIdOrderByIntSortOrderAsc(Long questionId);
    Optional<Answer> findByQuestionLgIdAndBIsCorrectTrue(Long questionId);
    long countByQuestionLgId(Long questionId);
}
