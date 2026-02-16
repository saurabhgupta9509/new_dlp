package com.ma.dlp.dto;

import java.util.List;

public class CertificateWithViolationsRequest {
    private SecurityCertificateDTO certificate;
    private List<Long> violationIds;

    public SecurityCertificateDTO getCertificate() {
        return certificate;
    }

    public void setCertificate(SecurityCertificateDTO certificate) {
        this.certificate = certificate;
    }

    public List<Long> getViolationIds() {
        return violationIds;
    }

    public void setViolationIds(List<Long> violationIds) {
        this.violationIds = violationIds;
    }
}
