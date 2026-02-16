package com.ma.dlp.dto;

import jdk.dynalink.Operation;
import lombok.Data;

import java.util.Date;
import java.util.List;
import com.ma.dlp.model.FileSystemPermission.PermissionType;
@Data
public class FileSystemPermission {
    private Long id;
    private String path;
    private PermissionType type; // FILE or DIRECTORY
    private List<Operation> allowedOperations;
    private List<String> allowedProcesses;
    private List<String> allowedUsers;
    private Boolean recursive;
    private String description;
    private Date createdAt;
    private Date updatedAt;


    public List<Operation> getAllowedOperations() {
        return allowedOperations;
    }

    public void setAllowedOperations(List<Operation> allowedOperations) {
        this.allowedOperations = allowedOperations;
    }

    public List<String> getAllowedProcesses() {
        return allowedProcesses;
    }

    public void setAllowedProcesses(List<String> allowedProcesses) {
        this.allowedProcesses = allowedProcesses;
    }

    public List<String> getAllowedUsers() {
        return allowedUsers;
    }

    public void setAllowedUsers(List<String> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getRecursive() {
        return recursive;
    }

    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    public com.ma.dlp.model.FileSystemPermission.PermissionType getType() {
        return type;
    }

    public void setType(com.ma.dlp.model.FileSystemPermission.PermissionType type) {
        this.type = type;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
