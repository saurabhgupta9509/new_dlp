package com.ma.dlp.dto;

import com.ma.dlp.model.Alert;
import com.ma.dlp.model.FileEventLog;
import com.ma.dlp.model.USBActivityLog;
import com.ma.dlp.model.WebHistoryLog;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public  class CombinedHistoryDTO {
    private String type; // "FILE", "WEB", "USB", "ALERT"
    private String eventType;
    private String description;
    private LocalDateTime timestamp;
    private Map<String, Object> details;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static CombinedHistoryDTO fromFileEvent(FileEventLog event) {
        CombinedHistoryDTO dto = new CombinedHistoryDTO();
        dto.setType("FILE");
        dto.setEventType("File Event");
        dto.setDescription(event.getOperation() + " - " + event.getFilePath());
        dto.setTimestamp(event.getTimestamp());

        Map<String, Object> details = new HashMap<>();
        details.put("operation", event.getOperation());
        details.put("filePath", event.getFilePath());
        details.put("process", event.getProcessName());
        details.put("user", event.getUserId());
        details.put("blocked", event.getBlocked());
        dto.setDetails(details);

        return dto;
    }
    public static CombinedHistoryDTO fromWebHistory(WebHistoryLog entry) {
        CombinedHistoryDTO dto = new CombinedHistoryDTO();
        dto.setType("WEB");
        dto.setEventType("Web Visit");
        dto.setDescription("Visited: " + entry.getUrl());
        dto.setTimestamp(entry.getVisitTimestamp());

        Map<String, Object> details = new HashMap<>();
        details.put("url", entry.getUrl());
        details.put("browser", entry.getBrowser());
        details.put("action", entry.getAction());
        details.put("blocked", entry.isBlocked());
        dto.setDetails(details);

        return dto;
    }
    public static CombinedHistoryDTO fromUSBActivity(USBActivityLog entry) {
        CombinedHistoryDTO dto = new CombinedHistoryDTO();
        dto.setType("USB");
        dto.setEventType("USB Device");
        dto.setDescription(entry.getAction() + " - " + entry.getDeviceName());
        dto.setTimestamp(entry.getTimestamp());

        Map<String, Object> details = new HashMap<>();
        details.put("deviceName", entry.getDeviceName());
        details.put("vendorId", entry.getVendorId());
        details.put("productId", entry.getProductId());
        details.put("serialNumber", entry.getSerialNumber());
        details.put("action", entry.getAction());
        dto.setDetails(details);

        return dto;
    }
    public static CombinedHistoryDTO fromAlert(Alert alert) {
        CombinedHistoryDTO dto = new CombinedHistoryDTO();
        dto.setType("ALERT");
        dto.setEventType("Security Alert");
        dto.setDescription(alert.getDescription());
        dto.setTimestamp(alert.getCreatedAt());

        Map<String, Object> details = new HashMap<>();
        details.put("alertType", alert.getAlertType());
        details.put("severity", alert.getSeverity());
        details.put("status", alert.getStatus());
        details.put("deviceInfo", alert.getDeviceInfo());
        dto.setDetails(details);

        return dto;
    }
}