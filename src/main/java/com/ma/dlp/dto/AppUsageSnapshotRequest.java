package com.ma.dlp.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AppUsageSnapshotRequest {
    // Accept both snake_case and camelCase from different agent versions
    @JsonAlias({"timestamp"})
    private String timestamp;

    @JsonAlias({"currentApp", "current_app"})
    private String currentApp;

    @JsonAlias({"currentSessionDuration", "current_session_duration"})
    private Double currentSessionDuration;

    @JsonAlias({"totalAppsTracked", "total_apps_tracked"})
    private Integer totalAppsTracked;

    @JsonAlias({"totalTimeTracked", "total_time_tracked"})
    private Double totalTimeTracked;

    @JsonAlias({"activeUsageTime", "active_usage_time"})
    private Double activeUsageTime;

    @JsonAlias({"categoryBreakdown", "category_breakdown"})
    private Map<String, Double> categoryBreakdown;

    // Keep as Object to avoid strict schema issues
    @JsonProperty("topApps")
    @JsonAlias({"top_apps"})
    private Object topApps;

    public Double getActiveUsageTime() {
        return activeUsageTime;
    }

    public void setActiveUsageTime(Double activeUsageTime) {
        this.activeUsageTime = activeUsageTime;
    }

    public Map<String, Double> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(Map<String, Double> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public String getCurrentApp() {
        return currentApp;
    }

    public void setCurrentApp(String currentApp) {
        this.currentApp = currentApp;
    }

    public Double getCurrentSessionDuration() {
        return currentSessionDuration;
    }

    public void setCurrentSessionDuration(Double currentSessionDuration) {
        this.currentSessionDuration = currentSessionDuration;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Object getTopApps() {
        return topApps;
    }

    public void setTopApps(Object topApps) {
        this.topApps = topApps;
    }

    public Integer getTotalAppsTracked() {
        return totalAppsTracked;
    }

    public void setTotalAppsTracked(Integer totalAppsTracked) {
        this.totalAppsTracked = totalAppsTracked;
    }

    public Double getTotalTimeTracked() {
        return totalTimeTracked;
    }

    public void setTotalTimeTracked(Double totalTimeTracked) {
        this.totalTimeTracked = totalTimeTracked;
    }
}

