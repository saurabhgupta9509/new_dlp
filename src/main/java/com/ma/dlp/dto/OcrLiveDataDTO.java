package com.ma.dlp.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OcrLiveDataDTO {

    @JsonProperty("agent_id")
    private Long agentId;

    private String timestamp;       // ISO 8601
    private String screenshotPath;
    private String extractedText;
    private String contentType;
    private String language;
    private float readabilityScore;
    private float threatScore;
    private int violationCount;
    private String primaryContext;
    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public float getReadabilityScore() {
        return readabilityScore;
    }

    public void setReadabilityScore(float readabilityScore) {
        this.readabilityScore = readabilityScore;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public float getThreatScore() {
        return threatScore;
    }

    public void setThreatScore(float threatScore) {
        this.threatScore = threatScore;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getViolationCount() {
        return violationCount;
    }

    public void setViolationCount(int violationCount) {
        this.violationCount = violationCount;
    }
}
