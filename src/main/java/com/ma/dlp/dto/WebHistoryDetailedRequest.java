package com.ma.dlp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WebHistoryDetailedRequest {
    private Long agentId;
    private String url;
    private String browser;
    private LocalDateTime visitTimestamp;
    private String action; // "BROWSE", "DOWNLOAD", "UPLOAD", "BLOCKED_ACCESS"
    private boolean blocked;
    private String fileInfo;

    // Constructors
    public WebHistoryDetailedRequest() {}

    public WebHistoryDetailedRequest(Long agentId, String url, String browser, LocalDateTime visitTimestamp,
                                     String action, boolean blocked, String fileInfo) {
        this.agentId = agentId;
        this.url = url;
        this.browser = browser;
        this.visitTimestamp = visitTimestamp;
        this.action = action;
        this.blocked = blocked;
        this.fileInfo = fileInfo;
    }

    // Getters and Setters
    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public LocalDateTime getVisitTimestamp() {
        return visitTimestamp;
    }

    public void setVisitTimestamp(LocalDateTime visitTimestamp) {
        this.visitTimestamp = visitTimestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(String fileInfo) {
        this.fileInfo = fileInfo;
    }
}