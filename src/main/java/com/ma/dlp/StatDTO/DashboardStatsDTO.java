package com.ma.dlp.StatDTO;

public class DashboardStatsDTO {

    private long totalAgents;
    private long onlineAgents;
    private long pendingAlerts;
    private long totalActivePolicies;

    // constructor
    public DashboardStatsDTO(long totalAgents, long onlineAgents,
                             long pendingAlerts, long totalActivePolicies) {
        this.totalAgents = totalAgents;
        this.onlineAgents = onlineAgents;
        this.pendingAlerts = pendingAlerts;
        this.totalActivePolicies = totalActivePolicies;
    }

    // getters

    public long getTotalAgents() {
        return totalAgents;
    }

    public long getOnlineAgents() {
        return onlineAgents;
    }

    public long getPendingAlerts() {
        return pendingAlerts;
    }

    public long getTotalActivePolicies() {
        return totalActivePolicies;
    }

    
}