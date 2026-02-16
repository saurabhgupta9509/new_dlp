package com.ma.dlp.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SecurityCertificateDTO {

    @JsonProperty("agent_id")
    private Long agentId;

    @Column(name="certificate_uuid", nullable=false, unique=true)
    private String certificateUuid;

    @JsonProperty("assessment_time")
    private String assessmentTime;

    @JsonProperty("user_device")
    private String userDevice;

    @JsonProperty("user_mac")
    private String userMac;

    @JsonProperty("threat_level")
    private String threatLevel;

    @JsonProperty("emoji")
    private String emoji;

    @JsonProperty("threat_score")
    private float threatScore;

    @JsonProperty("primary_context")
    private String primaryContext;

    @JsonProperty("total_violations")
    private int totalViolations;

    @JsonProperty("rule_breakdown")
    private Map<String, Integer> ruleBreakdown;

    @JsonProperty("risk_analysis")
    private String riskAnalysis;

    @JsonProperty("immediate_actions")
    private List<String> immediateActions;

    public String getCertificateUuid() {
        return certificateUuid;
    }

    public void setCertificateUuid(String certificateUuid) {
        this.certificateUuid = certificateUuid;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getAssessmentTime() {
        return assessmentTime;
    }

    public void setAssessmentTime(String assessmentTime) {
        this.assessmentTime = assessmentTime;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public List<String> getImmediateActions() {
        return immediateActions;
    }

    public void setImmediateActions(List<String> immediateActions) {
        this.immediateActions = immediateActions;
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

    public Map<String, Integer> getRuleBreakdown() {
        return ruleBreakdown;
    }

    public void setRuleBreakdown(Map<String, Integer> ruleBreakdown) {
        this.ruleBreakdown = ruleBreakdown;
    }

    public String getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(String threatLevel) {
        this.threatLevel = threatLevel;
    }

    public float getThreatScore() {
        return threatScore;
    }

    public void setThreatScore(float threatScore) {
        this.threatScore = threatScore;
    }

    public int getTotalViolations() {
        return totalViolations;
    }

    public void setTotalViolations(int totalViolations) {
        this.totalViolations = totalViolations;
    }

    public String getUserDevice() {
        return userDevice;
    }

    public void setUserDevice(String userDevice) {
        this.userDevice = userDevice;
    }

    public String getUserMac() {
        return userMac;
    }

    public void setUserMac(String userMac) {
        this.userMac = userMac;
    }
}
