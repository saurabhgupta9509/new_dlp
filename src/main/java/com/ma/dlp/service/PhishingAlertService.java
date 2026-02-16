package com.ma.dlp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ma.dlp.Repository.PhishingAlertRepository;
import com.ma.dlp.dto.PhishingAlertRequest;
import com.ma.dlp.dto.PhishingAlertResponse;
import com.ma.dlp.model.PhishingAlertEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PhishingAlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(PhishingAlertService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    
    private final PhishingAlertRepository phishingAlertRepository;
    
    public PhishingAlertService(PhishingAlertRepository phishingAlertRepository) {
        this.phishingAlertRepository = phishingAlertRepository;
    }
    
    @Transactional
    public PhishingAlertResponse processPhishingAlert(PhishingAlertRequest request) {
        // Generate unique email key
        String emailKey = generateEmailKey(request.getSender(), request.getSubject());
        
        logger.info("📥 Processing phishing alert: {}", emailKey);
        
        // Check if already exists (for cooldown)
        if (phishingAlertRepository.existsByEmailKey(emailKey)) {
            logger.info("⏸️ Duplicate alert detected, skipping: {}", emailKey);
            throw new IllegalArgumentException("Alert already processed recently");
        }
        
        // Convert to entity
        PhishingAlertEntity entity = convertToEntity(request, emailKey);
        
        // Save to database
        PhishingAlertEntity savedEntity = phishingAlertRepository.save(entity);
        logger.info("✅ Phishing alert saved to database with ID: {}", savedEntity.getId());
        
        return convertToResponse(savedEntity);
    }
    
    public List<PhishingAlertResponse> getAllAlerts() {
        return phishingAlertRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public PhishingAlertResponse getAlertById(Long id) {
        return phishingAlertRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found with id: " + id));
    }
    
    public List<PhishingAlertResponse> searchAlerts(String query) {
        List<PhishingAlertEntity> alerts = new ArrayList<>();
        
        // Search in sender
        alerts.addAll(phishingAlertRepository.findBySenderContainingIgnoreCase(query));
        
        // Search in subject
        alerts.addAll(phishingAlertRepository.findBySubjectContainingIgnoreCase(query));
        
        // Remove duplicates
        Set<Long> seenIds = new HashSet<>();
        List<PhishingAlertEntity> uniqueAlerts = alerts.stream()
                .filter(alert -> seenIds.add(alert.getId()))
                .collect(Collectors.toList());
        
        return uniqueAlerts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<PhishingAlertResponse> getAlertsBySource(String source) {
        return phishingAlertRepository.findBySource(source).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<PhishingAlertResponse> getHighRiskAlerts(Double threshold) {
        return phishingAlertRepository.findByMlScoreGreaterThan(threshold).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<PhishingAlertResponse> getRecentAlerts(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return phishingAlertRepository.findRecentAlerts(since).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<PhishingAlertEntity> allAlerts = phishingAlertRepository.findAll();
        long totalAlerts = allAlerts.size();
        
        // Count by source
        Map<String, Long> alertsBySource = allAlerts.stream()
                .collect(Collectors.groupingBy(
                    PhishingAlertEntity::getSource,
                    Collectors.counting()
                ));
        
        // Count by prediction
        Map<String, Long> alertsByPrediction = allAlerts.stream()
                .collect(Collectors.groupingBy(
                    PhishingAlertEntity::getMlPrediction,
                    Collectors.counting()
                ));
        
        // Average score
        Double averageScore = allAlerts.stream()
                .mapToDouble(PhishingAlertEntity::getMlScore)
                .average()
                .orElse(0.0);
        
        // Recent alerts (last 24 hours)
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        Long recentAlerts = phishingAlertRepository.countByDateRange(last24Hours, LocalDateTime.now());
        
        // Top senders
        List<Object[]> topSenders = phishingAlertRepository.countBySender();
        List<Map<String, Object>> topSenderList = topSenders.stream()
                .limit(10)
                .map(obj -> {
                    Map<String, Object> senderStat = new HashMap<>();
                    senderStat.put("sender", obj[0]);
                    senderStat.put("count", obj[1]);
                    return senderStat;
                })
                .collect(Collectors.toList());
        
        stats.put("totalAlerts", totalAlerts);
        stats.put("alertsBySource", alertsBySource);
        stats.put("alertsByPrediction", alertsByPrediction);
        stats.put("averageScore", averageScore);
        stats.put("recentAlerts24h", recentAlerts);
        stats.put("topSenders", topSenderList);
        stats.put("lastUpdated", LocalDateTime.now());
        
        return stats;
    }
    
    @Transactional
    public void deleteAlert(Long id) {
        if (!phishingAlertRepository.existsById(id)) {
            throw new IllegalArgumentException("Alert not found with id: " + id);
        }
        
        phishingAlertRepository.deleteById(id);
        logger.info("🗑️ Deleted phishing alert with ID: {}", id);
    }
    
    @Transactional
    public void cleanupOldAlerts(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<PhishingAlertEntity> oldAlerts = phishingAlertRepository.findByCreatedAtBefore(cutoffDate);
        
        if (!oldAlerts.isEmpty()) {
            phishingAlertRepository.deleteAll(oldAlerts);
            logger.info("🧹 Cleaned up {} old phishing alerts (older than {} days)", 
                oldAlerts.size(), days);
        }
    }
    
    private PhishingAlertEntity convertToEntity(PhishingAlertRequest request, String emailKey) {
        PhishingAlertEntity entity = new PhishingAlertEntity();
        
        entity.setSender(request.getSender() != null ? request.getSender() : "Unknown");
        entity.setReceiver(request.getReceiver() != null ? request.getReceiver() : "unknown");
        entity.setSubject(request.getSubject() != null ? request.getSubject() : "No Subject");
        entity.setBodyPreview(request.getBody_preview() != null ? request.getBody_preview() : "");
        
        // Parse timestamp
        if (request.getTimestamp() != null) {
            try {
                entity.setTimestamp(LocalDateTime.parse(request.getTimestamp(), formatter));
            } catch (Exception e) {
                entity.setTimestamp(LocalDateTime.now());
            }
        } else {
            entity.setTimestamp(LocalDateTime.now());
        }
        
        entity.setSource(request.getSource() != null ? request.getSource() : "unknown");
        entity.setMlScore(request.getMl_score() != null ? request.getMl_score() : 0.0);
        entity.setMlConfidence(request.getMl_confidence() != null ? request.getMl_confidence() : 0.0);
        entity.setMlPrediction(request.getMl_prediction() != null ? request.getMl_prediction() : "");
        entity.setDetectedAt(request.getDetected_at() != null ? request.getDetected_at() : System.currentTimeMillis());
        entity.setExtensionVersion(request.getExtension_version() != null ? request.getExtension_version() : "1.2.0");
        entity.setEmailKey(emailKey);
        entity.setWebhookSent(true);
        
        // Convert reasons list to JSON string
        if (request.getReasons() != null && !request.getReasons().isEmpty()) {
            try {
                entity.setReasons(objectMapper.writeValueAsString(request.getReasons()));
            } catch (JsonProcessingException e) {
                entity.setReasons("[]");
            }
        } else {
            entity.setReasons("[]");
        }
        
        return entity;
    }
    
    private PhishingAlertResponse convertToResponse(PhishingAlertEntity entity) {
        PhishingAlertResponse response = new PhishingAlertResponse();
        
        response.setId(entity.getId());
        response.setSender(entity.getSender());
        response.setReceiver(entity.getReceiver());
        response.setSubject(entity.getSubject());
        response.setBodyPreview(entity.getBodyPreview());
        response.setTimestamp(entity.getTimestamp());
        response.setSource(entity.getSource());
        response.setMlScore(entity.getMlScore());
        response.setMlConfidence(entity.getMlConfidence());
        response.setMlPrediction(entity.getMlPrediction());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setEmailKey(entity.getEmailKey());
        response.setWebhookSent(entity.isWebhookSent());
        
        // Parse reasons JSON
        if (entity.getReasons() != null && !entity.getReasons().isEmpty()) {
            try {
                String[] reasons = objectMapper.readValue(entity.getReasons(), String[].class);
                response.setReasons(reasons);
            } catch (JsonProcessingException e) {
                response.setReasons(new String[0]);
            }
        } else {
            response.setReasons(new String[0]);
        }
        
        return response;
    }
    
    private String generateEmailKey(String sender, String subject) {
        String safeSender = sender != null ? sender : "unknown";
        String safeSubject = subject != null ? subject : "no-subject";
        return safeSender + "-" + safeSubject;
    }
    
    // Add this method to your repository interface
    public interface CustomPhishingAlertRepository {
        List<PhishingAlertEntity> findByCreatedAtBefore(LocalDateTime date);
    }
}