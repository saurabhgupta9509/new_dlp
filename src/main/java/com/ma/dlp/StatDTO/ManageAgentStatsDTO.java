package com.ma.dlp.StatDTO;

public class ManageAgentStatsDTO {
    private long totalAgents;
    private long onlineAgents;
    private long offlineAgents;
    private long needsUpdate;

    public long getTotalAgents() {
        return totalAgents;
    }

    public void setTotalAgents(long totalAgents) {
        this.totalAgents = totalAgents;
    }

    public long getOnlineAgents() {
        return onlineAgents;
    }

    public void setOnlineAgents(long onlineAgents) {
        this.onlineAgents = onlineAgents;
    }

    public long getOfflineAgents() {
        return offlineAgents;
    }

    public void setOfflineAgents(long offlineAgents) {
        this.offlineAgents = offlineAgents;
    }

    public long getNeedsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(long needsUpdate) {
        this.needsUpdate = needsUpdate;
    }


}
