package com.ma.dlp.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProtectionAssignmentRequest {
    private String policyCode;
    private List<Long> agentIds;
    private String policyData;

    public String getPolicyData() {
        return policyData;
    }

    public void setPolicyData(String policyData) {
        this.policyData = policyData;
    }

    public List<Long> getAgentIds() {
        return agentIds;
    }

    public void setAgentIds(List<Long> agentIds) {
        this.agentIds = agentIds;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
    }
}
