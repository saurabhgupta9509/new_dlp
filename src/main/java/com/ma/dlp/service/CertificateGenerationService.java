// CertificateGenerationService.java
package com.ma.dlp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ma.dlp.Repository.PythonCertificateRepository;
import com.ma.dlp.Repository.PythonUrlDataRepository;
import com.ma.dlp.model.PythonCertificate;
import com.ma.dlp.model.PythonUrlData;

@Service
public class CertificateGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateGenerationService.class);
    
    private final PythonUrlDataRepository pythonUrlDataRepository;
    private final PythonCertificateRepository pythonCertificateRepository;
    private final PythonClientService pythonClientService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${python.model.api.url:http://localhost:5000/predict}")
    private String modelApiUrl;

    public CertificateGenerationService(
            PythonUrlDataRepository pythonUrlDataRepository,
            PythonCertificateRepository pythonCertificateRepository,
            PythonClientService pythonClientService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.pythonUrlDataRepository = pythonUrlDataRepository;
        this.pythonCertificateRepository = pythonCertificateRepository;
        this.pythonClientService = pythonClientService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Run every 5 minutes
    @Scheduled(fixedRate = 300000) // 300000 ms = 5 minutes
    @Transactional
    public void generateCertificatesFromRecentUrls() {
        try {
            logger.info("🔄 Starting certificate generation from recent URLs...");
            
            // Get URLs from last 2 minutes
            LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(1440); // set to 24 hours for testing
            
            // Get all devices
            List<String> deviceIds = pythonClientService.getAllDevices().stream()
                    .map(device -> device.getDeviceId())
                    .collect(Collectors.toList());
            
            for (String deviceId : deviceIds) {
                try {
                    generateCertificateForDevice(deviceId, twoMinutesAgo);
                } catch (Exception e) {
                    logger.error("❌ Failed to generate certificate for device {}: {}", deviceId, e.getMessage());
                }
            }
            
            logger.info("✅ Certificate generation completed");
            
        } catch (Exception e) {
            logger.error("❌ Certificate generation failed: {}", e.getMessage(), e);
        }
    }

    // Update the generateCertificateForDevice method

private void generateCertificateForDevice(String deviceId, LocalDateTime sinceTime) {
    // Get recent URL data for this device
    List<PythonUrlData> recentUrlData = pythonUrlDataRepository
            .findByDeviceIdOrderByTimestampDesc(deviceId).stream()
            .filter(data -> data.getTimestamp() != null && data.getTimestamp().isAfter(sinceTime))
            .collect(Collectors.toList());
    
    List<String> allUrls = new ArrayList<>();
    
    if (!recentUrlData.isEmpty()) {
        // Collect all URLs from recent data
        for (PythonUrlData urlData : recentUrlData) {
            try {
                if (urlData.getUrls() != null && !urlData.getUrls().isEmpty()) {
                    List<String> urls = objectMapper.readValue(urlData.getUrls(), List.class);
                    allUrls.addAll(urls);
                }
            } catch (Exception e) {
                logger.warn("Failed to parse URLs for device {}: {}", deviceId, e.getMessage());
            }
        }
        
        // Remove duplicates
        List<String> uniqueUrls = allUrls.stream()
                .distinct()
                .collect(Collectors.toList());
        
        logger.info("📊 Found {} unique URLs for device {} from last {} minutes", 
                uniqueUrls.size(), deviceId, 
                java.time.Duration.between(sinceTime, LocalDateTime.now()).toMinutes());
        
        if (!uniqueUrls.isEmpty()) {
            // Call the Python model
            Map<String, Object> modelResponse = callPythonModel(uniqueUrls);
            
            if (modelResponse != null) {
                // Create certificate from model response
                PythonCertificate certificate = createCertificateFromModelResponse(deviceId, modelResponse, uniqueUrls);
                pythonCertificateRepository.save(certificate);
                
                logger.info("📜 Certificate generated for device {}: {} (based on {} URLs)", 
                        deviceId, certificate.getCertificateId(), uniqueUrls.size());
                return;
            }
        }
    }
    
    // If no recent URLs or model failed, create a baseline certificate
    createBaselineCertificate(deviceId);
}

private void createBaselineCertificate(String deviceId) {
    logger.info("📝 Creating baseline certificate for device {} (no recent URL data)", deviceId);
    
    PythonCertificate certificate = new PythonCertificate();
    certificate.setDeviceId(deviceId);
    certificate.setCertificateId("CERT-" + UUID.randomUUID().toString().substring(0, 8));
    certificate.setUploadedAt(LocalDateTime.now());
    certificate.setGenerated(LocalDateTime.now().toString());
    certificate.setValidUntil(LocalDateTime.now().plusDays(7).toString());
    certificate.setIssuer("AI Security Monitor");
    certificate.setRecipient("Device Owner");
    certificate.setDevice("Python Device " + deviceId.substring(0, 8));
    
    // Create baseline security metrics
    Map<String, Object> securityMetrics = new HashMap<>();
    securityMetrics.put("overall_score", 85.0); // Default good score
    securityMetrics.put("grade", "B");
    securityMetrics.put("threat_level", "low");
    securityMetrics.put("safe_urls", 0);
    securityMetrics.put("risky_urls", 0);
    securityMetrics.put("suspicious_urls", 0);
    securityMetrics.put("total_urls_analyzed", 0);
    securityMetrics.put("analysis_date", LocalDateTime.now().toString());
    securityMetrics.put("model_confidence", "low");
    securityMetrics.put("checks_passed", 0);
    securityMetrics.put("checks_total", 0);
    securityMetrics.put("note", "No recent URL activity detected");
    certificate.setSecurityMetrics(securityMetrics);
    
    // Create detailed analysis
    Map<String, Object> detailedAnalysis = new HashMap<>();
    detailedAnalysis.put("analysis_method", "baseline_assessment");
    detailedAnalysis.put("reason", "No recent URL data available for analysis");
    detailedAnalysis.put("recommendation", "Monitor device activity for URL data");
    detailedAnalysis.put("timestamp", LocalDateTime.now().toString());
    certificate.setDetailedAnalysis(detailedAnalysis);
    
    // Create recommendations
    List<Map<String, Object>> recommendations = new ArrayList<>();
    
    Map<String, Object> rec1 = new HashMap<>();
    rec1.put("priority", "medium");
    rec1.put("action", "Monitor URL activity");
    rec1.put("description", "No recent URL data found. Ensure the Python client is sending URL data.");
    recommendations.add(rec1);
    
    Map<String, Object> rec2 = new HashMap<>();
    rec2.put("priority", "low");
    rec2.put("action", "Check device connectivity");
    rec2.put("description", "Verify the device is properly connected and sending data.");
    recommendations.add(rec2);
    
    certificate.setRecommendations(recommendations);
    
    // Create signature
    Map<String, Object> signature = new HashMap<>();
    signature.put("algorithm", "SHA256");
    signature.put("generated_by", "Baseline Assessment");
    signature.put("timestamp", LocalDateTime.now().toString());
    signature.put("model_version", "1.0");
    certificate.setSignature(signature);
    
    pythonCertificateRepository.save(certificate);
    logger.info("✅ Baseline certificate created for device: {}", deviceId);
}
    // Update the callPythonModel method in CertificateGenerationService.java

    private Map<String, Object> callPythonModel(List<String> urls) {
        try {
            // Prepare request
            Map<String, Object> request = new HashMap<>();
            request.put("urls", urls);
            request.put("timestamp", LocalDateTime.now().toString());
            
            // Call Python model API
            logger.info("🤖 Calling Python model API with {} URLs", urls.size());
            
            try {
                Map<String, Object> response = restTemplate.postForObject(modelApiUrl, request, Map.class);
                
                if (response != null && response.get("success") != null && (Boolean) response.get("success")) {
                    return (Map<String, Object>) response.get("data");
                } else {
                    logger.warn("⚠️ Model API returned unsuccessful response: {}", response);
                    return createDefaultModelResponse(urls);
                }
            } catch (ResourceAccessException e) {
                logger.warn("🌐 Python model API not reachable at {}. Using fallback analysis.", modelApiUrl);
                return createDefaultModelResponse(urls);
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to call Python model API: {}", e.getMessage());
            // Return default response if model fails
            return createDefaultModelResponse(urls);
        }
    }
    private Map<String, Object> createDefaultModelResponse(List<String> urls) {
        Map<String, Object> response = new HashMap<>();
        
        // Analyze URLs locally as fallback
        int safeUrls = 0;
        int suspiciousUrls = 0;
        int maliciousUrls = 0;
        
        for (String url : urls) {
            String analysis = analyzeUrlLocally(url);
            switch (analysis) {
                case "safe": safeUrls++; break;
                case "suspicious": suspiciousUrls++; break;
                case "malicious": maliciousUrls++; break;
                default: safeUrls++;
            }
        }
        
        int totalUrls = urls.size();
        double safetyScore = totalUrls > 0 ? 
                (double) safeUrls / totalUrls * 100.0 : 
                0.0;
        
        // Build response
        Map<String, Object> securityMetrics = new HashMap<>();
        securityMetrics.put("overall_score", Math.round(safetyScore));
        securityMetrics.put("grade", calculateGrade(safetyScore));
        securityMetrics.put("threat_level", calculateThreatLevel(safetyScore));
        securityMetrics.put("safe_urls", safeUrls);
        securityMetrics.put("suspicious_urls", suspiciousUrls);
        securityMetrics.put("malicious_urls", maliciousUrls);
        securityMetrics.put("total_urls_analyzed", totalUrls);
        securityMetrics.put("analysis_date", LocalDateTime.now().toString());
        
        Map<String, Object> detailedAnalysis = new HashMap<>();
        detailedAnalysis.put("analysis_method", "local_fallback");
        detailedAnalysis.put("url_sample", urls.size() > 5 ? urls.subList(0, 5) : urls);
        
        List<Map<String, Object>> recommendations = new ArrayList<>();
        if (safetyScore < 70) {
            Map<String, Object> rec = new HashMap<>();
            rec.put("priority", "high");
            rec.put("action", "Review browsing habits");
            rec.put("description", "High number of suspicious URLs detected");
            recommendations.add(rec);
        }
        
        response.put("securityMetrics", securityMetrics);
        response.put("detailedAnalysis", detailedAnalysis);
        response.put("recommendations", recommendations);
        
        return response;
    }

    private String analyzeUrlLocally(String url) {
        // Simple local analysis (can be enhanced)
        url = url.toLowerCase();
        
        if (url.contains("phishing") || url.contains("malware") || url.contains("hack")) {
            return "malicious";
        } else if (url.contains("suspicious") || url.contains("weird") || url.contains("unusual")) {
            return "suspicious";
        } else {
            return "safe";
        }
    }

    private String calculateGrade(double score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        return "F";
    }

    private String calculateThreatLevel(double score) {
        if (score >= 80) return "low";
        if (score >= 60) return "medium";
        return "high";
    }

    private PythonCertificate createCertificateFromModelResponse(
        String deviceId, 
        Map<String, Object> modelResponse,
        List<String> analyzedUrls) {
    
    PythonCertificate certificate = new PythonCertificate();
    certificate.setDeviceId(deviceId);
    certificate.setCertificateId("CERT-" + UUID.randomUUID().toString().substring(0, 8));
    certificate.setUploadedAt(LocalDateTime.now());
    certificate.setGenerated(LocalDateTime.now().toString());
    certificate.setValidUntil(LocalDateTime.now().plusDays(7).toString());
    certificate.setIssuer("AI Security Monitor");
    certificate.setRecipient("Device Owner");
    certificate.setDevice("Python Device " + deviceId.substring(0, 8));
    
    // Set security metrics
    Map<String, Object> securityMetrics = (Map<String, Object>) modelResponse.get("securityMetrics");
    certificate.setSecurityMetrics(securityMetrics);
    
    // Set detailed analysis with all URL results
    Map<String, Object> detailedAnalysis = (Map<String, Object>) modelResponse.getOrDefault(
            "detailedAnalysis", new HashMap<>());
    detailedAnalysis.put("analyzed_urls_count", analyzedUrls.size());
    detailedAnalysis.put("analysis_timestamp", LocalDateTime.now().toString());
    
    // Ensure detailed_url_results is included
    if (!detailedAnalysis.containsKey("detailed_url_results")) {
        // Create simplified detailed results from sample_results
        List<Map<String, Object>> sampleResults = (List<Map<String, Object>>) 
                detailedAnalysis.getOrDefault("sample_results", new ArrayList<>());
        detailedAnalysis.put("detailed_url_results", sampleResults);
    }
    
    certificate.setDetailedAnalysis(detailedAnalysis);
    
    // Set recommendations
    List<Map<String, Object>> recommendations = (List<Map<String, Object>>) 
            modelResponse.getOrDefault("recommendations", new ArrayList<>());
    certificate.setRecommendations(recommendations);
    
    // Create signature
    Map<String, Object> signature = new HashMap<>();
    signature.put("algorithm", "SHA256");
    signature.put("generated_by", "AI Model");
    signature.put("timestamp", LocalDateTime.now().toString());
    signature.put("model_version", "1.0");
    certificate.setSignature(signature);
    
    return certificate;
}
    // Manual trigger for certificate generation
    public void generateCertificateForDeviceNow(String deviceId) {
        generateCertificateForDevice(deviceId, LocalDateTime.now().minusHours(24)); // Last 24 hours
    }
}