package com.ma.dlp.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FileEventRequest {
    private Long agentId;
    private List<FileEventDTO> events;


    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public List<FileEventDTO> getEvents() {
        return events;
    }

    public void setEvents(List<FileEventDTO> events) {
        this.events = events;
    }
}

