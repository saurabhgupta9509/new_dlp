package com.ma.dlp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PartialAccessDTO {
    private String urlPattern;
    private String domain;
    private String reason;
    private String category;
    private boolean active;
    private boolean global;
    private boolean allowUpload;
    private boolean allowDownload;
    private List<String> restrictedFileTypes;
    private String monitorMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int uploadAttempts;
    private int downloadAttempts;
    private String addedBy;
    private String deviceId;
    private String userId;
    
    // Getters and Setters
    public String getUrlPattern() {
        return urlPattern;
    }
    
    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isGlobal() {
        return global;
    }
    
    public void setGlobal(boolean global) {
        this.global = global;
    }
    
    public boolean isAllowUpload() {
        return allowUpload;
    }
    
    public void setAllowUpload(boolean allowUpload) {
        this.allowUpload = allowUpload;
    }
    
    public boolean isAllowDownload() {
        return allowDownload;
    }
    
    public void setAllowDownload(boolean allowDownload) {
        this.allowDownload = allowDownload;
    }
    
    public List<String> getRestrictedFileTypes() {
        return restrictedFileTypes;
    }
    
    public void setRestrictedFileTypes(List<String> restrictedFileTypes) {
        this.restrictedFileTypes = restrictedFileTypes;
    }
    
    public String getMonitorMode() {
        return monitorMode;
    }
    
    public void setMonitorMode(String monitorMode) {
        this.monitorMode = monitorMode;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public int getUploadAttempts() {
        return uploadAttempts;
    }
    
    public void setUploadAttempts(int uploadAttempts) {
        this.uploadAttempts = uploadAttempts;
    }
    
    public int getDownloadAttempts() {
        return downloadAttempts;
    }
    
    public void setDownloadAttempts(int downloadAttempts) {
        this.downloadAttempts = downloadAttempts;
    }
    
    public String getAddedBy() {
        return addedBy;
    }
    
    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
}