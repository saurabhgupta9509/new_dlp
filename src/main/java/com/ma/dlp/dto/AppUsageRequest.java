package com.ma.dlp.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class AppUsageRequest {
    private String deviceId;
    private String timestamp;
    private String currentApp;
    private Double currentSessionDuration;
    private Integer totalAppsTracked;
    private Double totalTimeTracked;
    private List<Map<String, Object>> topApps;
    private Map<String, Object> categoryBreakdown;
    private Double activeUsageTime; 

    public Double getActiveUsageTime() {
        return activeUsageTime;
    }
    
    public void setActiveUsageTime(Double activeUsageTime) {
        this.activeUsageTime = activeUsageTime;
    }

    public Map<String, Object> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(Map<String, Object> categoryBreakdown) {
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<Map<String, Object>> getTopApps() {
        return topApps;
    }

    public void setTopApps(List<Map<String, Object>> topApps) {
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
