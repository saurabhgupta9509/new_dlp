package com.ma.dlp.Repository;

import com.ma.dlp.model.USBActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface USBActivityRepository extends JpaRepository<USBActivityLog, Long> {
    List<USBActivityLog> findByAgentIdOrderByTimestampDesc(Long agentId);
    List<USBActivityLog> findByAgentIdAndTimestampAfterOrderByTimestampDesc(Long agentId, LocalDateTime startDate);

    @Modifying
    @Query("DELETE FROM USBActivityLog u WHERE u.timestamp < :cutoffDate")
    void deleteOldUSBActivity(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(u) FROM USBActivityLog u WHERE u.timestamp BETWEEN :start AND :end AND (:agentId IS NULL OR u.agentId = :agentId)")
    Long countByTimestampBetweenAndAgentId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("agentId") Long agentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM USBActivityLog u WHERE u.timestamp BETWEEN :start AND :end AND (:agentId IS NULL OR u.agentId = :agentId)")
    int deleteByTimestampBetweenAndAgentId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("agentId") Long agentId);

}