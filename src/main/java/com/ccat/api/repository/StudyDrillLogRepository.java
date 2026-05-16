package com.ccat.api.repository;

import com.ccat.api.model.entity.StudyDrillLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyDrillLogRepository extends JpaRepository<StudyDrillLog, Long> {
    List<StudyDrillLog> findByUserLgIdAndDtDate(Long userId, LocalDate date);
    List<StudyDrillLog> findByUserLgIdAndDtDateBetween(Long userId, LocalDate from, LocalDate to);
    Optional<StudyDrillLog> findByUserLgIdAndStrDrillKeyAndDtDate(Long userId, String drillKey, LocalDate date);

    @Query("SELECT COUNT(s) FROM StudyDrillLog s WHERE s.user.lgId = :userId AND s.dtDate = :date AND s.bDone = true")
    long countDoneByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(s) FROM StudyDrillLog s WHERE s.user.lgId = :userId AND s.dtDate BETWEEN :from AND :to AND s.bDone = true")
    long countDoneByUserAndDateRange(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
