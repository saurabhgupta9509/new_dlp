package com.ma.dlp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "ocr_security_certificate")
@Data
public class OcrSecurityCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="agent_id")
    private Long agentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_user_id")
    private User agent;

    @Column(name="certificate_uuid", nullable=false, unique=true)
    private String certificateUuid;

    // Device
    @Column(name="user_device")
    private String userDevice;

    private String userMac;

    // Assessment Meta
    @Column(name="assessment_time")
    private String assessmentTime;
    private String assessmentMethod;
    private boolean llmAvailable;

    // Threat
    @Column(name="threat_level")
    private String threatLevel;

    @Column(name="emoji")
    private String emoji;

    @Column(name="threat_score")
    private float threatScore;

    // Context
    @Column(name="primary_context")
    private String primaryContext;


    private float contextConfidence;

    @ElementCollection
    private List<String> riskFactors;

    @ElementCollection
    private List<String> behavioralPatterns;

    // Violations
    @Column(name="total_violations")
    private int totalViolations;

    private int uniqueRuleTypes;

    @ElementCollection
    @MapKeyColumn(name="rule_name")
    @Column(name="rule_breakdown", columnDefinition="JSON")
    private Map<String, Integer> ruleBreakdown;


    // Narrative
    @Column(name="risk_analysis", columnDefinition="TEXT")
    private String riskAnalysis;
    private String behaviorInsights;

    @ElementCollection
    @Column(name="immediate_actions", columnDefinition="TEXT")
    private List<String> immediateActions;

    @ElementCollection
    private List<String> trainingRecommendations;

    // Storage
    @Column(name="certificate_file_path")
    private String certificateFilePath;

    @Column(name="created_at")
    private String createdAt;

    @Column(name="expires_at")
    private String expiresAt;


    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getCertificateUuid() {
        return certificateUuid;
    }

    public void setCertificateUuid(String certificateUuid) {
        this.certificateUuid = certificateUuid;
    }

    public String getAssessmentMethod() {
        return assessmentMethod;
    }


    public void setAssessmentMethod(String assessmentMethod) {
        this.assessmentMethod = assessmentMethod;
    }

    public String getAssessmentTime() {
        return assessmentTime;
    }

    public void setAssessmentTime(String assessmentTime) {
        this.assessmentTime = assessmentTime;
    }

    public List<String> getBehavioralPatterns() {
        return behavioralPatterns;
    }

    public void setBehavioralPatterns(List<String> behavioralPatterns) {
        this.behavioralPatterns = behavioralPatterns;
    }

    public String getBehaviorInsights() {
        return behaviorInsights;
    }

    public void setBehaviorInsights(String behaviorInsights) {
        this.behaviorInsights = behaviorInsights;
    }

    public String getCertificateFilePath() {
        return certificateFilePath;
    }

    public void setCertificateFilePath(String certificateFilePath) {
        this.certificateFilePath = certificateFilePath;
    }

    public float getContextConfidence() {
        return contextConfidence;
    }

    public void setContextConfidence(float contextConfidence) {
        this.contextConfidence = contextConfidence;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getImmediateActions() {
        return immediateActions;
    }

    public void setImmediateActions(List<String> immediateActions) {
        this.immediateActions = immediateActions;
    }

    public boolean isLlmAvailable() {
        return llmAvailable;
    }

    public void setLlmAvailable(boolean llmAvailable) {
        this.llmAvailable = llmAvailable;
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

    public List<String> getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors;
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

    public List<String> getTrainingRecommendations() {
        return trainingRecommendations;
    }

    public void setTrainingRecommendations(List<String> trainingRecommendations) {
        this.trainingRecommendations = trainingRecommendations;
    }

    public int getUniqueRuleTypes() {
        return uniqueRuleTypes;
    }

    public void setUniqueRuleTypes(int uniqueRuleTypes) {
        this.uniqueRuleTypes = uniqueRuleTypes;
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
