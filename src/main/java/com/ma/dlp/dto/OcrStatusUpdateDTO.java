package com.ma.dlp.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OcrStatusUpdateDTO {

    @JsonProperty("agent_id")
    private Long agentId;

    @JsonProperty("ocr_enabled")
    private boolean ocrEnabled;

    private String threatArrow;      // NEW FIELD
    private String trendColor;

    @JsonProperty("last_screenshot_time")
    private String lastScreenshotTime;

    @JsonProperty("threat_score")
    private float threatScore;

    @JsonProperty("violations_last_24h")
    private int violationsLast24h;

    @JsonProperty("agent_hostname")
    private String agentHostname;


    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getLastScreenshotTime() {
        return lastScreenshotTime;
    }

    public void setLastScreenshotTime(String lastScreenshotTime) {
        this.lastScreenshotTime = lastScreenshotTime;
    }

    public boolean isOcrEnabled() {
        return ocrEnabled;
    }

    public void setOcrEnabled(boolean ocrEnabled) {
        this.ocrEnabled = ocrEnabled;
    }

    public float getThreatScore() {
        return threatScore;
    }

    public void setThreatScore(float threatScore) {
        this.threatScore = threatScore;
    }

    public int getViolationsLast24h() {
        return violationsLast24h;
    }

    public void setViolationsLast24h(int violationsLast24h) {
        this.violationsLast24h = violationsLast24h;
    }

    public boolean isOcrCapable() {
        throw new UnsupportedOperationException("Unimplemented method 'isOcrCapable'");
    }
}
