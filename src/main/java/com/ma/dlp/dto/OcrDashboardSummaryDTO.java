package com.ma.dlp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OcrDashboardSummaryDTO {
    private long totalCapableAgents;
    private long activeOcr;

    public OcrDashboardSummaryDTO(long activeOcr, long totalCapableAgents) {
        this.activeOcr = activeOcr;
        this.totalCapableAgents = totalCapableAgents;
    }

    public long getActiveOcr() {
        return activeOcr;
    }

    public void setActiveOcr(long activeOcr) {
        this.activeOcr = activeOcr;
    }

    public long getTotalCapableAgents() {
        return totalCapableAgents;
    }

    public void setTotalCapableAgents(long totalCapableAgents) {
        this.totalCapableAgents = totalCapableAgents;
    }
}
