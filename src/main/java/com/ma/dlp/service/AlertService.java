package com.ma.dlp.service;

import com.ma.dlp.dto.AlertDTO;
import com.ma.dlp.dto.AlertStatsDTO;
import com.ma.dlp.dto.AlertsByDateDTO;
import com.ma.dlp.model.Alert;
import com.ma.dlp.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AlertService {

    Alert saveAlert(Alert alert);

    List<Alert> getRecentAlerts();

    List<AlertDTO> getPendingAlerts();

    List<AlertDTO> getAllAlerts();

    List<Alert> getAgentAlerts(Long agentId);

    AlertDTO handleDecision(Long alertId, String decision);

    Alert createAlert(User agent, String alertType, String description, String deviceInfo, String fileDetails);

    List<Alert> getAlertsBySeverity(String severity);

    long getPendingAlertCount();

    // THIS METHOD for the Pie Chart
    List<AlertStatsDTO> getAlertSummaryBySeverity();

    List<AlertsByDateDTO> getAlertSummaryByDate();

    // NEW: Dashboard statistics methods
    Long getTotalAlertsCount();

    Long getAlertsCountSince(LocalDateTime since);

    Long getAlertsCountBetween(LocalDateTime start, LocalDateTime end);

    Long countAlertsByType(String alertType);

    List<Object[]> getAlertsByDay(int days);

    List<Map<String, Object>> getRecentActivity(int limit);
}