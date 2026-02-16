package com.ma.dlp.dto;

import java.util.List;

public class BlockedUrlRequest {
    private String urlPattern;
    private String domain;
    private String reason;
    private String category;
    private boolean active = true;
    
    // For bulk operations
    private List<BlockedUrlRequest> blockedUrls;
    private List<String> deletePatterns;
    private String deviceId; // Optional: for device-specific blocking
    private String userId;   // Optional: for user-specific blocking
    
    // Getters and Setters
    public String getUrlPattern() { return urlPattern; }
    public void setUrlPattern(String urlPattern) { this.urlPattern = urlPattern; }
    
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public List<BlockedUrlRequest> getBlockedUrls() { return blockedUrls; }
    public void setBlockedUrls(List<BlockedUrlRequest> blockedUrls) { this.blockedUrls = blockedUrls; }
    
    public List<String> getDeletePatterns() { return deletePatterns; }
    public void setDeletePatterns(List<String> deletePatterns) { this.deletePatterns = deletePatterns; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}