package com.ma.dlp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_usage_logs")
@Data
public class AppUsageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "current_app", length = 255)
    private String currentApp;

    @Column(name = "active_usage_time")
    private Double activeUsageTime;

    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }
}

