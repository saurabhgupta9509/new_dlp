package com.ma.dlp.Repository;

import com.ma.dlp.model.FileEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileEventLogRepository extends JpaRepository<FileEventLog, Long> {

    List<FileEventLog> findByAgentIdOrderByTimestampDesc(Long agentId);

    List<FileEventLog> findByAgentIdAndBlockedTrueOrderByTimestampDesc(Long agentId);

    @Query("SELECT f FROM FileEventLog f WHERE f.agentId = :agentId AND f.timestamp BETWEEN :start AND :end ORDER BY f.timestamp DESC")
    List<FileEventLog> findByAgentIdAndTimestampBetween(
            @Param("agentId") Long agentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);


    @Query("SELECT COUNT(f) FROM FileEventLog f WHERE f.timestamp BETWEEN :start AND :end AND (:agentId IS NULL OR f.agentId = :agentId)")
    Long countByTimestampBetweenAndAgentId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("agentId") Long agentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FileEventLog f WHERE f.timestamp BETWEEN :startDate AND :endDate AND (:agentId IS NULL OR f.agentId = :agentId)")
    int deleteByTimestampBetweenAndAgentId(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           @Param("agentId") Long agentId);



    @Query("SELECT COUNT(f) FROM FileEventLog f WHERE f.agentId = :agentId AND f.blocked = true")
    Long countBlockedEventsByAgentId(@Param("agentId") Long agentId);

    @Query("SELECT f.operation, COUNT(f) FROM FileEventLog f WHERE f.agentId = :agentId GROUP BY f.operation")
    List<Object[]> countEventsByOperation(@Param("agentId") Long agentId);

    List<FileEventLog> findByAgentIdAndTimestampAfterOrderByTimestampDesc(Long agentId, LocalDateTime startDate);
}