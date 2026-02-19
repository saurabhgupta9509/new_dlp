package com.ma.dlp.model;
import com.ma.dlp.dto.ProtectionOperations;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class PolicyIntent {
    @JsonProperty("node_id")
    private Long nodeId;

    private String scope; // "File", "Folder", "FolderRecursive"
    private String action; // "Block", "Allow", "Audit"

    private ProtectionOperations operations;
    private String createdBy;
    private Long timestamp;
    private String comment;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public ProtectionOperations getOperations() {
        return operations;
    }

    public void setOperations(ProtectionOperations operations) {
        this.operations = operations;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}







