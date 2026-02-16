package com.ma.dlp.dto;

import lombok.Data;

@Data
public class FilePolicyAssignmentRequest {
    private Long agentId;
    private String policyCode;
    private String operation; // READ, WRITE, CREATE, DELETE
    private String filePath;
    private String action; // BLOCK, ALLOW

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
    }
}