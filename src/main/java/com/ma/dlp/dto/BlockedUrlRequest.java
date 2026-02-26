package com.ma.dlp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BlockedUrlRequest {
    private String urlPattern;
    private String domain;
    private String reason;
    private String category;
    private boolean active = true;
    private String deviceId;
    private String userId;
    private Long agentId; // Add this for single agent assignment
    private LocalDateTime createdAt; // Optional - will be set by server if not provided
    private LocalDateTime updatedAt; // Optional - will be set by server if not provided
}