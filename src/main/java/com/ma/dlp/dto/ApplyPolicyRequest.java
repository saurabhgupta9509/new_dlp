package com.ma.dlp.dto;

import com.ma.dlp.model.PolicyIntent;
import lombok.Data;

@Data
public class ApplyPolicyRequest {
    private PolicyIntent intent;
    private Boolean confirmed;

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public PolicyIntent getIntent() {
        return intent;
    }

    public void setIntent(PolicyIntent intent) {
        this.intent = intent;
    }
}
