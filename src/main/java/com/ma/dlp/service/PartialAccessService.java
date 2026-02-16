package com.ma.dlp.service;

import com.ma.dlp.Repository.PartialAccessRepository;
import com.ma.dlp.dto.PartialAccessRequest;
import com.ma.dlp.model.PartialAccessEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PartialAccessService {
    
    private static final Logger logger = LoggerFactory.getLogger(PartialAccessService.class);
    private final PartialAccessRepository partialAccessRepository;
    private final ObjectMapper objectMapper;
    
    public PartialAccessService(PartialAccessRepository partialAccessRepository, ObjectMapper objectMapper) {
        this.partialAccessRepository = partialAccessRepository;
        this.objectMapper = objectMapper;
    }
    
    @Transactional
    public PartialAccessEntity addPartialAccessSite(PartialAccessRequest request, String addedBy) {
        PartialAccessEntity entity = new PartialAccessEntity();
        entity.setUrlPattern(request.getUrlPattern());
        entity.setDomain(request.getDomain());
        entity.setReason(request.getReason());
        entity.setCategory(request.getCategory());
        entity.setActive(request.isActive());
        entity.setGlobal(request.isGlobal());
        entity.setAllowUpload(request.isAllowUpload());
        entity.setAllowDownload(request.isAllowDownload());
        entity.setMonitorMode(request.getMonitorMode());
        entity.setAddedBy(addedBy);
        
        // Set device/user specific if not global
        if (!request.isGlobal()) {
            entity.setDeviceId(request.getDeviceId());
            entity.setUserId(request.getUserId());
        }
        
        // Convert restricted file types to JSON
        try {
            if (request.getRestrictedFileTypes() != null && !request.getRestrictedFileTypes().isEmpty()) {
                entity.setRestrictedFileTypes(objectMapper.writeValueAsString(request.getRestrictedFileTypes()));
            }
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize restricted file types: {}", e.getMessage());
        }
        
        return partialAccessRepository.save(entity);
    }
    
    @Transactional
    public List<PartialAccessEntity> addPartialAccessSites(List<PartialAccessRequest> requests, String addedBy) {
        List<PartialAccessEntity> entities = new ArrayList<>();
        
        for (PartialAccessRequest request : requests) {
            try {
                PartialAccessEntity entity = addPartialAccessSite(request, addedBy);
                entities.add(entity);
            } catch (Exception e) {
                logger.error("Failed to add partial access site {}: {}", request.getUrlPattern(), e.getMessage());
            }
        }
        
        return entities;
    }
    
    @Transactional
    public PartialAccessEntity updatePartialAccessSite(Long id, PartialAccessRequest request) {
        return partialAccessRepository.findById(id)
                .map(entity -> {
                    entity.setDomain(request.getDomain());
                    entity.setReason(request.getReason());
                    entity.setCategory(request.getCategory());
                    entity.setActive(request.isActive());
                    entity.setGlobal(request.isGlobal());
                    entity.setAllowUpload(request.isAllowUpload());
                    entity.setAllowDownload(request.isAllowDownload());
                    entity.setMonitorMode(request.getMonitorMode());
                    
                    if (!request.isGlobal()) {
                        entity.setDeviceId(request.getDeviceId());
                        entity.setUserId(request.getUserId());
                    } else {
                        entity.setDeviceId(null);
                        entity.setUserId(null);
                    }
                    
                    // Convert restricted file types to JSON
                    try {
                        if (request.getRestrictedFileTypes() != null && !request.getRestrictedFileTypes().isEmpty()) {
                            entity.setRestrictedFileTypes(objectMapper.writeValueAsString(request.getRestrictedFileTypes()));
                        } else {
                            entity.setRestrictedFileTypes(null);
                        }
                    } catch (JsonProcessingException e) {
                        logger.warn("Failed to serialize restricted file types: {}", e.getMessage());
                    }
                    
                    return partialAccessRepository.save(entity);
                })
                .orElseThrow(() -> new RuntimeException("Partial access site not found with id: " + id));
    }
    
    @Transactional
    public PartialAccessEntity togglePartialAccessSite(Long id, boolean active) {
        return partialAccessRepository.findById(id)
                .map(entity -> {
                    entity.setActive(active);
                    return partialAccessRepository.save(entity);
                })
                .orElseThrow(() -> new RuntimeException("Partial access site not found with id: " + id));
    }
    
    @Transactional
    public void deletePartialAccessSite(Long id) {
        if (!partialAccessRepository.existsById(id)) {
            throw new RuntimeException("Partial access site not found with id: " + id);
        }
        partialAccessRepository.deleteById(id);
    }
    
    @Transactional
    public int deleteByPatterns(List<String> patterns) {
        int deletedCount = 0;
        for (String pattern : patterns) {
            try {
                partialAccessRepository.deleteByUrlPattern(pattern);
                deletedCount++;
            } catch (Exception e) {
                logger.error("Failed to delete pattern {}: {}", pattern, e.getMessage());
            }
        }
        return deletedCount;
    }
    
    @Transactional
    public int deleteInactiveSites() {
        List<PartialAccessEntity> inactiveSites = partialAccessRepository.findAll().stream()
                .filter(site -> !site.isActive())
                .collect(Collectors.toList());
        
        partialAccessRepository.deleteAll(inactiveSites);
        return inactiveSites.size();
    }
    
    @Transactional
    public void resetAllAttempts() {
        partialAccessRepository.resetAllAttempts();
        logger.info("Reset all upload/download attempts for partial access sites");
    }
    
    public List<PartialAccessEntity> getAllPartialAccessSites() {
        return partialAccessRepository.findAllByOrderByUpdatedAtDesc();
    }
    
    public List<PartialAccessEntity> getActivePartialAccessSites() {
        return partialAccessRepository.findByActiveTrue();
    }
    
    public Optional<PartialAccessEntity> getPartialAccessSiteById(Long id) {
        return partialAccessRepository.findById(id);
    }
    
    public Optional<PartialAccessEntity> getPartialAccessSiteByPattern(String urlPattern) {
        return partialAccessRepository.findByUrlPattern(urlPattern);
    }
    
    public List<PartialAccessEntity> getPartialAccessForDevice(String deviceId, String userId) {
        return partialAccessRepository.findApplicableForDevice(deviceId, userId);
    }
    
    public List<String> getPartialAccessPatterns(String deviceId, String userId) {
        List<PartialAccessEntity> sites = getPartialAccessForDevice(deviceId, userId);
        return sites.stream()
                .map(PartialAccessEntity::getUrlPattern)
                .toList();
    }
    
    public Map<String, Object> getPartialAccessConfig(String deviceId, String userId) {
        List<PartialAccessEntity> sites = getPartialAccessForDevice(deviceId, userId);
        
        Map<String, Object> config = new HashMap<>();
        List<Map<String, Object>> sitesList = new ArrayList<>();
        
        for (PartialAccessEntity site : sites) {
            Map<String, Object> siteConfig = new HashMap<>();
            siteConfig.put("urlPattern", site.getUrlPattern());
            siteConfig.put("domain", site.getDomain());
            siteConfig.put("allowUpload", site.isAllowUpload());
            siteConfig.put("allowDownload", site.isAllowDownload());
            siteConfig.put("monitorMode", site.getMonitorMode());
            siteConfig.put("active", site.isActive());
            
            // Parse restricted file types
            try {
                if (site.getRestrictedFileTypes() != null && !site.getRestrictedFileTypes().isEmpty()) {
                    List<String> fileTypes = objectMapper.readValue(
                        site.getRestrictedFileTypes(), 
                        new TypeReference<List<String>>() {}
                    );
                    siteConfig.put("restrictedFileTypes", fileTypes);
                } else {
                    siteConfig.put("restrictedFileTypes", new ArrayList<>());
                }
            } catch (Exception e) {
                siteConfig.put("restrictedFileTypes", new ArrayList<>());
            }
            
            sitesList.add(siteConfig);
        }
        
        config.put("partialAccessSites", sitesList);
        config.put("fetchedAt", LocalDateTime.now().toString());
        config.put("totalSites", sitesList.size());
        
        return config;
    }
    
    public List<PartialAccessEntity> searchPartialAccessSites(String keyword) {
        return partialAccessRepository.searchByKeyword(keyword);
    }
    
    public List<PartialAccessEntity> getPartialAccessByCategory(String category) {
        return partialAccessRepository.findByCategoryAndActiveTrue(category);
    }
    
    public List<PartialAccessEntity> getMostAttemptedSites(int limit) {
        return partialAccessRepository.findByMostAttempts().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void incrementUploadAttempt(String urlPattern) {
        partialAccessRepository.findByUrlPattern(urlPattern).ifPresent(entity -> {
            entity.setUploadAttempts(entity.getUploadAttempts() + 1);
            partialAccessRepository.save(entity);
            logger.info("📤 Upload attempt recorded for: {}", urlPattern);
        });
    }
    
    @Transactional
    public void incrementDownloadAttempt(String urlPattern) {
        partialAccessRepository.findByUrlPattern(urlPattern).ifPresent(entity -> {
            entity.setDownloadAttempts(entity.getDownloadAttempts() + 1);
            partialAccessRepository.save(entity);
            logger.info("📥 Download attempt recorded for: {}", urlPattern);
        });
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalSites = partialAccessRepository.count();
        long activeSites = partialAccessRepository.countActive();
        Long totalUploadAttempts = partialAccessRepository.sumUploadAttempts();
        Long totalDownloadAttempts = partialAccessRepository.sumDownloadAttempts();
        
        stats.put("totalSites", totalSites);
        stats.put("activeSites", activeSites);
        stats.put("inactiveSites", totalSites - activeSites);
        stats.put("totalUploadAttempts", totalUploadAttempts != null ? totalUploadAttempts : 0);
        stats.put("totalDownloadAttempts", totalDownloadAttempts != null ? totalDownloadAttempts : 0);
        
        // Category breakdown
        List<Object[]> categoryCounts = partialAccessRepository.countByCategory();
        Map<String, Long> categoryStats = new HashMap<>();
        for (Object[] result : categoryCounts) {
            String category = (String) result[0];
            Long count = (Long) result[1];
            categoryStats.put(category, count);
        }
        stats.put("categoryBreakdown", categoryStats);
        
        // Top 5 sites by attempts
        List<Map<String, Object>> topAttempted = getMostAttemptedSites(5).stream()
                .map(site -> {
                    Map<String, Object> siteMap = new HashMap<>();
                    siteMap.put("urlPattern", site.getUrlPattern());
                    siteMap.put("domain", site.getDomain());
                    siteMap.put("uploadAttempts", site.getUploadAttempts());
                    siteMap.put("downloadAttempts", site.getDownloadAttempts());
                    return siteMap;
                })
                .collect(Collectors.toList());
        stats.put("topAttemptedSites", topAttempted);
        
        // Mode distribution
        List<PartialAccessEntity> allSites = getAllPartialAccessSites();
        Map<String, Long> modeDistribution = allSites.stream()
                .collect(Collectors.groupingBy(
                    PartialAccessEntity::getMonitorMode,
                    Collectors.counting()
                ));
        stats.put("modeDistribution", modeDistribution);
        
        stats.put("generatedAt", LocalDateTime.now().toString());
        
        return stats;
    }
    
    public boolean isPartialAccessSite(String url, String deviceId, String userId) {
        List<PartialAccessEntity> sites = getPartialAccessForDevice(deviceId, userId);
        
        for (PartialAccessEntity site : sites) {
            String pattern = site.getUrlPattern().toLowerCase();
            String urlLower = url.toLowerCase();
            
            // Simple pattern matching
            if (pattern.contains("*")) {
                // Convert wildcard pattern to regex
                String regexPattern = pattern.replace(".", "\\.")
                                            .replace("*", ".*");
                if (urlLower.matches(regexPattern)) {
                    return true;
                }
            } else if (urlLower.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isUploadAllowed(String url, String deviceId, String userId) {
        List<PartialAccessEntity> sites = getPartialAccessForDevice(deviceId, userId);
        
        for (PartialAccessEntity site : sites) {
            String pattern = site.getUrlPattern().toLowerCase();
            String urlLower = url.toLowerCase();
            
            // Check if URL matches pattern
            if ((pattern.contains("*") && urlLower.matches(pattern.replace(".", "\\.").replace("*", ".*"))) ||
                urlLower.contains(pattern)) {
                return site.isAllowUpload();
            }
        }
        
        // If site not in partial access list, upload is allowed
        return true;
    }
    
    public boolean isDownloadAllowed(String url, String deviceId, String userId) {
        List<PartialAccessEntity> sites = getPartialAccessForDevice(deviceId, userId);
        
        for (PartialAccessEntity site : sites) {
            String pattern = site.getUrlPattern().toLowerCase();
            String urlLower = url.toLowerCase();
            
            // Check if URL matches pattern
            if ((pattern.contains("*") && urlLower.matches(pattern.replace(".", "\\.").replace("*", ".*"))) ||
                urlLower.contains(pattern)) {
                return site.isAllowDownload();
            }
        }
        
        // If site not in partial access list, download is allowed
        return true;
    }
    
    public List<String> getRestrictedFileTypes(String url, String deviceId, String userId) {
        List<PartialAccessEntity> sites = getPartialAccessForDevice(deviceId, userId);
        
        for (PartialAccessEntity site : sites) {
            String pattern = site.getUrlPattern().toLowerCase();
            String urlLower = url.toLowerCase();
            
            // Check if URL matches pattern
            if ((pattern.contains("*") && urlLower.matches(pattern.replace(".", "\\.").replace("*", ".*"))) ||
                urlLower.contains(pattern)) {
                
                try {
                    if (site.getRestrictedFileTypes() != null && !site.getRestrictedFileTypes().isEmpty()) {
                        return objectMapper.readValue(
                            site.getRestrictedFileTypes(), 
                            new TypeReference<List<String>>() {}
                        );
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse restricted file types for {}: {}", url, e.getMessage());
                }
                
                return new ArrayList<>();
            }
        }
        
        return new ArrayList<>();
    }
    
    public String getMonitorMode(String url, String deviceId, String userId) {
        List<PartialAccessEntity> sites = getPartialAccessForDevice(deviceId, userId);
        
        for (PartialAccessEntity site : sites) {
            String pattern = site.getUrlPattern().toLowerCase();
            String urlLower = url.toLowerCase();
            
            // Check if URL matches pattern
            if ((pattern.contains("*") && urlLower.matches(pattern.replace(".", "\\.").replace("*", ".*"))) ||
                urlLower.contains(pattern)) {
                return site.getMonitorMode();
            }
        }
        
        return "allow"; // Default to allow if not in partial access list
    }
}