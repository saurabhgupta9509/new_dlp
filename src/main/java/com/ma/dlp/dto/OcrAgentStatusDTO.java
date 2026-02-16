package com.ma.dlp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class OcrAgentStatusDTO {

    @JsonProperty("agent_id")
    private Long agentId;

    @JsonProperty("agent_hostname")
    private String agentHostname;

    @JsonProperty("agent_username")  // Add this field
    private String agentUsername;

    @JsonProperty("ocr_enabled")
    private Boolean ocrEnabled;

    @JsonProperty("current_threat_score")
    private Double currentThreatScore;

    @JsonProperty("threat_level")
    private String threatLevel;

    private String emoji;
    private String riskAnalysis;

    @JsonProperty("violations_last_24h")
    private Long violationsLast24h;

    @JsonProperty("last_screenshot_time")
    private LocalDateTime lastScreenshotTime;

    @JsonProperty("last_assessment_time")
    private LocalDateTime lastAssessmentTime;

    @JsonProperty("primary_context")
    private String primaryContext;

    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }

    public String getAgentHostname() {
        return agentHostname;
    }

    public void setAgentHostname(String agentHostname) {
        this.agentHostname = agentHostname;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public Double getCurrentThreatScore() {
        return currentThreatScore;
    }

    public void setCurrentThreatScore(Double currentThreatScore) {
        this.currentThreatScore = currentThreatScore;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public LocalDateTime getLastAssessmentTime() {
        return lastAssessmentTime;
    }

    public void setLastAssessmentTime(LocalDateTime lastAssessmentTime) {
        this.lastAssessmentTime = lastAssessmentTime;
    }

    public LocalDateTime getLastScreenshotTime() {
        return lastScreenshotTime;
    }

    public void setLastScreenshotTime(LocalDateTime lastScreenshotTime) {
        this.lastScreenshotTime = lastScreenshotTime;
    }

    public Boolean getOcrEnabled() {
        return ocrEnabled;
    }

    public void setOcrEnabled(Boolean ocrEnabled) {
        this.ocrEnabled = ocrEnabled;
    }

    public String getPrimaryContext() {
        return primaryContext;
    }

    public void setPrimaryContext(String primaryContext) {
        this.primaryContext = primaryContext;
    }

    public String getRiskAnalysis() {
        return riskAnalysis;
    }

    public void setRiskAnalysis(String riskAnalysis) {
        this.riskAnalysis = riskAnalysis;
    }

    public String getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(String threatLevel) {
        this.threatLevel = threatLevel;
    }

    public Long getViolationsLast24h() {
        return violationsLast24h;
    }

    public void setViolationsLast24h(Long violationsLast24h) {
        this.violationsLast24h = violationsLast24h;
    }
}