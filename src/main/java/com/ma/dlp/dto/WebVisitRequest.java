package com.ma.dlp.dto;

import lombok.Data;

@Data
public class WebVisitRequest {
    private Long agentId;
    private String url;
    private String timestamp; // ISO string (RFC3339) from the agent

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
}

