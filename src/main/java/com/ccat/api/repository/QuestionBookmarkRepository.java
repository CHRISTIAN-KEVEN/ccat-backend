package com.ccat.api.repository;

import com.ccat.api.model.entity.QuestionBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface QuestionBookmarkRepository extends JpaRepository<QuestionBookmark, Long> {
    Optional<QuestionBookmark> findByUserLgIdAndQuestionLgId(Long userId, Long questionId);
    boolean existsByUserLgIdAndQuestionLgId(Long userId, Long questionId);
    List<QuestionBookmark> findByUserLgIdOrderByDtCreatedDesc(Long userId);

    @Query("SELECT b.question.lgId FROM QuestionBookmark b WHERE b.user.lgId = :userId")
    Set<Long> findBookmarkedQuestionIdsByUserId(@Param("userId") Long userId);
}
