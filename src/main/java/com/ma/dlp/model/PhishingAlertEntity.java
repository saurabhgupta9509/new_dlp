package com.ma.dlp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "phishing_alerts")
public class PhishingAlertEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sender", nullable = false)
    private String sender;
    
    @Column(name = "receiver")
    private String receiver;
    
    @Column(name = "subject")
    private String subject;
    
    @Column(name = "body_preview", length = 1000)
    private String bodyPreview;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "source")
    private String source;
    
    @Column(name = "ml_score")
    private Double mlScore;
    
    @Column(name = "ml_confidence")
    private Double mlConfidence;
    
    @Column(name = "ml_prediction")
    private String mlPrediction;
    
    @Column(name = "reasons", length = 2000)
    private String reasons; // JSON array stored as string
    
    @Column(name = "detected_at")
    private Long detectedAt;
    
    @Column(name = "extension_version")
    private String extensionVersion;
    
    @Column(name = "email_key")
    private String emailKey; // Unique key to prevent duplicates
    
    @Column(name = "webhook_sent")
    private boolean webhookSent = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getBodyPreview() { return bodyPreview; }
    public void setBodyPreview(String bodyPreview) { this.bodyPreview = bodyPreview; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public Double getMlScore() { return mlScore; }
    public void setMlScore(Double mlScore) { this.mlScore = mlScore; }
    
    public Double getMlConfidence() { return mlConfidence; }
    public void setMlConfidence(Double mlConfidence) { this.mlConfidence = mlConfidence; }
    
    public String getMlPrediction() { return mlPrediction; }
    public void setMlPrediction(String mlPrediction) { this.mlPrediction = mlPrediction; }
    
    public String getReasons() { return reasons; }
    public void setReasons(String reasons) { this.reasons = reasons; }
    
    public Long getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Long detectedAt) { this.detectedAt = detectedAt; }
    
    public String getExtensionVersion() { return extensionVersion; }
    public void setExtensionVersion(String extensionVersion) { this.extensionVersion = extensionVersion; }
    
    public String getEmailKey() { return emailKey; }
    public void setEmailKey(String emailKey) { this.emailKey = emailKey; }
    
    public boolean isWebhookSent() { return webhookSent; }
    public void setWebhookSent(boolean webhookSent) { this.webhookSent = webhookSent; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}