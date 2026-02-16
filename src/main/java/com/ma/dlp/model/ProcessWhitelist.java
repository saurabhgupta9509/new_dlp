package com.ma.dlp.model;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "process_whitelist")
public class ProcessWhitelist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String processName;

    private String processPath;
    private String description;

    @ElementCollection
    @CollectionTable(name = "whitelist_allowed_paths", joinColumns = @JoinColumn(name = "whitelist_id"))
    @Column(name = "allowed_path")
    private List<String> allowedPaths;

    private Boolean systemWide = false;


    public List<String> getAllowedPaths() {
        return allowedPaths;
    }

    public void setAllowedPaths(List<String> allowedPaths) {
        this.allowedPaths = allowedPaths;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessPath() {
        return processPath;
    }

    public void setProcessPath(String processPath) {
        this.processPath = processPath;
    }

    public Boolean getSystemWide() {
        return systemWide;
    }

    public void setSystemWide(Boolean systemWide) {
        this.systemWide = systemWide;
    }
}