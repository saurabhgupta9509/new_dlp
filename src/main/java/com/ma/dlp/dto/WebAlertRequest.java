package com.ma.dlp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WebAlertRequest {
    private Long agentId;
    private String type; // "FILE_UPLOAD_BLOCKED", "FILE_DOWNLOAD_BLOCKED"
    private String details;
    private LocalDateTime visitTimestamp;

    // Constructors
    public WebAlertRequest() {}

    public WebAlertRequest(Long agentId, String type, String details, LocalDateTime visitTimestamp) {
        this.agentId = agentId;
        this.type = type;
        this.details = details;
        this.visitTimestamp = visitTimestamp;
    }

    // Getters and Setters
    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getVisitTimestamp() {
        return visitTimestamp;
    }

    public void setVisitTimestamp(LocalDateTime visitTimestamp) {
        this.visitTimestamp = visitTimestamp;
    }
}