package com.ma.dlp.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.ma.dlp.dto.*;
import com.ma.dlp.service.AgentService;
import com.ma.dlp.service.OcrService;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class OcrAgentController {

    private static final Logger log = LoggerFactory.getLogger(OcrAgentController.class);

    @Autowired
    private AgentService agentService;

    @Autowired
    private OcrService ocrService;

    // ====== SECURITY MONITOR REALTIME (used by Rust: send_realtime_update) ======


    @PostMapping("/{agentId}/security-monitor/realtime")
    public ResponseEntity<ApiResponse<String>> receiveRealtimeUpdate(
            @RequestHeader("Authorization") String token,
            @JsonProperty("agent_id")
            @PathVariable Long agentId,
            @RequestBody JsonNode realtimePayload
    ) {

        String cleanToken = agentService.cleanToken(token);
        log.info("🔐 Clean token: {}", cleanToken);
        log.info("🔐 Clean token length: {}", cleanToken.length());


        if (!agentService.validateToken(cleanToken, agentId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid token"));
        }

        // For now just log – later you can map parts to OCR tables if you want
        log.info("🔄 Realtime security update from agent {}: {}", agentId, realtimePayload.toString());

        // Example: if payload contains OCR status you can route it into ocrService here.

        return ResponseEntity.ok(new ApiResponse<>(true, "Realtime update received"));
    }

    @GetMapping("/{agentId}/security-monitor/status")
    public ResponseEntity<ApiResponse<String>> getSecurityMonitorStatus(
            @RequestHeader("Authorization") String token,
            @JsonProperty("agent_id")
            @PathVariable Long agentId
    ) {
        String cleanToken = agentService.cleanToken(token);
        log.info("🔐 Clean token: {}", cleanToken);
        log.info("🔐 Clean token length: {}", cleanToken.length());

        if (!agentService.validateToken(cleanToken, agentId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid token"));
        }

        // You can extend to return aggregate OCR status for this agent.
        return ResponseEntity.ok(new ApiResponse<>(true, "Security monitor status OK"));
    }

    // ====== PURE OCR ENDPOINTS ======

    @PostMapping("/ocr/status")
    public ResponseEntity<ApiResponse<String>> pushOcrStatus(
            @RequestHeader("Authorization") String token,
            @RequestBody OcrStatusUpdateDTO status
    ) {

        String cleanToken = agentService.cleanToken(token);
        log.info("🔐 Clean token: {}", cleanToken);
        log.info("🔐 Clean token length: {}", cleanToken.length());

        if (!agentService.validateToken(cleanToken, status.getAgentId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid token"));
        }

        ocrService.saveStatusFromAgent(status);
        return ResponseEntity.ok(new ApiResponse<>(true, "OCR status received"));
    }

    @PostMapping("/ocr/live")
    public ResponseEntity<ApiResponse<String>> pushOcrLiveData(
            @RequestHeader("Authorization") String token,
            @RequestBody OcrLiveDataDTO live
    ) {
        String cleanToken = agentService.cleanToken(token);
        log.info("🔐 Clean token: {}", cleanToken);
        log.info("🔐 Clean token length: {}", cleanToken.length());

        if (!agentService.validateToken(cleanToken, live.getAgentId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid token"));
        }

        ocrService.saveLiveDataFromAgent(live);
        return ResponseEntity.ok(new ApiResponse<>(true, "Live OCR data received"));
    }

    @PostMapping("/ocr/violation")
    public ResponseEntity<ApiResponse<Map<String , Object>>> pushOcrViolation(
            @RequestHeader("Authorization") String token,
            @RequestBody OcrViolationDTO violation
    ) {
        String cleanToken = agentService.cleanToken(token);
        log.info("🔐 Clean token: {}", cleanToken);
        log.info("🔐 Clean token length: {}", cleanToken.length());

        if (!agentService.validateToken(cleanToken, violation.getAgentId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid token"));
        }   

        Long savedId =  ocrService.saveViolationFromAgent(violation);

           // Return with ID field
             Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", savedId);
        
        return ResponseEntity.ok(ApiResponse.success("OCR violation received", responseData));
    }

//    @PostMapping("/ocr/certificate")
//    public ResponseEntity<ApiResponse<String>> pushSecurityCertificate(
//            @RequestHeader("Authorization") String token,
//            @RequestBody SecurityCertificateDTO certificate
//    ) {
//        String cleanToken = agentService.cleanToken(token);
//        log.info("🔐 Clean token: {}", cleanToken);
//        log.info("🔐 Clean token length: {}", cleanToken.length());
//
//        if (!agentService.validateToken(cleanToken, certificate.getAgentId())) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new ApiResponse<>(false, "Invalid token"));
//        }
//
//        ocrService.saveCertificateFromAgent(certificate);
//        return ResponseEntity.ok(new ApiResponse<>(true, "Security certificate received"));
//    }

    @PostMapping("/ocr/certificate")
    public ResponseEntity<ApiResponse<String>> pushSecurityCertificate(
            @RequestHeader("Authorization") String token,
            @RequestBody CertificateWithViolationsRequest request
    ) {
        String cleanToken = agentService.cleanToken(token);
        log.info("🔐 Clean token: {}", cleanToken);

        if (!agentService.validateToken(cleanToken, request.getCertificate().getAgentId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid token"));
        }

        ocrService.saveCertificateFromAgent(request.getCertificate(), request.getViolationIds());
        return ResponseEntity.ok(new ApiResponse<>(true, "Security certificate received"));
    }

}
