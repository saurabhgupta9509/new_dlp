package com.ma.dlp.config;

import com.ma.dlp.Repository.EventLogRepository;
import com.ma.dlp.Repository.USBActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

// Add to your main configuration
@Configuration
@EnableScheduling
public class DatabaseCleanupConfig {

    @Autowired
    private EventLogRepository eventLogRepository;

    private static final Logger log = LoggerFactory.getLogger(DatabaseCleanupConfig.class); // ✅ FIXED

    @Autowired
    private USBActivityRepository usbActivityRepository;

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupOldData() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        // Cleanup event logs
        eventLogRepository.deleteOldLogs(cutoff);

        // Cleanup USB activity logs
        usbActivityRepository.deleteOldUSBActivity(cutoff);

        log.info("🧹 Cleaned up logs older than 30 days");
    }
}