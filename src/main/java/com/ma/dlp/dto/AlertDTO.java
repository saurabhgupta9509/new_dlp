package com.ma.dlp.dto;

import com.ma.dlp.model.Alert;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class AlertDTO {
    private Long id;
    private String alertCode;
    private String alertType;
    private String description;
    private String severity;
    private String status;
    private String deviceInfo;
    private String fileDetails;
    private String actionTaken;
    private LocalDateTime createdAt;
    private String agentName; // We use a simple name, not the full User object
    private Long agentId;
    private String username;

    public String getAlertCode() {
        return alertCode;
    }

    public void setAlertCode(String alertCode) {
        this.alertCode = alertCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getFileDetails() {
        return fileDetails;
    }

    public void setFileDetails(String fileDetails) {
        this.fileDetails = fileDetails;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Helper method to convert an Alert Entity to an AlertDTO
     */
    public static AlertDTO fromEntity(Alert alert) {
        AlertDTO dto = new AlertDTO();
        dto.setId(alert.getId());
        dto.setAlertCode(alert.getAlertCode());
        dto.setAlertType(alert.getAlertType());
        dto.setDescription(alert.getDescription());
        dto.setSeverity(alert.getSeverity());
        dto.setStatus(alert.getStatus());
        dto.setDeviceInfo(alert.getDeviceInfo());
        dto.setFileDetails(alert.getFileDetails());
        dto.setActionTaken(alert.getActionTaken());
        dto.setCreatedAt(alert.getCreatedAt());



         // Get username from agent if available
        if (alert.getAgent() != null) {
            // Use hostname for agentName (computer name)
            dto.setAgentName(alert.getAgent().getHostname() != null ?
                    alert.getAgent().getHostname() :
                    alert.getAgent().getUsername()); // Fallback to username

            dto.setAgentId(alert.getAgent().getId());

            // Use username for user (logged in user)
            dto.setUsername(alert.getAgent().getUsername());
        } else {
            dto.setAgentName("Unknown Host");
            dto.setUsername("Unknown User");
        }

        return dto;
    }
}