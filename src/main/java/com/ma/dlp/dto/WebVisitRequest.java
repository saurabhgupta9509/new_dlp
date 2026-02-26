package com.ma.dlp.dto;

import lombok.Data;

@Data
public class WebVisitRequest {
    private Long agentId;
    private String url;
    private String timestamp; // ISO string (RFC3339) from the agent
}

