package com.ma.dlp.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ma.dlp.Repository.AgentCapabilityRepository;
import com.ma.dlp.Repository.OcrLiveDataRepository;
import com.ma.dlp.Repository.OcrSecurityCertificateRepository;
import com.ma.dlp.Repository.OcrStatusRepository;
import com.ma.dlp.Repository.OcrViolationRepository;
import com.ma.dlp.dto.*;
import com.ma.dlp.model.*;

import jakarta.transaction.Transactional;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OcrService {

    @Autowired
    private OcrStatusRepository statusRepository;

    @Autowired
    private OcrViolationRepository ocrViolationRepository;

    @Autowired
    private OcrLiveDataRepository liveDataRepository;

    @Autowired
    private OcrSecurityCertificateRepository certificateRepository;

    @Autowired
    private OcrViolationRepository ocrviolationRepository;

    @Autowired
    private AgentCapabilityRepository agentCapabilityRepository;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // private static final String OCR_CAPABILITY_CODE = "SECURITY_MONITOR";
    private static final String OCR_CAPABILITY_CODE = "POLICY_OCR_MONITOR";

    // --- FROM AGENT (Rust) ---

    public long getTotalOcrCapableAgents() {
        return agentCapabilityRepository
                .countAgentsWithCapability(OCR_CAPABILITY_CODE);
    }

    public OcrDashboardSummaryDTO getOcrDashboardSummary() {

        // 1️⃣ Get ALL OCR-capable agents
        List<Long> ocrCapableAgentIds = agentCapabilityRepository.findAgentIdsWithCapability(OCR_CAPABILITY_CODE);

        long totalCapableAgents = ocrCapableAgentIds.size();

        // if (totalCapableAgents == 0) {
        // return new OcrDashboardSummaryDTO(0, 0);
        // }

        // // 2️⃣ Get latest status for these agents
        // List<OcrStatus> latestStatuses =
        // statusRepository.findLatestForAgents(ocrCapableAgentIds);

        // 3️⃣ Count agents with ocrEnabled = true in their latest status
        long activeOcr = statusRepository.findLatestForAgents(ocrCapableAgentIds)
                .stream()
                .filter(status -> status.isOcrEnabled())
                .count();

        return new OcrDashboardSummaryDTO(activeOcr, totalCapableAgents);
    }

    public long getActiveOcrAgents() {
        return statusRepository.countByOcrEnabledTrue();
    }

    public void saveStatusFromAgent(OcrStatusUpdateDTO dto) {
        OcrStatus status =
                // new OcrStatus();
                statusRepository
                        .findTopByAgentIdOrderByUpdatedAtDesc(dto.getAgentId())
                        .orElse(new OcrStatus());

        status.setAgentId(dto.getAgentId());
        status.setAgentHostname(dto.getAgentHostname());
        // status.setOcrEnabled(dto.isOcrEnabled());
        status.setAgentUsername(dto.getUsername());
        boolean isCapableAndActive = agentCapabilityRepository
                .existsByAgent_IdAndCapabilityCodeAndIsActiveTrue(
                        dto.getAgentId(),
                        OCR_CAPABILITY_CODE);

        status.setOcrEnabled(isCapableAndActive && dto.isOcrEnabled());
        // status.setOcrEnabled(isCapableAndActive && dto.isOcrEnabled());
        status.setThreatScore(dto.getThreatScore());
        // ✅ NEW: Save threat arrow and trend color
        status.setThreatArrow(dto.getThreatArrow() != null ? dto.getThreatArrow() : "→");
        status.setTrendColor(dto.getTrendColor() != null ? dto.getTrendColor() : "gray");

        status.setViolationsLast24h(dto.getViolationsLast24h());
        status.setLastScreenshotTime(parseDateTime(dto.getLastScreenshotTime()));
        status.setUpdatedAt(LocalDateTime.now());

        userService.findById(dto.getAgentId())
                .ifPresent(status::setAgent);

        statusRepository.save(status);
    }

    public void saveLiveDataFromAgent(OcrLiveDataDTO dto) {
        OcrLiveData live = new OcrLiveData();
        live.setAgentId(dto.getAgentId());
        live.setScreenshotPath(dto.getScreenshotPath());
        live.setExtractedText(dto.getExtractedText());
        live.setContentType(dto.getContentType());
        live.setLanguage(dto.getLanguage());
        live.setReadabilityScore(dto.getReadabilityScore());
        live.setThreatScore(dto.getThreatScore());
        live.setViolationCount(dto.getViolationCount());
        live.setPrimaryContext(dto.getPrimaryContext());
        live.setActive(dto.isActive());
        live.setTimestamp(parseDateTime(dto.getTimestamp()));

        userService.findById(dto.getAgentId())
                .ifPresent(live::setAgent);

        liveDataRepository.save(live);
    }

    // public Long saveViolationFromAgent(OcrViolationDTO dto) {
    // OcrViolation violation = new OcrViolation();
    // violation.setAgentId(dto.getAgentId());
    // violation.setRuleType(dto.getRuleType());
    // violation.setMatchedText(dto.getMatchedText());
    // violation.setConfidence(dto.getConfidence());
    // violation.setThreatScore(dto.getThreatScore());
    // violation.setContextConfidence(dto.getContextConfidence());
    // violation.setScreenshotPath(dto.getScreenshotPath());
    // violation.setTimestamp(parseDateTime(dto.getTimestamp()));
    // //
    // // userService.findById(dto.getAgentId())
    // // .ifPresent(violation::setAgent);

    // violationRepository.save(violation);
    // }
    @Transactional
    public Long saveViolationFromAgent(OcrViolationDTO dto) {
        // Convert DTO to entity
        OcrViolation entity = new OcrViolation();
        entity.setAgentId(dto.getAgentId());
        entity.setTimestamp(parseDateTime(dto.getTimestamp()));
        entity.setRuleType(dto.getRuleType());
        entity.setMatchedText(dto.getMatchedText());
        entity.setConfidence(dto.getConfidence());
        entity.setThreatScore(dto.getThreatScore());
        entity.setContextConfidence(dto.getContextConfidence());
        entity.setScreenshotPath(dto.getScreenshotPath());

        // Save and return ID
        entity = ocrviolationRepository.save(entity);
        return entity.getId();
    }

    // public List<OcrDashboardStatsDTO> getLatestStatusForAllAgents() {
    // return statusRepository.findLatestForAllAgents()
    // .stream()
    // .map(this::toDashboardDTO)
    // .collect(Collectors.toList());
    // }

    // public List<OcrDashboardStatsDTO> getLatestStatusForAllAgents() {
    //
    // // 1️⃣ Get agents that are OCR-capable
    // List<Long> ocrCapableAgentIds =
    // agentCapabilityRepository.findAllAgentIdsWithCapability(OCR_CAPABILITY_CODE);
    //
    // if (ocrCapableAgentIds.isEmpty()) {
    // return List.of();
    // }
    //
    // System.out.println("OCR Capable Agent IDs: " + ocrCapableAgentIds); // Debug
    // log System.out.println("OCR Capable Agent IDs: " + ocrCapableAgentIds); //
    // Debug log
    //
    // // 2️⃣ Fetch latest OCR status for ALL capable agents
    // List<OcrStatus> allStatuses =
    // statusRepository.findLatestForAgents(ocrCapableAgentIds);
    //
    // // 3️⃣ Create a map to ensure we have at least a placeholder for each capable
    // agent
    // Map<Long, OcrStatus> statusMap = allStatuses.stream()
    // .collect(Collectors.toMap(OcrStatus::getAgentId, Function.identity()));
    //
    // // 4️⃣ Return DTOs for ALL capable agents
    // return ocrCapableAgentIds.stream()
    // .map(agentId -> {
    // OcrStatus status = statusMap.get(agentId);
    // if (status != null) {
    // OcrDashboardStatsDTO dto = toDashboardDTO(status);
    // dto.setOcrCapable(true);
    // return dto;
    // } else {
    // // Create a default DTO for agents with no status yet
    // OcrDashboardStatsDTO dto = new OcrDashboardStatsDTO();
    // dto.setAgentId(agentId);
    // dto.setOcrCapable(true); // This agent HAS OCR capability
    // dto.setOcrEnabled(false); // But OCR is not enabled/reported yet
    //
    // dto.setCurrentThreatScore(0f);
    // dto.setViolationsLast24h(0);
    //
    // // Try to get agent hostname from UserService if available
    // userService.findById(agentId).ifPresent(user -> {
    // dto.setAgentHostname(user.getHostname() != null ? user.getHostname() :
    // user.getUsername() != null ? user.getUsername() :
    // "Agent " + agentId);
    // });
    //
    // if (dto.getAgentHostname() == null) {
    // dto.setAgentHostname("Agent " + agentId);
    // }
    //
    // return dto;
    // }
    // })
    // .collect(Collectors.toList());
    //
    // }

    // public List<OcrDashboardStatsDTO> getLatestStatusForAllAgents() {

    // // 1️⃣ ONLY OCR-capable agents (SOURCE OF TRUTH)
    // List<Long> ocrCapableAgentIds =
    // agentCapabilityRepository.findAgentIdsWithCapability(OCR_CAPABILITY_CODE);

    // if (ocrCapableAgentIds.isEmpty()) {
    // return List.of();
    // }

    // // 2️⃣ Latest OCR status ONLY for capable agents
    // List<OcrStatus> latestStatuses =
    // statusRepository.findLatestForAgents(ocrCapableAgentIds);

    // Map<Long, OcrStatus> statusMap = latestStatuses.stream()
    // .collect(Collectors.toMap(OcrStatus::getAgentId, Function.identity()));

    // // 3️⃣ Build DTO for EVERY capable agent
    // return ocrCapableAgentIds.stream()
    // .map(agentId -> {
    // OcrDashboardStatsDTO dto = new OcrDashboardStatsDTO();
    // dto.setAgentId(agentId);
    // // dto.setOcrCapable(true); // 🔒 ALWAYS true here

    // OcrStatus status = statusMap.get(agentId);

    // if (status != null) {
    // dto.setAgentHostname(status.getAgentHostname());
    // dto.setAgentUsername(status.getAgentUsername());
    // dto.setOcrEnabled(status.isOcrEnabled());
    // dto.setCurrentThreatScore(
    // status.getThreatScore() != null ? status.getThreatScore() : 0f
    // );

    // // ✅ NEW: Set threat arrow and trend color
    // dto.setThreatArrow(status.getThreatArrow());
    // dto.setTrendColor(status.getTrendColor());

    // dto.setViolationsLast24h(
    // status.getViolationsLast24h() != null ? status.getViolationsLast24h() : 0
    // );
    // dto.setLastScreenshotTime(status.getLastScreenshotTime());
    // } else {
    // // Never reported yet → default OFF
    // dto.setOcrEnabled(false);
    // dto.setCurrentThreatScore(0f);

    // dto.setThreatArrow("→"); // Default arrow
    // dto.setTrendColor("gray"); // Default color

    // dto.setViolationsLast24h(0);
    // dto.setLastScreenshotTime(null);

    // userService.findById(agentId).ifPresent(user ->{
    // dto.setAgentUsername(user.getUsername());
    // dto.setAgentHostname(
    // user.getHostname() != null
    // ? user.getHostname()
    // : user.getUsername()
    // );
    // }

    // );
    // if (dto.getAgentUsername() == null) {
    // dto.setAgentUsername("Agent " + agentId);
    // }
    // if (dto.getAgentHostname() == null) {
    // dto.setAgentHostname("Agent " + agentId);
    // }
    // }

    // return dto;
    // })
    // .collect(Collectors.toList());
    // }

    public List<OcrDashboardStatsDTO> getLatestStatusForAllAgents() {
        // 1️⃣ ONLY OCR-capable agents (SOURCE OF TRUTH)
        List<Long> ocrCapableAgentIds = agentCapabilityRepository.findAgentIdsWithCapability(OCR_CAPABILITY_CODE);

        if (ocrCapableAgentIds.isEmpty()) {
            return List.of();
        }

        // 2️⃣ Latest OCR status ONLY for capable agents
        List<OcrStatus> latestStatuses = statusRepository.findLatestForAgents(ocrCapableAgentIds);

        Map<Long, OcrStatus> statusMap = latestStatuses.stream()
                .collect(Collectors.toMap(OcrStatus::getAgentId, Function.identity()));

        // 3️⃣ Build DTO for EVERY capable agent
        return ocrCapableAgentIds.stream()
                .map(agentId -> {
                    OcrDashboardStatsDTO dto = new OcrDashboardStatsDTO();
                    dto.setAgentId(agentId);

                    OcrStatus status = statusMap.get(agentId);
                    // ✅ ALWAYS get hostname from User entity, not from OCR status
                    Optional<User> userOpt = userService.findById(agentId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        // Get actual hostname from User entity
                        String actualHostname = user.getHostname();
                        String username = user.getUsername();

                        dto.setAgentHostname(actualHostname != null ? actualHostname : "Agent " + agentId);
                        dto.setAgentUsername(username != null ? username : "N/A");
                    } else {
                        // Fallback if user not found
                        dto.setAgentHostname("Agent " + agentId);
                        dto.setAgentUsername("Unknown");
                    }

                    if (status != null) {
                        // ✅ DON'T overwrite hostname from status (it's IP)
                        // Keep the hostname from User entity above

                        dto.setOcrEnabled(status.isOcrEnabled());
                        dto.setCurrentThreatScore(
                                status.getThreatScore() != null ? status.getThreatScore() : 0f);

                        // ✅ NEW: Set threat arrow and trend color
                        dto.setThreatArrow(status.getThreatArrow());
                        dto.setTrendColor(status.getTrendColor());

                        dto.setViolationsLast24h(
                                status.getViolationsLast24h() != null ? status.getViolationsLast24h() : 0);
                        dto.setLastScreenshotTime(status.getLastScreenshotTime());
                    } else {
                        // Never reported yet → default OFF
                        dto.setOcrEnabled(false);
                        dto.setCurrentThreatScore(0f);

                        dto.setThreatArrow("→"); // Default arrow
                        dto.setTrendColor("gray"); // Default color

                        dto.setViolationsLast24h(0);
                        dto.setLastScreenshotTime(null);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<OcrLiveData> getRecentLiveData(Long agentId) {
        return liveDataRepository.findTop50ByAgentIdOrderByTimestampDesc(agentId);
    }

    public List<OcrViolation> getViolations(Long agentId) {
        return ocrviolationRepository.findByAgentIdOrderByTimestampDesc(agentId);
    }

    // public void saveCertificateFromAgent(SecurityCertificateDTO dto , List<Long>
    // violationIds) {
    //
    // OcrSecurityCertificate cert = new OcrSecurityCertificate();
    //
    // cert.setAgentId(dto.getAgentId());
    // cert.setAssessmentTime(dto.getAssessmentTime());
    // cert.setUserDevice(dto.getUserDevice());
    // cert.setUserMac(dto.getUserMac());
    //
    // cert.setThreatLevel(dto.getThreatLevel());
    // cert.setEmoji(dto.getEmoji());
    // cert.setThreatScore(dto.getThreatScore());
    // cert.setPrimaryContext(dto.getPrimaryContext());
    //
    // cert.setTotalViolations(dto.getTotalViolations());
    // cert.setRuleBreakdown(dto.getRuleBreakdown());
    //
    // cert.setRiskAnalysis(dto.getRiskAnalysis());
    // cert.setImmediateActions(dto.getImmediateActions());
    //
    // cert.setCreatedAt(LocalDateTime.now().toString());
    // cert.setExpiresAt(LocalDateTime.now().plusMinutes(10).toString());
    // try{
    // // Generate UUID for certificate
    // cert.setCertificateFilePath("CERT-" + UUID.randomUUID());
    // String baseDir = "src/main/resources/certificates/agent_" +
    // cert.getAgentId();
    // File dir = new File(baseDir);
    // if (!dir.exists()) dir.mkdirs();
    //
    // String filename = "certificate_" + cert.getAssessmentTime().replace(":", "-")
    // + ".pdf";
    // String pdfPath = baseDir + "/" + filename;
    //
    //
    //// String pdfPath = baseDir + "certificates/" + cert.getAgentId() + "_" +
    // System.currentTimeMillis() + ".pdf";
    //
    // generateCertificatePdf(cert, pdfPath);
    //
    // cert.setCertificateFilePath(pdfPath);
    // certificateRepository.save(cert);
    // certificateRepository.save(cert);
    // }catch (Exception e) {
    // e.printStackTrace();
    // }
    //
    // }

    public void saveCertificateFromAgent(SecurityCertificateDTO dto, List<Long> violationIds) {
        OcrSecurityCertificate cert = new OcrSecurityCertificate();

        cert.setAgentId(dto.getAgentId());
        cert.setAssessmentTime(dto.getAssessmentTime());
        cert.setUserDevice(dto.getUserDevice());
        cert.setUserMac(dto.getUserMac());
        cert.setThreatLevel(dto.getThreatLevel());
        cert.setEmoji(dto.getEmoji());
        cert.setThreatScore(dto.getThreatScore());
        cert.setPrimaryContext(dto.getPrimaryContext());
        cert.setTotalViolations(dto.getTotalViolations());
        cert.setRuleBreakdown(dto.getRuleBreakdown());
        cert.setRiskAnalysis(dto.getRiskAnalysis());
        cert.setImmediateActions(dto.getImmediateActions());
        cert.setCreatedAt(LocalDateTime.now().toString());
        cert.setExpiresAt(LocalDateTime.now().plusMinutes(10).toString());

        try {
            // Generate UUID for certificate store
            String uuid = "CERT-" + UUID.randomUUID().toString();
            cert.setCertificateUuid(uuid);

            String baseDir = "src/main/resources/certificates/agent_" + cert.getAgentId();
            File dir = new File(baseDir);
            if (!dir.exists())
                dir.mkdirs();

            String filename = "certificate_" + cert.getAssessmentTime().replace(":", "-") + ".pdf";
            String pdfPath = baseDir + "/" + filename;

            // Create PDF
            generateCertificatePdf(cert, pdfPath);
            cert.setCertificateFilePath(pdfPath);

            // Save certificate (persist to get ID)
            OcrSecurityCertificate saved = certificateRepository.save(cert);

            // Attach violations (if provided)
            if (violationIds != null && !violationIds.isEmpty()) {
                try {
                    int updated = ocrviolationRepository.assignViolationsToCertificate(saved.getId(), violationIds);
                    // Optional logging
                    System.out.println("Assigned " + updated + " violations to certificate " + saved.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateCertificatePdf(OcrSecurityCertificate cert, String path) {
        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage();
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);

            content.setFont(PDType1Font.HELVETICA_BOLD, 20);
            content.beginText();
            content.newLineAtOffset(50, 750);
            content.showText("Security Assessment Certificate");
            content.endText();

            content.setFont(PDType1Font.HELVETICA, 12);

            int y = 710;

            y = writeLine(content, "Agent ID: " + cert.getAgentId(), y);
            y = writeLine(content, "Device: " + cert.getUserDevice(), y);
            y = writeLine(content, "Threat Level: " + cert.getThreatLevel(), y);
            y = writeLine(content, "Threat Score: " + cert.getThreatScore(), y);
            y = writeLine(content, "Primary Context: " + cert.getPrimaryContext(), y);
            y = writeLine(content, "Total Violations: " + cert.getTotalViolations(), y);
            y = writeLine(content, "Risk Analysis: " + cert.getRiskAnalysis(), y);

            content.close();
            doc.save(path);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int writeLine(PDPageContentStream content, String text, int y) throws IOException {
        content.beginText();
        content.newLineAtOffset(50, y);
        content.showText(text);
        content.endText();
        return y - 20;
    }

    public OcrSecurityCertificate getLatestCertificate(Long agentId) {
        return certificateRepository.findTopByAgentIdOrderByCreatedAtDesc(agentId)
                .orElse(null);
    }

    public List<OcrSecurityCertificate> getAllCertificates(Long agentId) {
        return certificateRepository.findByAgentIdOrderByCreatedAtDesc(agentId);
    }

    // --- Helper methods ---

    private LocalDateTime parseDateTime(String isoString) {
        if (isoString == null || isoString.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(isoString).toLocalDateTime();
        } catch (DateTimeParseException e) {
            // fallback: try LocalDateTime
            try {
                return LocalDateTime.parse(isoString);
            } catch (DateTimeParseException ex) {
                return LocalDateTime.now();
            }
        }
    }

    private OcrDashboardStatsDTO toDashboardDTO(OcrStatus status) {
        OcrDashboardStatsDTO dto = new OcrDashboardStatsDTO();
        dto.setAgentId(status.getAgentId());
        dto.setAgentHostname(status.getAgentHostname());
        dto.setAgentUsername(status.getAgentUsername()); // Set username
        dto.setOcrEnabled(status.isOcrEnabled());
        // dto.setOcrCapable(status.isOcrCapable());
        dto.setCurrentThreatScore(status.getThreatScore() != null ? status.getThreatScore() : 0f);

        // ✅ NEW: Add threat arrow and trend color
        dto.setThreatArrow(status.getThreatArrow());
        dto.setTrendColor(status.getTrendColor());

        dto.setViolationsLast24h(status.getViolationsLast24h() != null ? status.getViolationsLast24h() : 0);
        dto.setLastScreenshotTime(status.getLastScreenshotTime());
        return dto;
    }

    // Add these methods to count OCR violations
    public Long countViolationsSince(LocalDateTime since) {
        return ocrViolationRepository.countByTimestampAfter(since);
    }

    public Long countViolationsBetween(LocalDateTime start, LocalDateTime end) {
        return ocrViolationRepository.countByTimestampBetween(start, end);
    }
}
