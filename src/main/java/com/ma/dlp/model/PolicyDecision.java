package com.ma.dlp.model;

import lombok.Data;

@Data
public class PolicyDecision {
    private boolean allowed;
    private String ruleType;
    private String ruleDescription;
    private boolean requiresApproval;

    public PolicyDecision() {
        this.allowed = false; // Default deny
        this.ruleType = "DEFAULT_DENY";
        this.ruleDescription = "No explicit permission found";
        this.requiresApproval = false;
    }


    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }
}