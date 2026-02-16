package com.ma.dlp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentHeartbeatDTO {
    private Long agentId;
    private Boolean ocrActive;
    private Boolean screenLocked;
    private LocalDateTime timestamp;

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public Boolean getOcrActive() {
        return ocrActive;
    }

    public void setOcrActive(Boolean ocrActive) {
        this.ocrActive = ocrActive;
    }
    public Boolean getScreenLocked() {
        return screenLocked;
    }

    public void setScreenLocked(Boolean screenLocked) {
        this.screenLocked = screenLocked;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
