package com.ma.dlp.dto;

import lombok.Data;

@Data
public class WebVisitRequest {
    private Long agentId;
    private String url;
    private String timestamp; // ISO string (RFC3339) from the agent
    private boolean blocked;   // Add this field to indicate if the URL was blocked
    private String browser;

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }
}