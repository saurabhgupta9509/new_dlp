package com.ma.dlp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ma.dlp.dto.PhishingAlertRequest;
import com.ma.dlp.dto.PhishingAlertResponse;
import com.ma.dlp.service.PhishingAlertService;

@RestController
@RequestMapping("/api/phishing")
// Allow requests from Chrome extension
public class PhishingAlertController {
    
    private static final Logger logger = LoggerFactory.getLogger(PhishingAlertController.class);
    
    private final PhishingAlertService phishingAlertService;
    
    public PhishingAlertController(PhishingAlertService phishingAlertService) {
        this.phishingAlertService = phishingAlertService;
    }
    
    @PostMapping("/alert")
    public ResponseEntity<Map<String, Object>> receivePhishingAlert(@RequestBody PhishingAlertRequest request) {
        logger.info("📨 Received phishing alert from extension");
        logger.debug("Alert details - Sender: {}, Subject: {}, Score: {}", 
            request.getSender(), request.getSubject(), request.getMl_score());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            PhishingAlertResponse alertResponse = phishingAlertService.processPhishingAlert(request);
            
            response.put("success", true);
            response.put("message", "Alert processed successfully");
            response.put("alertId", alertResponse.getId());
            response.put("emailKey", alertResponse.getEmailKey());
            
            logger.info("✅ Phishing alert processed successfully");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Duplicate alert (cooldown)
            response.put("success", true);
            response.put("message", "Alert already processed recently (cooldown)");
            response.put("skipped", true);
            
            logger.info("⏸️ Alert skipped due to cooldown");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to process alert: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            logger.error("❌ Failed to process phishing alert: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/alerts")
    public ResponseEntity<List<PhishingAlertResponse>> getAllAlerts() {
        List<PhishingAlertResponse> alerts = phishingAlertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/alerts/{id}")
    public ResponseEntity<PhishingAlertResponse> getAlertById(@PathVariable Long id) {
        try {
            PhishingAlertResponse alert = phishingAlertService.getAlertById(id);
            return ResponseEntity.ok(alert);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/alerts/search")
    public ResponseEntity<List<PhishingAlertResponse>> searchAlerts(@RequestParam String query) {
        List<PhishingAlertResponse> alerts = phishingAlertService.searchAlerts(query);
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/alerts/source/{source}")
    public ResponseEntity<List<PhishingAlertResponse>> getAlertsBySource(@PathVariable String source) {
        List<PhishingAlertResponse> alerts = phishingAlertService.getAlertsBySource(source);
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/alerts/high-risk")
    public ResponseEntity<List<PhishingAlertResponse>> getHighRiskAlerts(
            @RequestParam(defaultValue = "7.0") Double threshold) {
        List<PhishingAlertResponse> alerts = phishingAlertService.getHighRiskAlerts(threshold);
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/alerts/recent")
    public ResponseEntity<List<PhishingAlertResponse>> getRecentAlerts(
            @RequestParam(defaultValue = "24") int hours) {
        List<PhishingAlertResponse> alerts = phishingAlertService.getRecentAlerts(hours);
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = phishingAlertService.getStatistics();
        return ResponseEntity.ok(stats);
    }
    
    @DeleteMapping("/alerts/{id}")
    public ResponseEntity<Map<String, Object>> deleteAlert(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            phishingAlertService.deleteAlert(id);
            response.put("success", true);
            response.put("message", "Alert deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete alert: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldAlerts(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            phishingAlertService.cleanupOldAlerts(days);
            response.put("success", true);
            response.put("message", "Cleanup completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Cleanup failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Phishing Alert Service");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }
}