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
}

