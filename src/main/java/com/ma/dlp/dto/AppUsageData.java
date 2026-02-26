package com.ma.dlp.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AppUsageData {
    private String timestamp;
    private String currentApp;
    private double currentSessionDuration;
    private int totalAppsTracked;
    private double totalTimeTracked;
    private double activeUsageTime;
    private List<Map<String, Object>> topApps;
    private Map<String, Double> categoryBreakdown;
    
    // Getters and setters (Lombok @Data will generate these)


    public double getActiveUsageTime() {
        return activeUsageTime;
    }

    public void setActiveUsageTime(double activeUsageTime) {
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

    public double getCurrentSessionDuration() {
        return currentSessionDuration;
    }

    public void setCurrentSessionDuration(double currentSessionDuration) {
        this.currentSessionDuration = currentSessionDuration;
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

    public int getTotalAppsTracked() {
        return totalAppsTracked;
    }

    public void setTotalAppsTracked(int totalAppsTracked) {
        this.totalAppsTracked = totalAppsTracked;
    }

    public double getTotalTimeTracked() {
        return totalTimeTracked;
    }

    public void setTotalTimeTracked(double totalTimeTracked) {
        this.totalTimeTracked = totalTimeTracked;
    }
}