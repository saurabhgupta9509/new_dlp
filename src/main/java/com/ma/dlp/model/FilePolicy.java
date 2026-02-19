package com.ma.dlp.model;

import jakarta.persistence.*;
@Entity
@Table(name = "FilePolicies")
public class FilePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_id")
    private Long nodeId;

    private String scope; // "file", "folder", "folder_recursive"
    private String action; // "block", "allow", "audit"

    @Column(columnDefinition = "TEXT")
    private String operations; // JSON: {"read": true, "write": false, ...}

    @Column(name = "created_by")
    private String createdBy;
    private String comment;
    private Boolean confirmed = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    // For Rust Agent tracking
    @Column(name = "agent_policy_id")
    private Long agentPolicyId; // Policy ID returned by Rust Agent

    @Column(name = "agent_synced")
    private Boolean agentSynced = false;

    public void setAction(String action) {
        this.action = action;
    }

    public void setAgentPolicyId(Long agentPolicyId) {
        this.agentPolicyId = agentPolicyId;
    }

    public void setAgentSynced(Boolean agentSynced) {
        this.agentSynced = agentSynced;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public void setOperations(String operations) {
        this.operations = operations;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAction() {
        return action;
    }

    public Long getAgentPolicyId() {
        return agentPolicyId;
    }

    public Boolean getAgentSynced() {
        return agentSynced;
    }

    public String getComment() {
        return comment;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Long getId() {
        return id;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public String getOperations() {
        return operations;
    }

    public String getScope() {
        return scope;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

}
