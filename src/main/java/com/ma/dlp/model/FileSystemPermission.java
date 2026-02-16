package com.ma.dlp.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "file_system_permissions")
public class FileSystemPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(name = "`type`")
    private PermissionType type;

    @ElementCollection
    @CollectionTable(name = "permission_operations", joinColumns = @JoinColumn(name = "permission_id"))
    @Column(name = "operation")
    @Enumerated(EnumType.STRING)
    private List<Operation> allowedOperations;

    @ElementCollection
    @CollectionTable(name = "permission_processes", joinColumns = @JoinColumn(name = "permission_id"))
    @Column(name = "process_name")
    private List<String> allowedProcesses;

    @ElementCollection
    @CollectionTable(name = "permission_users", joinColumns = @JoinColumn(name = "permission_id"))
    @Column(name = "user_id")
    private List<String> allowedUsers;

    @Column(name = "`recursive`")
    private Boolean recursive = false;

    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    public enum PermissionType {
        FILE, DIRECTORY
    }

    public enum Operation {
        READ, WRITE, CREATE, DELETE, RENAME, COPY, MOVE, EXECUTE
    }


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

    public PermissionType getType() {
        return type;
    }

    public void setType(PermissionType type) {
        this.type = type;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}