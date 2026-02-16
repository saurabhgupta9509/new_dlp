package com.ma.dlp.dto;


import lombok.Data;

@Data
public class EventLogRequest {
    private String eventType; // POLICY_ASSIGNMENT, POLICY_REMOVAL, FILE_EVENT, WEB_VISIT, USB_EVENT, BLOCKLIST_UPDATE
    private Long agentId;
    private String details; // JSON string with event-specific details


    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
