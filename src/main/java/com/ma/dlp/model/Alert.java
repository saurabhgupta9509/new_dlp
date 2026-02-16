// Alert.java
package com.ma.dlp.model;

import lombok.Data;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

@Entity
@Table(name = "alerts")
@Data
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String alertCode;  // NEW: For "ALT-001" format

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private User agent;

    @Column(nullable = false)
    private String alertType; // USB_INSERTION, FILE_BLOCKED, etc.

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false)
    private String status; // PENDING, RESOLVED, IGNORED

    @Column(columnDefinition = "TEXT")
    private String deviceInfo;

    @Column(columnDefinition = "TEXT")
    private String fileDetails;

    private String actionTaken; // BLOCKED, ALLOWED, MONITORED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @Transient  // Not persisted, used during generation
    private static final AtomicLong sequenceCounter = new AtomicLong(1);

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "OPEN";
        }
        if (severity == null) {
            severity = "MEDIUM";
        }

        // Generate alertCode - THIS IS THE "ALT-001" FORMAT
        if (alertCode == null) {
            alertCode = generateAlertCode();
        }
    }

    private String generateAlertCode() {
        // Get next sequence number
        long sequence = sequenceCounter.getAndIncrement();

        // Format: ALT-001, ALT-002, etc.
        return String.format("ALT-%03d", sequence);
    }


    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public User getAgent() {
        return agent;
    }

    public void setAgent(User agent) {
        this.agent = agent;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getFileDetails() {
        return fileDetails;
    }

    public void setFileDetails(String fileDetails) {
        this.fileDetails = fileDetails;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAlertCode() {
        return alertCode;
    }

    public void setAlertCode(String alertCode) {
        this.alertCode = alertCode;
    }
}