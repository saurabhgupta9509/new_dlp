package com.ma.dlp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocr_status")
@Data
public class OcrStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long agentId;

    private String agentHostname;

    @Column(name = "agent_username")  // Add this column
    private String agentUsername;     // Add this field

    private boolean ocrEnabled;

    private boolean ocrCapable;

    private String threatArrow;      // NEW COLUMN
    private String trendColor;

    private Float threatScore;

    private Integer violationsLast24h;

    private LocalDateTime lastScreenshotTime;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_user_id")
    private User agent;  // This creates the foreign key

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

    public boolean isOcrCapable() {
        return ocrCapable;
    }

    public void setOcrCapable(boolean ocrCapable) {
        this.ocrCapable = ocrCapable;
    }

    public User getAgent() {
        return agent;
    }

    public void setAgent(User agent) {
        this.agent = agent;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getLastScreenshotTime() {
        return lastScreenshotTime;
    }

    public void setLastScreenshotTime(LocalDateTime lastScreenshotTime) {
        this.lastScreenshotTime = lastScreenshotTime;
    }

    

    public boolean isOcrEnabled() {
        return ocrEnabled;
    }

    public void setOcrEnabled(boolean ocrEnabled) {
        this.ocrEnabled = ocrEnabled;
    }

    public Float getThreatScore() {
        return threatScore;
    }

    public void setThreatScore(Float threatScore) {
        this.threatScore = threatScore;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getViolationsLast24h() {
        return violationsLast24h;
    }

    public void setViolationsLast24h(Integer violationsLast24h) {
        this.violationsLast24h = violationsLast24h;
    }
}
