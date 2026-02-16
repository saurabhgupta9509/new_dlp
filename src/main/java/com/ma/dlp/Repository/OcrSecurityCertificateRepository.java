package com.ma.dlp.Repository;

import com.ma.dlp.model.OcrSecurityCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OcrSecurityCertificateRepository extends JpaRepository<OcrSecurityCertificate, Long> {

    Optional<OcrSecurityCertificate> findTopByAgentIdOrderByAssessmentTimeDesc(Long agentId);

    Optional<OcrSecurityCertificate> findTopByAgentIdOrderByCreatedAtDesc(Long agentId);
    List<OcrSecurityCertificate> findByAgentIdOrderByCreatedAtDesc(Long agentId);

    List<OcrSecurityCertificate> findByAgentIdOrderByAssessmentTimeDesc(Long agentId);





}
