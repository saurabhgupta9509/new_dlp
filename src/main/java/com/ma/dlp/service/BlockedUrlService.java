package com.ma.dlp.service;

import com.ma.dlp.Repository.BlockedUrlRepository;
import com.ma.dlp.dto.BlockedUrlRequest;
import com.ma.dlp.model.BlockedUrlEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BlockedUrlService {
    
    private static final Logger logger = LoggerFactory.getLogger(BlockedUrlService.class);
    
    private final BlockedUrlRepository blockedUrlRepository;
    private final PythonClientService pythonClientService;
    
    public BlockedUrlService(BlockedUrlRepository blockedUrlRepository, 
                            PythonClientService pythonClientService) {
        this.blockedUrlRepository = blockedUrlRepository;
        this.pythonClientService = pythonClientService;
    }
    
    // Get all blocked URLs (sorted by updated date)
    public List<BlockedUrlEntity> getAllBlockedUrls() {
        return blockedUrlRepository.findAllByOrderByUpdatedAtDesc();
    }
    
    // Get active blocked URLs
    public List<BlockedUrlEntity> getActiveBlockedUrls() {
        return blockedUrlRepository.findByActiveTrue();
    }
    
    // Get global blocked URLs
    public List<BlockedUrlEntity> getGlobalBlockedUrls() {
        return blockedUrlRepository.findByGlobalTrueAndActiveTrue();
    }
    
    // Get blocked URLs for a specific device
    public List<BlockedUrlEntity> getBlockedUrlsForDevice(String deviceId) {
        return blockedUrlRepository.findByDeviceIdAndActiveTrue(deviceId);
    }
    
    // Get blocked URLs for a specific user
    public List<BlockedUrlEntity> getBlockedUrlsForUser(String userId) {
        return blockedUrlRepository.findByUserIdAndActiveTrue(userId);
    }
    
    // Get all applicable blocked URLs for a device (global + device-specific)
    public List<BlockedUrlEntity> getApplicableBlockedUrls(String deviceId, String userId) {
        return blockedUrlRepository.findApplicableForDevice(deviceId, userId);
    }
    
    // Get blocked URLs as simple list of patterns
    public List<String> getBlockedUrlPatterns(String deviceId, String userId) {
        List<BlockedUrlEntity> blockedUrls = getApplicableBlockedUrls(deviceId, userId);
        return blockedUrls.stream()
                .map(BlockedUrlEntity::getUrlPattern)
                .collect(Collectors.toList());
    }
    
    // Add a single blocked URL
    @Transactional
    public BlockedUrlEntity addBlockedUrl(BlockedUrlRequest request, String addedBy) {
        if (blockedUrlRepository.existsByUrlPattern(request.getUrlPattern())) {
            throw new IllegalArgumentException("URL pattern already exists: " + request.getUrlPattern());
        }
        
        BlockedUrlEntity entity = new BlockedUrlEntity();
        entity.setUrlPattern(request.getUrlPattern());
        entity.setDomain(extractDomain(request.getUrlPattern()));
        entity.setReason(request.getReason());
        entity.setCategory(request.getCategory());
        entity.setActive(request.isActive());
        entity.setGlobal(request.getDeviceId() == null && request.getUserId() == null);
        entity.setDeviceId(request.getDeviceId());
        entity.setUserId(request.getUserId());
        entity.setAddedBy(addedBy);
        
        BlockedUrlEntity saved = blockedUrlRepository.save(entity);
        logger.info("✅ Blocked URL added: {} by {}", saved.getUrlPattern(), addedBy);
        
        // Notify affected devices if this is a global rule
        if (saved.isGlobal() && saved.isActive()) {
            notifyDevicesOfRuleUpdate();
        }
        
        return saved;
    }
    
    // Add multiple blocked URLs
    @Transactional
    public List<BlockedUrlEntity> addBlockedUrls(List<BlockedUrlRequest> requests, String addedBy) {
        List<BlockedUrlEntity> savedEntities = new ArrayList<>();
        
        for (BlockedUrlRequest request : requests) {
            try {
                // Check if already exists
                if (!blockedUrlRepository.existsByUrlPattern(request.getUrlPattern())) {
                    BlockedUrlEntity entity = new BlockedUrlEntity();
                    entity.setUrlPattern(request.getUrlPattern());
                    entity.setDomain(extractDomain(request.getUrlPattern()));
                    entity.setReason(request.getReason());
                    entity.setCategory(request.getCategory());
                    entity.setActive(request.isActive());
                    entity.setGlobal(request.getDeviceId() == null && request.getUserId() == null);
                    entity.setDeviceId(request.getDeviceId());
                    entity.setUserId(request.getUserId());
                    entity.setAddedBy(addedBy);
                    
                    BlockedUrlEntity saved = blockedUrlRepository.save(entity);
                    savedEntities.add(saved);
                    logger.info("✅ Blocked URL added: {}", saved.getUrlPattern());
                } else {
                    logger.warn("⚠️ Skipping duplicate URL pattern: {}", request.getUrlPattern());
                }
            } catch (Exception e) {
                logger.warn("⚠️ Failed to add blocked URL: {}", e.getMessage());
            }
        }
        
        if (!savedEntities.isEmpty()) {
            notifyDevicesOfRuleUpdate();
        }
        
        return savedEntities;
    }
    
    // Update a blocked URL
    @Transactional
    public BlockedUrlEntity updateBlockedUrl(Long id, BlockedUrlRequest request) {
        BlockedUrlEntity entity = blockedUrlRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blocked URL not found with id: " + id));
        
        boolean wasActive = entity.isActive();
        boolean wasGlobal = entity.isGlobal();
        
        entity.setUrlPattern(request.getUrlPattern());
        entity.setDomain(extractDomain(request.getUrlPattern()));
        entity.setReason(request.getReason());
        entity.setCategory(request.getCategory());
        entity.setActive(request.isActive());
        entity.setDeviceId(request.getDeviceId());
        entity.setUserId(request.getUserId());
        entity.setGlobal(request.getDeviceId() == null && request.getUserId() == null);
        entity.setUpdatedAt(LocalDateTime.now());
        
        BlockedUrlEntity updated = blockedUrlRepository.save(entity);
        logger.info("✅ Blocked URL updated: {}", updated.getUrlPattern());
        
        // Notify devices if the rule status changed
        if ((wasActive != updated.isActive()) || (wasGlobal != updated.isGlobal())) {
            notifyDevicesOfRuleUpdate();
        }
        
        return updated;
    }
    
    // Delete blocked URL
    @Transactional
    public void deleteBlockedUrl(Long id) {
        BlockedUrlEntity entity = blockedUrlRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blocked URL not found with id: " + id));
        
        boolean wasGlobal = entity.isGlobal();
        blockedUrlRepository.delete(entity);
        logger.info("🗑️ Blocked URL deleted: {}", entity.getUrlPattern());
        
        // Notify devices if this was a global rule
        if (wasGlobal) {
            notifyDevicesOfRuleUpdate();
        }
    }
    
    // Delete by patterns
    @Transactional
    public int deleteByPatterns(List<String> patterns) {
        int deletedCount = 0;
        for (String pattern : patterns) {
            try {
                blockedUrlRepository.deleteByUrlPattern(pattern);
                deletedCount++;
                logger.info("🗑️ Blocked URL deleted by pattern: {}", pattern);
            } catch (Exception e) {
                logger.warn("⚠️ Failed to delete blocked URL by pattern: {}", pattern);
            }
        }
        
        if (deletedCount > 0) {
            notifyDevicesOfRuleUpdate();
        }
        
        return deletedCount;
    }
    
    // Toggle blocked URL status
    @Transactional
    public BlockedUrlEntity toggleBlockedUrl(Long id, boolean active) {
        BlockedUrlEntity entity = blockedUrlRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blocked URL not found with id: " + id));
        
        entity.setActive(active);
        entity.setUpdatedAt(LocalDateTime.now());
        
        BlockedUrlEntity updated = blockedUrlRepository.save(entity);
        logger.info("🔄 Blocked URL {}: {}", active ? "activated" : "deactivated", updated.getUrlPattern());
        
        notifyDevicesOfRuleUpdate();
        
        return updated;
    }
    
    // Increment hit count when a URL is blocked
    @Transactional
    public void incrementHitCount(String urlPattern) {
        blockedUrlRepository.findByUrlPattern(urlPattern)
                .ifPresent(entity -> {
                    entity.incrementHitCount();
                    blockedUrlRepository.save(entity);
                });
    }
    
    // Get statistics
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<BlockedUrlEntity> allUrls = blockedUrlRepository.findAll();
        long totalUrls = allUrls.size();
        long activeUrls = allUrls.stream().filter(BlockedUrlEntity::isActive).count();
        long globalUrls = allUrls.stream().filter(BlockedUrlEntity::isGlobal).count();
        long deviceSpecificUrls = allUrls.stream()
                .filter(url -> url.getDeviceId() != null && !url.getDeviceId().isEmpty())
                .count();
        long userSpecificUrls = allUrls.stream()
                .filter(url -> url.getUserId() != null && !url.getUserId().isEmpty())
                .count();
        
        // Category breakdown
        Map<String, Long> categoryCount = allUrls.stream()
                .collect(Collectors.groupingBy(
                    url -> url.getCategory() != null ? url.getCategory() : "uncategorized",
                    Collectors.counting()
                ));
        
        // Total hits
        int totalHits = allUrls.stream()
                .mapToInt(BlockedUrlEntity::getHitCount)
                .sum();
        
        stats.put("totalUrls", totalUrls);
        stats.put("activeUrls", activeUrls);
        stats.put("globalUrls", globalUrls);
        stats.put("deviceSpecificUrls", deviceSpecificUrls);
        stats.put("userSpecificUrls", userSpecificUrls);
        stats.put("categoryBreakdown", categoryCount);
        stats.put("totalHits", totalHits);
        stats.put("lastUpdated", LocalDateTime.now());
        
        return stats;
    }
    
    // Get most blocked URLs (by hit count)
    public List<Map<String, Object>> getMostBlockedUrls(int limit) {
        return blockedUrlRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(b.getHitCount(), a.getHitCount()))
                .limit(limit)
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }
    
    // Get blocked URLs for a specific category
    public List<BlockedUrlEntity> getUrlsByCategory(String category) {
        return blockedUrlRepository.findByCategory(category);
    }
    
    // Search blocked URLs
    public List<BlockedUrlEntity> searchUrls(String keyword) {
        List<BlockedUrlEntity> allUrls = blockedUrlRepository.findAll();
        return allUrls.stream()
                .filter(url -> 
                    (url.getUrlPattern() != null && url.getUrlPattern().toLowerCase().contains(keyword.toLowerCase())) ||
                    (url.getDomain() != null && url.getDomain().toLowerCase().contains(keyword.toLowerCase())) ||
                    (url.getReason() != null && url.getReason().toLowerCase().contains(keyword.toLowerCase())) ||
                    (url.getCategory() != null && url.getCategory().toLowerCase().contains(keyword.toLowerCase()))
                )
                .collect(Collectors.toList());
    }
    
    // Notify all Python clients about rule updates
    private void notifyDevicesOfRuleUpdate() {
        // This will trigger devices to fetch updated rules on next heartbeat
        logger.info("🔄 Blocked URL rules updated. Devices will fetch new rules on next heartbeat.");
        
        // You could implement real-time push notifications here
        // For now, we'll rely on devices polling on heartbeat
    }
    
    // Extract domain from URL pattern
    private String extractDomain(String urlPattern) {
        if (urlPattern == null || urlPattern.trim().isEmpty()) {
            return "";
        }
        
        try {
            String pattern = urlPattern.trim().toLowerCase();
            
            // Remove protocol if present
            if (pattern.contains("://")) {
                pattern = pattern.split("://")[1];
            }
            
            // Remove path and query parameters
            pattern = pattern.split("/")[0];
            
            // Remove port if present
            pattern = pattern.split(":")[0];
            
            // Remove wildcards
            pattern = pattern.replace("*", "");
            
            return pattern;
        } catch (Exception e) {
            logger.warn("⚠️ Failed to extract domain from pattern: {}", urlPattern);
            return urlPattern;
        }
    }
    
    // Convert entity to map for response
    private Map<String, Object> convertToMap(BlockedUrlEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("urlPattern", entity.getUrlPattern());
        map.put("domain", entity.getDomain());
        map.put("reason", entity.getReason());
        map.put("category", entity.getCategory());
        map.put("active", entity.isActive());
        map.put("global", entity.isGlobal());
        map.put("deviceId", entity.getDeviceId());
        map.put("userId", entity.getUserId());
        map.put("addedBy", entity.getAddedBy());
        map.put("hitCount", entity.getHitCount());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }
}