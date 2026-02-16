package com.ma.dlp.dto;

import java.util.List;

public class PhishingAlertRequest {
    private String sender;
    private String receiver;
    private String subject;
    private String body_preview;
    private String timestamp;
    private String source;
    private Double ml_score;
    private Double ml_confidence;
    private String ml_prediction;
    private List<String> reasons;
    private Long detected_at;
    private String extension_version;
    
    // Getters and Setters
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getBody_preview() { return body_preview; }
    public void setBody_preview(String body_preview) { this.body_preview = body_preview; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public Double getMl_score() { return ml_score; }
    public void setMl_score(Double ml_score) { this.ml_score = ml_score; }
    
    public Double getMl_confidence() { return ml_confidence; }
    public void setMl_confidence(Double ml_confidence) { this.ml_confidence = ml_confidence; }
    
    public String getMl_prediction() { return ml_prediction; }
    public void setMl_prediction(String ml_prediction) { this.ml_prediction = ml_prediction; }
    
    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
    
    public Long getDetected_at() { return detected_at; }
    public void setDetected_at(Long detected_at) { this.detected_at = detected_at; }
    
    public String getExtension_version() { return extension_version; }
    public void setExtension_version(String extension_version) { this.extension_version = extension_version; }
}