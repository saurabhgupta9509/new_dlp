package com.ma.dlp.dto;

import java.time.LocalDateTime;

public class PhishingAlertResponse {
    private Long id;
    private String sender;
    private String receiver;
    private String subject;
    private String bodyPreview;
    private LocalDateTime timestamp;
    private String source;
    private Double mlScore;
    private Double mlConfidence;
    private String mlPrediction;
    private String[] reasons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String emailKey;
    private boolean webhookSent;
    
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
    
    public String[] getReasons() { return reasons; }
    public void setReasons(String[] reasons) { this.reasons = reasons; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getEmailKey() { return emailKey; }
    public void setEmailKey(String emailKey) { this.emailKey = emailKey; }
    
    public boolean isWebhookSent() { return webhookSent; }
    public void setWebhookSent(boolean webhookSent) { this.webhookSent = webhookSent; }
}