package com.ma.dlp.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private Long totalAlerts;  // Should be Long, not int
    private Integer alertsPercentageChange;

    private Integer activeAgents;
    private Integer newAgentsToday;

    private Long ocrViolations;  // Should be Long, not int
    private Integer ocrViolationsPercentageChange;

    private Integer activePolicies;
    private Integer pendingReviewPolicies;

    public Integer getActiveAgents() {
        return activeAgents;
    }

    public void setActiveAgents(Integer activeAgents) {
        this.activeAgents = activeAgents;
    }

    public Integer getActivePolicies() {
        return activePolicies;
    }

    public void setActivePolicies(Integer activePolicies) {
        this.activePolicies = activePolicies;
    }

    public Integer getAlertsPercentageChange() {
        return alertsPercentageChange;
    }

    public void setAlertsPercentageChange(Integer alertsPercentageChange) {
        this.alertsPercentageChange = alertsPercentageChange;
    }

    public Integer getNewAgentsToday() {
        return newAgentsToday;
    }

    public void setNewAgentsToday(Integer newAgentsToday) {
        this.newAgentsToday = newAgentsToday;
    }

    public Long getOcrViolations() {
        return ocrViolations;
    }

    public void setOcrViolations(Long ocrViolations) {
        this.ocrViolations = ocrViolations;
    }

    public Integer getOcrViolationsPercentageChange() {
        return ocrViolationsPercentageChange;
    }

    public void setOcrViolationsPercentageChange(Integer ocrViolationsPercentageChange) {
        this.ocrViolationsPercentageChange = ocrViolationsPercentageChange;
    }

    public Integer getPendingReviewPolicies() {
        return pendingReviewPolicies;
    }

    public void setPendingReviewPolicies(Integer pendingReviewPolicies) {
        this.pendingReviewPolicies = pendingReviewPolicies;
    }

    public Long getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(Long totalAlerts) {
        this.totalAlerts = totalAlerts;
    }
}