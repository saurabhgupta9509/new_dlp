package com.ma.dlp.controller;

import com.ma.dlp.Repository.OcrSecurityCertificateRepository;
import com.ma.dlp.Repository.OcrViolationRepository;
import com.ma.dlp.dto.ApiResponse;
import com.ma.dlp.dto.OcrDashboardStatsDTO;
import com.ma.dlp.dto.OcrDashboardSummaryDTO;
import com.ma.dlp.model.OcrLiveData;
import com.ma.dlp.model.OcrSecurityCertificate;
import com.ma.dlp.model.OcrViolation;
import com.ma.dlp.model.User;
import com.ma.dlp.service.OcrService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ocr")
public class OcrAdminController {

    @Autowired
    private OcrService ocrService;

    @Autowired
    private OcrViolationRepository ocrViolationRepository;

    @Autowired
    private OcrSecurityCertificateRepository ocrSecurityCertificateRepository;
    // Same logic as in AdminController.isAdminAuthenticated()
    private boolean isAdminAuthenticated(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    // === Dashboard: per-agent latest OCR status ===
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<OcrDashboardStatsDTO>>> getAllAgentOcrStatus(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        List<OcrDashboardStatsDTO> stats = ocrService.getLatestStatusForAllAgents();
        return ResponseEntity.ok(new ApiResponse<>(true, "OCR status retrieved", stats));
    }

    // Recent OCR frames for a specific agent
    @GetMapping("/live/{agentId}")
    public ResponseEntity<ApiResponse<List<OcrLiveData>>> getAgentLiveOcr(
            @PathVariable Long agentId,
            HttpSession session
    ) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        List<OcrLiveData> data = ocrService.getRecentLiveData(agentId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Live OCR data retrieved", data));
    }

    // All violations (or filter in frontend)
    @GetMapping("/violations/{agentId}")
    public ResponseEntity<ApiResponse<List<OcrViolation>>> getAgentViolations(
            @PathVariable Long agentId,
            HttpSession session
    ) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        List<OcrViolation> violations = ocrService.getViolations(agentId);
        return ResponseEntity.ok(new ApiResponse<>(true, "OCR violations retrieved", violations));
    }

    // @GetMapping("/violations/recent")
    // public ResponseEntity<ApiResponse<List<OcrViolation>>> getRecentViolations(
    //         HttpSession session,
    //         @RequestParam(defaultValue = "10") int limit) {
        
    //     if (!isAdminAuthenticated(session)) {
    //         return ResponseEntity.status(HttpStatus.FORBIDDEN)
    //                 .body(new ApiResponse<>(false, "Admin access required"));
    //     }

    //     // Fetch recent violations from all agents, ordered by timestamp
    //     List<OcrViolation> recentViolations = ocrViolationRepository
    //             .findRecentViolations(limit);
        
    //     return ResponseEntity.ok(new ApiResponse<>(true, "Recent violations retrieved", recentViolations));
    // }

    // Latest certificate per agent
    @GetMapping("/certificate/{agentId}/latest")
    public ResponseEntity<ApiResponse<OcrSecurityCertificate>> getLatestCertificate(
            @PathVariable Long agentId,
            HttpSession session
    ) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        OcrSecurityCertificate cert = ocrService.getLatestCertificate(agentId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Latest certificate retrieved", cert));
    }

    @GetMapping("/certificate/{agentId}/all")
    public ResponseEntity<ApiResponse<List<OcrSecurityCertificate>>> getAllCertificates(
            @PathVariable Long agentId,
            HttpSession session
    ) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        List<OcrSecurityCertificate> certificates = ocrService.getAllCertificates(agentId);
        return ResponseEntity.ok(new ApiResponse<>(true, "All certificates retrieved", certificates));
    }

    @GetMapping("/certificate/download")
    public ResponseEntity<?> downloadCertificate(@RequestParam String path, HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Admin access required");
        }

        try {
            File file = new File(path);
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + file.getName())
                    .contentLength(file.length())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error downloading certificate");
        }
    }


    @GetMapping("/certificate/view/{certificateId}")
    public ResponseEntity<ApiResponse<?>> viewCertificate(@PathVariable Long certificateId, HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        OcrSecurityCertificate cert = ocrSecurityCertificateRepository.findById(certificateId).orElse(null);
        if (cert == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Certificate not found"));

        List<OcrViolation> violations = ocrViolationRepository.findByCertificateIdOrderByTimestampDesc(certificateId);

        // Build response containing certificate + violations
        Map<String, Object> payload = new HashMap<>();
        payload.put("certificate", cert);
        payload.put("violations", violations);

        return ResponseEntity.ok(new ApiResponse<>(true, "Certificate with violations", payload));
    }

    @GetMapping("/certificate/download/{certificateId}")
    public ResponseEntity<?> downloadCertificateById(@PathVariable Long certificateId, HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        OcrSecurityCertificate cert = ocrSecurityCertificateRepository.findById(certificateId).orElse(null);
        if (cert == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Certificate not found");

        try {
            File file = new File(cert.getCertificateFilePath());
            if (!file.exists()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + file.getName())
                    .contentLength(file.length())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading file");
        }
    }


    @GetMapping("/summary")
    public ResponseEntity<OcrDashboardSummaryDTO> getOcrSummary() {
        return ResponseEntity.ok(ocrService.getOcrDashboardSummary());
    }

}
