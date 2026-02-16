package com.ma.dlp.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocr_live_data")
@Data
public class OcrLiveData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long agentId;

    private String screenshotPath;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String extractedText;

    private String contentType;

    private String language;

    private Float readabilityScore;

    private Float threatScore;

    private Integer violationCount;

    private String primaryContext;

    private boolean active;

    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_user_id")
    private User agent;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public User getAgent() {
        return agent;
    }

    public void setAgent(User agent) {
        this.agent = agent;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPrimaryContext() {
        return primaryContext;
    }

    public void setPrimaryContext(String primaryContext) {
        this.primaryContext = primaryContext;
    }

    public Float getReadabilityScore() {
        return readabilityScore;
    }

    public void setReadabilityScore(Float readabilityScore) {
        this.readabilityScore = readabilityScore;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public Float getThreatScore() {
        return threatScore;
    }

    public void setThreatScore(Float threatScore) {
        this.threatScore = threatScore;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getViolationCount() {
        return violationCount;
    }

    public void setViolationCount(Integer violationCount) {
        this.violationCount = violationCount;
    }
}

