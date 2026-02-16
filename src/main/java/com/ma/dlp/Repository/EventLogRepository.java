package com.ma.dlp.Repository;

import com.ma.dlp.model.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    // Automatic cleanup of logs older than 30 days
    @Modifying
    @Query("DELETE FROM EventLog e WHERE e.timestamp < :cutoffDate")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    default void cleanupOldLogs() {
        deleteOldLogs(LocalDateTime.now().minusDays(30));
    }

}
