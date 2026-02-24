// AlertRepository.java
package com.ma.dlp.Repository;

import com.ma.dlp.dto.AlertStatsDTO;
import com.ma.dlp.model.Alert;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Date;
import java.util.Map;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    Optional<Alert> findByAlertCode(String alertCode);

    List<Alert> findByStatusOrderByCreatedAtDesc(String status);

    List<Alert> findAllByOrderByCreatedAtDesc();

    List<Alert> findByAgentIdOrderByCreatedAtDesc(Long agentId);

    List<Alert> findByCreatedAtAfter(Date date);

    List<Alert> findBySeverityAndStatus(String severity, String status);

    List<Alert> findByStatus(String status);

    // ADD THIS QUERY for the Pie Chart
    @Query("SELECT new com.ma.dlp.dto.AlertStatsDTO(a.severity, COUNT(a)) FROM Alert a GROUP BY a.severity")
    List<AlertStatsDTO> countBySeverity();

    long countBySeverity(String severity);

    long countByStatus(String status);

    // ADD THIS QUERY for the Bar Chart (MySQL compatible)
    @Query(value = "SELECT CAST(a.created_at AS DATE) as date, COUNT(a.id) as count " +
            "FROM alerts a " +
            "WHERE a.created_at >= CURDATE() - INTERVAL 7 DAY " +
            "GROUP BY CAST(a.created_at AS DATE) " +
            "ORDER BY date ASC", nativeQuery = true)
    List<Map<String, Object>> countByDateLast7Days();

    List<Alert> findByAgentIdAndCreatedAtAfterOrderByCreatedAtDesc(Long agentId, LocalDateTime startDate);

    // In AlertRepository.java - Add these methods
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.createdAt BETWEEN :start AND :end AND (:agentId IS NULL OR a.agent.id = :agentId)")
    Long countByCreatedAtBetweenAndAgentId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("agentId") Long agentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Alert a WHERE a.createdAt BETWEEN :start AND :end AND (:agentId IS NULL OR a.agent.id = :agentId)")
    int deleteByCreatedAtBetweenAndAgentId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("agentId") Long agentId);

    Long countByCreatedAtAfter(LocalDateTime date);

    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Long countByAlertTypeContaining(String alertType);

    @Query("SELECT DATE_FORMAT(a.createdAt, '%a') as day, COUNT(a) as count " +
            "FROM Alert a " +
            "WHERE a.createdAt >= :startDate " +
            "GROUP BY DATE(a.createdAt) " +
            "ORDER BY DATE(a.createdAt)")
    List<Object[]> getDailyAlertCounts(@Param("startDate") LocalDateTime startDate);

    List<Alert> findTop10ByOrderByCreatedAtDesc();

    @Query(value = "SELECT nextval('alert_sequence')", nativeQuery = true)
    Long getNextAlertSequence();

    // Or simpler: use count + 1
    @Query("SELECT COUNT(a) + 1 FROM Alert a")
    Long getNextAlertNumber();

    @Query(value = "SELECT * FROM alerts WHERE alert_type = 'OCR_HIGH_THREAT' ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<Alert> findRecentOcrHighThreatAlerts(@Param("limit") int limit);
}