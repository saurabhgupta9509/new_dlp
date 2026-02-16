package com.ma.dlp.Repository;

import com.ma.dlp.model.WebHistoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebHistoryLogRepository extends JpaRepository<WebHistoryLog, Long> {

    List<WebHistoryLog> findByAgentIdOrderByVisitTimestampDesc(Long agentId);

    @Query("SELECT w FROM WebHistoryLog w WHERE w.agentId = :agentId ORDER BY w.visitTimestamp DESC")
    List<WebHistoryLog> findByAgentIdOrderByTimestampDesc(@Param("agentId") Long agentId);

    List<WebHistoryLog> findByAgentIdAndVisitTimestampAfterOrderByVisitTimestampDesc(Long agentId, LocalDateTime startDate);

    @Query("SELECT COUNT(w) FROM WebHistoryLog w WHERE w.visitTimestamp BETWEEN :start AND :end AND (:agentId IS NULL OR w.agentId = :agentId)")
    Long countByVisitTimestampBetweenAndAgentId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("agentId") Long agentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM WebHistoryLog w WHERE w.visitTimestamp BETWEEN :start AND :end AND (:agentId IS NULL OR w.agentId = :agentId)")
    int deleteByVisitTimestampBetweenAndAgentId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("agentId") Long agentId);



}