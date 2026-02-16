package com.ma.dlp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "partial_access_sites")
public class PartialAccessEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "url_pattern", nullable = false, unique = true)
    private String urlPattern;
    
    @Column(name = "domain")
    private String domain;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    
    @Column(name = "is_global", nullable = false)
    private boolean global = true;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "allow_upload", nullable = false)
    private boolean allowUpload = false;
    
    @Column(name = "allow_download", nullable = false)
    private boolean allowDownload = false;
    
    @Column(name = "restricted_file_types", columnDefinition = "TEXT")
    private String restrictedFileTypes; // JSON array: ["exe", "zip", "pdf", ...]
    
    @Column(name = "monitor_mode", nullable = false)
    private String monitorMode = "block"; // "block", "warn", "log-only"
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "added_by")
    private String addedBy;
    
    @Column(name = "upload_attempts")
    private int uploadAttempts = 0;
    
    @Column(name = "download_attempts")
    private int downloadAttempts = 0;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getRestrictedFileTypes() {
        return restrictedFileTypes;
    }
    
    public void setRestrictedFileTypes(String restrictedFileTypes) {
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
    
    public String getAddedBy() {
        return addedBy;
    }
    
    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
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
}