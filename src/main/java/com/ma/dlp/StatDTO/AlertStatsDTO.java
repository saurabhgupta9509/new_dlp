package com.ma.dlp.StatDTO;

public class AlertStatsDTO {
    private long totalAlerts;
    private long criticalAlerts;
    private long highAlerts;
    private long pendingAlertsCount;



    public AlertStatsDTO(long totalAlerts, long criticalAlerts, long highAlerts, long pendingAlertsCount) {
        this.totalAlerts = totalAlerts;
        this.criticalAlerts = criticalAlerts;
        this.highAlerts = highAlerts;
        this.pendingAlertsCount = pendingAlertsCount;
    }

    public long getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(long totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public long getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(long criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }

    public long getHighAlerts() {
        return highAlerts;
    }

    public void setHighAlerts(long highAlerts) {
        this.highAlerts = highAlerts;
    }

    public long getPendingAlertsCount() {
        return pendingAlertsCount;
    }

    public void setPendingAlertsCount(long pendingAlertsCount) {
        this.pendingAlertsCount = pendingAlertsCount;
    }

   
}
