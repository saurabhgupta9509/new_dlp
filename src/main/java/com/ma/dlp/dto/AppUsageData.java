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



}