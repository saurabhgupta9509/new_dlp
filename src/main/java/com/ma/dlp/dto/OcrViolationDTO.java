package com.ma.dlp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OcrViolationDTO {

    @JsonProperty("agent_id")
    private Long agentId;

    private String timestamp;   // ISO 8601
    private String ruleType;
    private String matchedText;
    private float confidence;
    private float threatScore;
    private float contextConfidence;
    private String screenshotPath;

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public float getContextConfidence() {
        return contextConfidence;
    }

    public void setContextConfidence(float contextConfidence) {
        this.contextConfidence = contextConfidence;
    }

    public String getMatchedText() {
        return matchedText;
    }

    public void setMatchedText(String matchedText) {
        this.matchedText = matchedText;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
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
}
