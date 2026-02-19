package com.ma.dlp.dto;
import lombok.Data;
import java.util.Map;

@Data
public class ApplyPolicyRequestDTO {
    private Long node_id;
    private String scope;
    private String action;
    private Map<String, Boolean> operations;
    private String created_by;
    private String comment;
    private Boolean confirmed;
    private Long timestamp;


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

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public Long getNode_id() {
        return node_id;
    }

    public void setNode_id(Long node_id) {
        this.node_id = node_id;
    }

    public Map<String, Boolean> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, Boolean> operations) {
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