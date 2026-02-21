package com.ma.dlp.RestService;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ma.dlp.Repository.AlertRepository;
import com.ma.dlp.StatDTO.AlertStatsDTO;

@Service
public class AlertStatsService {

    private final AlertRepository alertRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AlertStatsService(AlertRepository alertRepository,
                             SimpMessagingTemplate messagingTemplate) {
        this.alertRepository = alertRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public AlertStatsDTO getAlertStats() {

        long totalAlerts = alertRepository.count();

        long criticalAlerts = alertRepository.countBySeverity("CRITICAL");

        long highAlerts = alertRepository.countBySeverity("HIGH");

        long pendingAlertsCount = alertRepository.countByStatus("PENDING");

        return new AlertStatsDTO(
                totalAlerts,
                criticalAlerts,
                highAlerts,
                pendingAlertsCount
        );
    }

    // 🔥 Real-time push every 5 sec (optional)
    @Scheduled(fixedRate = 5000)
    public void pushAlertStatsUpdate() {
        AlertStatsDTO stats = getAlertStats();
        messagingTemplate.convertAndSend("/topic/alert-stats", stats);
    }
}