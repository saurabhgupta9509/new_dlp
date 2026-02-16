package com.ma.dlp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OcrDashboardStatsDTO {

    @JsonProperty("agent_id")
    private Long agentId;

    @JsonProperty("agent_hostname")
    private String agentHostname;

    @JsonProperty("agent_username")  // Add this field
    private String agentUsername;

    @JsonProperty("ocr_enabled")
    private boolean ocrEnabled;

    private String threatArrow;      // NEW FIELD
    private String trendColor;

    @JsonProperty("current_threat_score")
    private float currentThreatScore;

    @JsonProperty("violations_last_24h")
    private int violationsLast24h;

    @JsonProperty("last_screenshot_time")
    private LocalDateTime lastScreenshotTime;

    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }

    public String getThreatArrow() {
        return threatArrow;
    }

    public void setThreatArrow(String threatArrow) {
        this.threatArrow = threatArrow;
    }

    public String getTrendColor() {
        return trendColor;
    }

    public void setTrendColor(String trendColor) {
        this.trendColor = trendColor;
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

    public float getCurrentThreatScore() {
        return currentThreatScore;
    }

    public void setCurrentThreatScore(float currentThreatScore) {
        this.currentThreatScore = currentThreatScore;
    }

    public LocalDateTime getLastScreenshotTime() {
        return lastScreenshotTime;
    }

    public void setLastScreenshotTime(LocalDateTime lastScreenshotTime) {
        this.lastScreenshotTime = lastScreenshotTime;
    }

//    public boolean isOcrCapable() {
//        return ocrCapable;
//    }
//
//    public void setOcrCapable(boolean ocrCapable) {
//        this.ocrCapable = ocrCapable;
//    }

    public boolean isOcrEnabled() {
        return ocrEnabled;
    }

    public void setOcrEnabled(boolean ocrEnabled) {
        this.ocrEnabled = ocrEnabled;
    }

    public int getViolationsLast24h() {
        return violationsLast24h;
    }

    public void setViolationsLast24h(int violationsLast24h) {
        this.violationsLast24h = violationsLast24h;
    }
}
