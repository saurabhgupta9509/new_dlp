package com.ma.dlp.dto;

import lombok.Data;
import java.util.List;

@Data
public class WebHistoryRequest {
    private Long agentId;
    private List<WebLogDto> logs;

    // Constructors
    public WebHistoryRequest() {}

    public WebHistoryRequest(Long agentId, List<WebLogDto> logs) {
        this.agentId = agentId;
        this.logs = logs;
    }

    // Getters and Setters
    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public List<WebLogDto> getLogs() {
        return logs;
    }

    public void setLogs(List<WebLogDto> logs) {
        this.logs = logs;
    }
}