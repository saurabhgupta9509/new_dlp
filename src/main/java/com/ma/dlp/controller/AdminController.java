package com.ma.dlp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ma.dlp.Repository.*;
import com.ma.dlp.dto.*;
import com.ma.dlp.model.*;
import com.ma.dlp.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// done
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private OcrService ocrService;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private FileEventLogRepository fileEventLogRepository;

    @Autowired
    private EventLogRepository eventLogRepository;

    @Autowired
    private WebHistoryLogRepository webHistoryLogRepository;

    @Autowired
    private AppUsageLogRepository appUsageLogRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AgentCommandRepository agentCommandRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private USBActivityRepository usbActivityRepository;

    @Autowired
    private BlockedUrlService blockedUrlService;

    @Autowired
    private PartialAccessService partialAccessService;

    @Autowired
    private PythonClientService pythonClientService;

    @Autowired
    private CertificateGenerationService certificateGenerationService;


    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    @GetMapping("/agents/{agentId}/browse-result")
    public ResponseEntity<ApiResponse<FileBrowseResponseDTO>> getBrowseResult(
            @PathVariable(name = "agentId") Long agentId) {

        FileBrowseResponseDTO response = agentService.getBrowseResponse(agentId);

        if (response == null) {
            return ResponseEntity.ok(
                    new ApiResponse<FileBrowseResponseDTO>(false, "No data yet", null));
        }

        return ResponseEntity.ok(
                new ApiResponse<FileBrowseResponseDTO>(true, "Success", response));
    }

    // In AdminController.java - update the createAgent endpoint
    @PostMapping("/agents/create")
    public ResponseEntity<ApiResponse<AgentAuthResponse>> createAgent(@RequestBody CreateUpdateAgentRequest request,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // ✅ Pass the custom password to the service method
            AgentAuthResponse response = agentService.createAgentDirectly(null, null, request.getUsername(),
                    request.getPassword(), null, request.getEmail() // ✅ Pass the custom password
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "Agent created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to create agent: " + e.getMessage()));
        }
    }

   @PutMapping("/agents/{id}")
public ResponseEntity<ApiResponse<Map<String, Object>>> updateAgent(
        @PathVariable Long id,
        @RequestBody CreateUpdateAgentRequest request,
        HttpSession session) {

    if (!isAdminAuthenticated(session)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Admin access required", null));
    }

    try {
        User updatedUser = agentService.updateAgent(
                id,
                request.getUsername(),
                request.getPassword(),
                request.getEmail());

        // Return ONLY the updated fields, not the full object with relationships
        Map<String, Object> response = new HashMap<>();
        response.put("id", updatedUser.getId());
        response.put("username", updatedUser.getUsername());
        response.put("email", updatedUser.getEmail());
        response.put("message", "Agent updated successfully");

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Agent updated successfully", response));
    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Failed to update agent: " + e.getMessage(), null));
    }
}

   @GetMapping("/agents/{agentId}/blocked-urls")
public ResponseEntity<ApiResponse<List<BlockedUrlEntity>>> getAgentBlockedUrls(
        @PathVariable Long agentId,
        HttpSession session) {
    
    if (!isAdminAuthenticated(session)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Admin access required", null));
    }

    try {
        // Get agent details
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        
        // Get device ID (MAC address or hostname)
        String deviceId = agent.getMacAddress();
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = agent.getHostname();
        }
        
        // Get applicable blocked URLs (global + device-specific)
        List<BlockedUrlEntity> blockedUrls = blockedUrlService.getApplicableBlockedUrls(
                deviceId, 
                String.valueOf(agentId)
        );
        
        log.info("📋 Retrieved {} blocked URLs for agent {}", blockedUrls.size(), agentId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                "Blocked URLs retrieved successfully", 
                blockedUrls));
                
    } catch (Exception e) {
        log.error("❌ Failed to get blocked URLs for agent {}: {}", agentId, e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, 
                        "Failed to get blocked URLs: " + e.getMessage(), 
                        null));
    }
}

/**
 * Get all applicable blocked URLs for a device/user
 * This combines global rules + device-specific + user-specific rules
 */
// public List<BlockedUrlEntity> getApplicableBlockedUrls(String deviceId, String userId) {
//     List<BlockedUrlEntity> result = new ArrayList<>();
    
//     // Get global active URLs
//     result.addAll(blockedUrlRepository.findByGlobalTrueAndActiveTrue());
    
//     // Get device-specific URLs if deviceId provided
//     if (deviceId != null && !deviceId.isEmpty()) {
//         result.addAll(blockedUrlRepository.findByDeviceIdAndActiveTrue(deviceId));
//     }
    
//     // Get user-specific URLs if userId provided
//     if (userId != null && !userId.isEmpty()) {
//         result.addAll(blockedUrlRepository.findByUserIdAndActiveTrue(userId));
//     }
    
//     // Remove duplicates (by ID)
//     return result.stream()
//             .distinct()
//             .sorted((a, b) -> {
//                 if (a.getUpdatedAt() == null) return -1;
//                 if (b.getUpdatedAt() == null) return 1;
//                 return b.getUpdatedAt().compareTo(a.getUpdatedAt());
//             })
//             .collect(Collectors.toList());
// }


   /**
 * Add a single partial access site
 */
@PostMapping("/partial-access")
public ResponseEntity<ApiResponse<PartialAccessEntity>> addPartialAccess(
        @RequestBody PartialAccessRequest request,
        HttpSession session) {
    
    if (!isAdminAuthenticated(session)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Admin access required", null));
    }
    
    try {
        User admin = (User) session.getAttribute("currentUser");
        
        // Set default values if not provided
        if (request.getCategory() == null) request.setCategory("Other");
        if (request.getMonitorMode() == null) request.setMonitorMode("block");
        
        PartialAccessEntity saved = partialAccessService.addPartialAccessSite(request, admin.getUsername());
        log.info("✅ Partial access site added: {} by {}", request.getUrlPattern(), admin.getUsername());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Site added successfully", saved));
    } catch (Exception e) {
        log.error("❌ Failed to add partial access site: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to add site: " + e.getMessage(), null));
    }
}


    // --- Blocked URLs Admin Logic ---

    @GetMapping("/blocked-urls")
    public ResponseEntity<ApiResponse<List<BlockedUrlEntity>>> getAllBlockedUrls(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "All blocked URLs retrieved",
                blockedUrlService.getAllBlockedUrls()));
    }

    @PostMapping("/blocked-urls/bulk")
    public ResponseEntity<ApiResponse<List<BlockedUrlEntity>>> addBlockedUrlsBulk(
            @RequestBody List<BlockedUrlRequest> requests,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        User admin = (User) session.getAttribute("currentUser");
        return ResponseEntity.ok(new ApiResponse<>(true, "Blocked URLs added successfully",
                blockedUrlService.addBlockedUrls(requests, admin.getUsername())));
    }

    @PutMapping("/blocked-urls/{id}")
    public ResponseEntity<ApiResponse<BlockedUrlEntity>> updateBlockedUrl(
            @PathVariable Long id,
            @RequestBody BlockedUrlRequest request,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Blocked URL updated",
                blockedUrlService.updateBlockedUrl(id, request)));
    }

    @DeleteMapping("/blocked-urls/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBlockedUrl(
            @PathVariable Long id,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        blockedUrlService.deleteBlockedUrl(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Blocked URL deleted", null));
    }

    @PatchMapping("/blocked-urls/{id}/toggle")
    public ResponseEntity<ApiResponse<BlockedUrlEntity>> toggleBlockedUrl(
            @PathVariable Long id,
            @RequestParam boolean active,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Blocked URL status updated",
                blockedUrlService.toggleBlockedUrl(id, active)));
    }

    @GetMapping("/blocked-urls/search")
    public ResponseEntity<ApiResponse<List<BlockedUrlEntity>>> searchBlockedUrls(
            @RequestParam String keyword,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Search results retrieved",
                blockedUrlService.searchUrls(keyword)));
    }

    // --- Partial Access Admin Logic ---

   /**
 * Get partial access sites for a specific agent
 */
@GetMapping("/agents/{agentId}/partial-access")
public ResponseEntity<ApiResponse<List<PartialAccessEntity>>> getAgentPartialAccess(
        @PathVariable Long agentId,
        HttpSession session) {
    if (!isAdminAuthenticated(session)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
    }
    return ResponseEntity.ok(new ApiResponse<>(true, "Partial access rules retrieved",
            partialAccessService.getPartialAccessForAgent(agentId)));
}

/**
 * Get all partial access sites (admin)
 */
@GetMapping("/partial-access")
public ResponseEntity<ApiResponse<List<PartialAccessEntity>>> getAllPartialAccess(HttpSession session) {
    if (!isAdminAuthenticated(session)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
    }
    return ResponseEntity.ok(new ApiResponse<>(true, "All partial access rules retrieved",
            partialAccessService.getAllPartialAccessSites()));
}

    @PostMapping("/partial-access/bulk")
    public ResponseEntity<ApiResponse<List<PartialAccessEntity>>> addPartialAccessBulk(
            @RequestBody List<PartialAccessRequest> requests,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        User admin = (User) session.getAttribute("currentUser");
        return ResponseEntity.ok(new ApiResponse<>(true, "Partial access rules added successfully",
                partialAccessService.addPartialAccessSites(requests, admin.getUsername())));
    }

    @PutMapping("/partial-access/{id}")
    public ResponseEntity<ApiResponse<PartialAccessEntity>> updatePartialAccess(
            @PathVariable Long id,
            @RequestBody PartialAccessRequest request,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Partial access rule updated",
                partialAccessService.updatePartialAccessSite(id, request)));
    }

    @DeleteMapping("/partial-access/{id}")
    public ResponseEntity<ApiResponse<String>> deletePartialAccess(
            @PathVariable Long id,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        partialAccessService.deletePartialAccessSite(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Partial access rule deleted", null));
    }

    @PatchMapping("/partial-access/{id}/toggle")
    public ResponseEntity<ApiResponse<PartialAccessEntity>> togglePartialAccess(
            @PathVariable Long id,
            @RequestParam boolean active,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Partial access status updated",
                partialAccessService.togglePartialAccessSite(id, active)));
    }

    @GetMapping("/partial-access/search")
    public ResponseEntity<ApiResponse<List<PartialAccessEntity>>> searchPartialAccess(
            @RequestParam String keyword,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Search results retrieved",
                partialAccessService.searchPartialAccessSites(keyword)));
    }

    @GetMapping("/agents/{agentId}/web-urls/summary")
    public ResponseEntity<ApiResponse<List<WebHistoryLog>>> getAgentWebSummary(
            @PathVariable Long agentId,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        List<WebHistoryLog> history = webHistoryLogRepository.findByAgentIdOrderByVisitTimestampDesc(agentId);
        List<WebHistoryLog> summary = history.stream().limit(10).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Web history summary retrieved", summary));
    }

    @GetMapping("/agents/{agentId}/app-usage/summary")
    public ResponseEntity<ApiResponse<List<AppUsageLog>>> getAgentAppSummary(
            @PathVariable Long agentId,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        List<AppUsageLog> usage = appUsageLogRepository.findByAgentIdOrderByReceivedAtDesc(agentId);
        List<AppUsageLog> summary = usage.stream().limit(10).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "App usage summary retrieved", summary));
    }

    // In AdminController.java - add debug endpoint
    @GetMapping("/debug/agent-by-mac/{macAddress}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugAgentByMac(@PathVariable String macAddress,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            List<User> agents = userService.findByMacAddress(macAddress);
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("macAddress", macAddress);
            debugInfo.put("agentsFound", agents.size());

            List<Map<String, Object>> agentDetails = new ArrayList<>();
            for (User agent : agents) {
                Map<String, Object> agentInfo = new HashMap<>();
                agentInfo.put("id", agent.getId());
                agentInfo.put("username", agent.getUsername());
                agentInfo.put("hostname", agent.getHostname());
                agentInfo.put("plainPassword", agent.getPlainPassword());
                agentInfo.put("hasEncodedPassword", agent.getPassword() != null);
                agentDetails.add(agentInfo);
            }
            debugInfo.put("agents", agentDetails);

            return ResponseEntity.ok(new ApiResponse<>(true, "Agent debug by MAC", debugInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Debug failed: " + e.getMessage()));
        }
    }

    // In AdminController.java - Fix getAllAgents method
    @GetMapping("/agents")
    public ResponseEntity<ApiResponse<List<AgentDTO>>> getAllAgents(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            List<User> agents = userService.getAllAgents();
            List<AgentDTO> agentDTOs = agents.stream().map(agent -> {
                AgentDTO dto = AgentDTO.fromUser(agent);
                dto.setAgentRuntimeState(agentService.calculateRuntimeState(agent));
                try {
                    List<AgentCapability> capabilities = agentService.getAllCapabilities(agent.getId());
                    dto.setCapabilityCount(capabilities != null ? capabilities.size() : 0);
                    dto.setActivePolicyCount(
                            capabilities != null
                                    ? (int) capabilities.stream()
                                            .filter(cap -> cap != null && cap.getIsActive() != null
                                                    && cap.getIsActive())
                                            .count()
                                    : 0);
                } catch (Exception e) {
                    log.warn("Failed to get capabilities for agent {}: {}", agent.getId(), e.getMessage());
                    dto.setCapabilityCount(0);
                    dto.setActivePolicyCount(0);
                }
                return dto;
            }).toList();

            log.info("📊 Returning {} agents to admin", agentDTOs.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "Agents retrieved successfully", agentDTOs));
        } catch (Exception e) {
            log.error("❌ Error getting agents: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get agents: " + e.getMessage()));
        }
    }

    /**
 * Get single agent details by ID
 */
@GetMapping("/agents/{agentId}")
public ResponseEntity<ApiResponse<AgentDTO>> getAgentById(
        @PathVariable Long agentId,
        HttpSession session) {
    
    if (!isAdminAuthenticated(session)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Admin access required", null));
    }
    
    try {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        
        AgentDTO dto = AgentDTO.fromUser(agent);
        dto.setAgentRuntimeState(agentService.calculateRuntimeState(agent));
        
        try {
            List<AgentCapability> capabilities = agentService.getAllCapabilities(agent.getId());
            dto.setCapabilityCount(capabilities != null ? capabilities.size() : 0);
            dto.setActivePolicyCount(
                    capabilities != null
                            ? (int) capabilities.stream()
                                    .filter(cap -> cap != null && cap.getIsActive() != null && cap.getIsActive())
                                    .count()
                            : 0);
        } catch (Exception e) {
            log.warn("Failed to get capabilities for agent {}: {}", agent.getId(), e.getMessage());
            dto.setCapabilityCount(0);
            dto.setActivePolicyCount(0);
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Agent details retrieved", dto));
        
    } catch (Exception e) {
        log.error("❌ Failed to get agent {}: {}", agentId, e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to get agent: " + e.getMessage(), null));
    }
}


@GetMapping("/agents/{agentId}/detail")
public ResponseEntity<ApiResponse<AgentDetailDTO>> getAgentDetail(
        @PathVariable Long agentId,
        HttpSession session) {
    
    if (!isAdminAuthenticated(session)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Admin access required", null));
    }
    
    try {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        
        AgentDetailDTO dto = new AgentDetailDTO();
        dto.setId(agent.getId());
        dto.setUsername(agent.getUsername());
        dto.setEmail(agent.getEmail());
        dto.setHostname(agent.getHostname());
        dto.setMacAddress(agent.getMacAddress());
        dto.setIpAddress(agent.getIpAddress());
        dto.setLastHeartbeat(agent.getLastHeartbeat());
        dto.setLastLogin(agent.getLastLogin());
        dto.setCreatedAt(agent.getCreatedAt());
        dto.setStatus(agent.getStatus() != null ? agent.getStatus().toString() : null);
        dto.setRole(agent.getRole() != null ? agent.getRole().toString() : null);
        
        dto.setAgentRuntimeState(agentService.calculateRuntimeState(agent));
        
        // Get capabilities count
        try {
            List<AgentCapability> capabilities = agentService.getAllCapabilities(agent.getId());
            dto.setCapabilityCount(capabilities != null ? capabilities.size() : 0);
            dto.setActivePolicyCount(
                    capabilities != null
                            ? (int) capabilities.stream()
                                    .filter(cap -> cap != null && cap.getIsActive() != null && cap.getIsActive())
                                    .count()
                            : 0);
        } catch (Exception e) {
            log.warn("Failed to get capabilities for agent {}: {}", agent.getId(), e.getMessage());
            dto.setCapabilityCount(0);
            dto.setActivePolicyCount(0);
        }
        
        // Get OCR statuses WITHOUT recursive references
        if (agent.getOcrStatuses() != null && !agent.getOcrStatuses().isEmpty()) {
            List<Map<String, Object>> simplifiedStatuses = new ArrayList<>();
            for (OcrStatus status : agent.getOcrStatuses()) {
                Map<String, Object> statusMap = new HashMap<>();
                statusMap.put("id", status.getId());
                statusMap.put("ocrEnabled", status.isOcrEnabled());
                statusMap.put("ocrCapable", status.isOcrCapable());
                statusMap.put("threatScore", status.getThreatScore());
                statusMap.put("violationsLast24h", status.getViolationsLast24h());
                statusMap.put("lastScreenshotTime", status.getLastScreenshotTime());
                statusMap.put("updatedAt", status.getUpdatedAt());
                // DON'T include the agent reference
                simplifiedStatuses.add(statusMap);
            }
            dto.setOcrStatuses(simplifiedStatuses);
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Agent details retrieved", dto));
        
    } catch (Exception e) {
        log.error("❌ Failed to get agent {}: {}", agentId, e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to get agent: " + e.getMessage(), null));
    }
}

/**
 * Get violations count for an agent
 */
@GetMapping("/agents/{agentId}/violations")
public ResponseEntity<ApiResponse<Integer>> getAgentViolations(
        @PathVariable Long agentId,
        HttpSession session) {
    
    if (!isAdminAuthenticated(session)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Admin access required", null));
    }
    
    try {
        // Count alerts with HIGH/CRITICAL severity in last 24 hours for this agent
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        
        // You need to add this method to your AlertRepository
        int violations = alertRepository.countByAgentIdAndSeverityInAndCreatedAtAfter(
                agentId, 
                Arrays.asList("HIGH", "CRITICAL"),
                yesterday
        );
        
        log.info("📊 Agent {} has {} violations in last 24 hours", agentId, violations);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Violations count retrieved", violations));
    } catch (Exception e) {
        log.error("❌ Failed to get violations for agent {}: {}", agentId, e.getMessage());
        // Return 0 instead of error to not break UI
        return ResponseEntity.ok(new ApiResponse<>(true, "Violations count retrieved (with default)", 0));
    }
}

    @PutMapping("/agents/{id}/status")
    public ResponseEntity<ApiResponse<User>> updateAgentStatus(@PathVariable Long id, @RequestParam String status,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
            User updatedUser = userService.updateUserStatus(id, userStatus);
            return ResponseEntity.ok(new ApiResponse<>(true, "User status updated", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to update status: " + e.getMessage()));
        }
    }

    @PostMapping("/agents/{id}/reset-password")
    public ResponseEntity<ApiResponse<String>> resetAgentPassword(@PathVariable Long id, HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        ApiResponse<String> response = userService.resetAgentPassword(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/agents/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteAgent(@PathVariable Long id, HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            // Check if this is an agent (not admin)
            if (user.getRole() != User.UserRole.AGENT) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Cannot delete admin users"));
            }

            // Clear ALL related entities
            user.getOcrStatuses().clear();

            // Clear other related entities if they exist
            if (user.getOcrLiveData() != null) {
                user.getOcrLiveData().clear();
            }

            if (user.getOcrSecurityCertificates() != null) {
                user.getOcrSecurityCertificates().clear();
            }

            if (user.getAlerts() != null) {
                user.getAlerts().clear();
            }

            // If you have AgentCapabilities relationship:
            if (user.getAgentCapabilities() != null) {
                user.getAgentCapabilities().clear();
            }

            userRepository.save(user); // Save to persist all removals

            // Now delete the user
            userRepository.delete(user);

            return ResponseEntity.ok(new ApiResponse<>(true, "Agent deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to delete agent: " + e.getMessage()));
        }
    }

    // Replace your getProtectionPolicies method with this one
    @GetMapping("/protection-policies")
    public ResponseEntity<ApiResponse<Map<String, List<Policy>>>> getProtectionPolicies(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // This is now fully dynamic. It first finds what categories exist.
            List<String> distinctCategories = policyRepository.findDistinctCategories();

            Map<String, List<Policy>> policiesByCategory = new HashMap<>();

            // Then, for each category it found, it fetches the corresponding policies.
            for (String category : distinctCategories) {
                policiesByCategory.put(category, policyService.getPoliciesByCategory(category));
            }

            log.info("🛡️ Returning protection policies for {} dynamically found categories",
                    policiesByCategory.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "Protection policies retrieved", policiesByCategory));

        } catch (Exception e) {
            log.error("❌ Error getting protection policies: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get protection policies: " + e.getMessage()));
        }
    }

    @PostMapping("/assign-protection")
    public ResponseEntity<ApiResponse<String>> assignProtectionPolicy(@RequestBody ProtectionAssignmentRequest request,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            int successCount = 0;
            List<String> errors = new ArrayList<>();

            for (Long agentId : request.getAgentIds()) {
                try {
                    agentService.activateCapability(agentId, request.getPolicyCode(), request.getPolicyData());
                    successCount++;
                } catch (Exception e) {
                    errors.add("Agent " + agentId + ": " + e.getMessage());
                }
            }

            String message = String.format("Policy activated for %d agents", successCount);
            if (!errors.isEmpty()) {
                message += ". Errors: " + String.join("; ", errors);
            }

            log.info("✅ {} - Success: {}, Errors: {}", request.getPolicyCode(), successCount, errors.size());
            return ResponseEntity.ok(new ApiResponse<>(true, message));
        } catch (Exception e) {
            log.error("❌ Failed to assign policy {}: {}", request.getPolicyCode(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to assign policy: " + e.getMessage()));
        }
    }

    @GetMapping("/agents/{agentId}/file-operations")
    public ResponseEntity<ApiResponse<List<String>>> getAgentFileOperations(@PathVariable Long agentId,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // Get only active file-related capabilities for this agent
            List<AgentCapability> activeCapabilities = agentService.getActiveCapabilities(agentId);

            List<String> fileOperations = activeCapabilities.stream()
                    .filter(cap -> "FILE".equals(cap.getCategory()))
                    .map(AgentCapability::getCapabilityCode)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(true, "File operations retrieved", fileOperations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get file operations: " + e.getMessage()));
        }
    }

    @PostMapping("/file-policy/assign")
    public ResponseEntity<ApiResponse<String>> assignFilePolicy(@RequestBody FilePolicyAssignmentRequest request,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // Create policy data in specific format
            String policyData = String.format("operation:%s,path:%s,action:%s",
                    request.getOperation(), request.getFilePath(), request.getAction());

            agentService.activateCapability(request.getAgentId(), request.getPolicyCode(), policyData);

            return ResponseEntity.ok(new ApiResponse<>(true, "File policy assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to assign file policy: " + e.getMessage()));
        }
    }

    @PostMapping("/deactivate-protection")
    public ResponseEntity<ApiResponse<String>> deactivateProtectionPolicy(
            @RequestBody ProtectionAssignmentRequest request, HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            int successCount = 0;
            List<String> errors = new ArrayList<>();

            for (Long agentId : request.getAgentIds()) {
                try {
                    agentService.deactivateCapability(agentId, request.getPolicyCode());
                    successCount++;
                } catch (Exception e) {
                    errors.add("Agent " + agentId + ": " + e.getMessage());
                }
            }

            String message = String.format("Policy deactivated for %d agents", successCount);
            if (!errors.isEmpty()) {
                message += ". Errors: " + String.join("; ", errors);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, message));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to deactivate policy: " + e.getMessage()));
        }
    }

    @GetMapping("/web-history/{agentId}")
    public ResponseEntity<ApiResponse<List<WebHistoryLog>>> getWebHistory(@PathVariable Long agentId) {
        try {
            List<WebHistoryLog> history = webHistoryLogRepository.findByAgentIdOrderByVisitTimestampDesc(agentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Web history retrieved", history));
        } catch (Exception e) {
            log.error("❌ Failed to get web history for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get history: " + e.getMessage()));
        }
    }

    // In AdminController.java - UPDATE THE MAPPING
    @GetMapping("/web-history-detailed/{agentId}")
    public ResponseEntity<ApiResponse<List<WebHistoryLog>>> getDetailedWebHistory(@PathVariable Long agentId,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            List<WebHistoryLog> history = webHistoryLogRepository.findByAgentIdOrderByVisitTimestampDesc(agentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Detailed web history retrieved", history));
        } catch (Exception e) {
            log.error("❌ Failed to get detailed web history for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get detailed history: " + e.getMessage()));
        }
    }

    @GetMapping("/app-usage/{agentId}")
    public ResponseEntity<ApiResponse<List<AppUsageLog>>> getAppUsage(
            @PathVariable Long agentId,
            @RequestParam(defaultValue = "50") int limit,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // Fix: Use findByAgentIdOrderByReceivedAtDesc which exists in repository
            List<AppUsageLog> usage = appUsageLogRepository.findByAgentIdOrderByReceivedAtDesc(agentId);

            // Limit results
            if (usage.size() > limit) {
                usage = usage.subList(0, limit);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "App usage retrieved", usage));
        } catch (Exception e) {
            log.error("❌ Failed to get app usage for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get app usage: " + e.getMessage()));
        }
    }

    @GetMapping("/file-events/{agentId}")
    public ResponseEntity<ApiResponse<List<FileEventLog>>> getFileEvents(@PathVariable Long agentId,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            List<FileEventLog> fileEvents = fileEventLogRepository.findByAgentIdOrderByTimestampDesc(agentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "File events retrieved", fileEvents));
        } catch (Exception e) {
            log.error("❌ Failed to get file events for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get file events: " + e.getMessage()));
        }
    }

    @GetMapping("/file-events/blocked/{agentId}")
    public ResponseEntity<ApiResponse<List<FileEventLog>>> getBlockedFileEvents(@PathVariable Long agentId,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            List<FileEventLog> blockedEvents = fileEventLogRepository
                    .findByAgentIdAndBlockedTrueOrderByTimestampDesc(agentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Blocked file events retrieved", blockedEvents));
        } catch (Exception e) {
            log.error("❌ Failed to get blocked file events for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get blocked file events: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/file-events/{agentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFileEventStats(@PathVariable Long agentId,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            Map<String, Object> stats = new HashMap<>();

            // Total blocked events
            Long blockedCount = fileEventLogRepository.countBlockedEventsByAgentId(agentId);
            stats.put("blockedCount", blockedCount);

            // Events by operation type
            List<Object[]> operationCounts = fileEventLogRepository.countEventsByOperation(agentId);
            Map<String, Long> operationStats = new HashMap<>();
            for (Object[] result : operationCounts) {
                operationStats.put((String) result[0], (Long) result[1]);
            }
            stats.put("operationStats", operationStats);

            // Recent activity (last 24 hours)
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            List<FileEventLog> recentActivity = fileEventLogRepository.findByAgentIdAndTimestampBetween(agentId,
                    yesterday, LocalDateTime.now());
            stats.put("recentActivityCount", recentActivity.size());

            return ResponseEntity.ok(new ApiResponse<>(true, "File event stats retrieved", stats));
        } catch (Exception e) {
            log.error("❌ Failed to get file event stats for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get file event stats: " + e.getMessage()));
        }
    }

    @PostMapping("/log-event")
    public ResponseEntity<ApiResponse<String>> logEvent(@RequestBody EventLogRequest request, HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // Save event to database with automatic 30-day cleanup
            EventLog eventLog = new EventLog();
            eventLog.setEventType(request.getEventType());
            eventLog.setAgentId(request.getAgentId());
            eventLog.setDetails(request.getDetails());
            eventLog.setTimestamp(LocalDateTime.now());

            eventLogRepository.save(eventLog);

            log.info("📝 Event logged: {} for agent {}", request.getEventType(), request.getAgentId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Event logged successfully"));
        } catch (Exception e) {
            log.error("❌ Failed to log event: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to log event: " + e.getMessage()));
        }
    }

    @GetMapping("/all-history/{agentId}")
    public ResponseEntity<ApiResponse<List<CombinedHistoryDTO>>> getAllHistory(
            @PathVariable Long agentId,
            @RequestParam(defaultValue = "TODAY") String period,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            LocalDateTime startDate = calculateStartDate(period);
            List<CombinedHistoryDTO> allHistory = new ArrayList<>();

            // Get file events
            List<FileEventLog> fileEvents = fileEventLogRepository
                    .findByAgentIdAndTimestampAfterOrderByTimestampDesc(agentId, startDate);
            fileEvents.forEach(event -> allHistory.add(CombinedHistoryDTO.fromFileEvent(event)));

            // Get web history
            List<WebHistoryLog> webHistory = webHistoryLogRepository
                    .findByAgentIdAndVisitTimestampAfterOrderByVisitTimestampDesc(agentId, startDate);
            webHistory.forEach(entry -> allHistory.add(CombinedHistoryDTO.fromWebHistory(entry)));

            // Get USB history
            List<USBActivityLog> usbHistory = usbActivityRepository
                    .findByAgentIdAndTimestampAfterOrderByTimestampDesc(agentId, startDate);
            usbHistory.forEach(entry -> allHistory.add(CombinedHistoryDTO.fromUSBActivity(entry)));

            // Get alerts
            List<Alert> alerts = alertRepository.findByAgentIdAndCreatedAtAfterOrderByCreatedAtDesc(agentId, startDate);
            alerts.forEach(alert -> allHistory.add(CombinedHistoryDTO.fromAlert(alert)));

            // Sort by timestamp (newest first)
            allHistory.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

            return ResponseEntity.ok(new ApiResponse<>(true, "All history retrieved", allHistory));
        } catch (Exception e) {
            log.error("❌ Failed to get all history for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get history: " + e.getMessage()));
        }
    }

    @GetMapping("/logs/count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLogCounts(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam String logType,
            @RequestParam(required = false) Long agentId,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            LocalDateTime startDate = LocalDateTime.parse(fromDate + "T00:00:00");
            LocalDateTime endDate = LocalDateTime.parse(toDate + "T23:59:59");

            Map<String, Object> counts = new HashMap<>();
            long total = 0;

            if ("ALL".equals(logType) || "FILE_EVENTS".equals(logType)) {
                Long fileCount = fileEventLogRepository.countByTimestampBetweenAndAgentId(startDate, endDate, agentId);
                counts.put("fileEvents", fileCount);
                total += fileCount != null ? fileCount : 0;
            }

            if ("ALL".equals(logType) || "WEB_HISTORY".equals(logType)) {
                Long webCount = webHistoryLogRepository.countByVisitTimestampBetweenAndAgentId(startDate, endDate,
                        agentId);
                counts.put("webHistory", webCount);
                total += webCount != null ? webCount : 0;
            }

            if ("ALL".equals(logType) || "USB_HISTORY".equals(logType)) {
                Long usbCount = usbActivityRepository.countByTimestampBetweenAndAgentId(startDate, endDate, agentId);
                counts.put("usbHistory", usbCount);
                total += usbCount != null ? usbCount : 0;
            }

            if ("ALL".equals(logType) || "ALERTS".equals(logType)) {
                Long alertCount = alertRepository.countByCreatedAtBetweenAndAgentId(startDate, endDate, agentId);
                counts.put("alerts", alertCount);
                total += alertCount != null ? alertCount : 0;
            }

            counts.put("total", total);

            return ResponseEntity.ok(new ApiResponse<>(true, "Log counts retrieved", counts));

        } catch (Exception e) {
            log.error("❌ Failed to get log counts: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get log counts: " + e.getMessage()));
        }
    }

    @PostMapping("/logs/delete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteLogs(
            @RequestBody DeleteLogsRequest request,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            LocalDateTime startDate = LocalDateTime.parse(request.getFromDate() + "T00:00:00");
            LocalDateTime endDate = LocalDateTime.parse(request.getToDate() + "T23:59:59");
            Long agentId = request.getAgentId();

            Map<String, Object> result = new HashMap<>();
            long totalDeleted = 0;

            if ("ALL".equals(request.getLogType()) || "FILE_EVENTS".equals(request.getLogType())) {
                int deleted = fileEventLogRepository.deleteByTimestampBetweenAndAgentId(startDate, endDate, agentId);
                result.put("fileEventsDeleted", deleted);
                totalDeleted += deleted;
            }

            if ("ALL".equals(request.getLogType()) || "WEB_HISTORY".equals(request.getLogType())) {
                int deleted = webHistoryLogRepository.deleteByVisitTimestampBetweenAndAgentId(startDate, endDate,
                        agentId);
                result.put("webHistoryDeleted", deleted);
                totalDeleted += deleted;
            }

            if ("ALL".equals(request.getLogType()) || "USB_HISTORY".equals(request.getLogType())) {
                // Assuming you have a USBHistoryLogRepository
                // int deleted =
                // usbHistoryLogRepository.deleteByTimestampBetweenAndAgentId(startDate,
                // endDate, agentId);
                int deleted = 0; // Replace with actual deletion
                result.put("usbHistoryDeleted", deleted);
                totalDeleted += deleted;
            }

            if ("ALL".equals(request.getLogType()) || "ALERTS".equals(request.getLogType())) {
                int deleted = alertRepository.deleteByCreatedAtBetweenAndAgentId(startDate, endDate, agentId);
                result.put("alertsDeleted", deleted);
                totalDeleted += deleted;
            }

            result.put("deletedCount", totalDeleted);

            log.info("🧹 Deleted {} logs of type {} for date range {} to {}",
                    totalDeleted, request.getLogType(), request.getFromDate(), request.getToDate());

            return ResponseEntity.ok(new ApiResponse<>(true, "Logs deleted successfully", result));

        } catch (Exception e) {
            log.error("❌ Failed to delete logs: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to delete logs: " + e.getMessage()));
        }
    }

    @GetMapping("/agents/usb-history/{agentId}")
    public ResponseEntity<ApiResponse<List<USBActivityDTO>>> getUsbHistory(
            @PathVariable Long agentId,
            @RequestParam(defaultValue = "TODAY") String period,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            LocalDateTime startDate = calculateStartDate(period);
            List<USBActivityLog> usbHistory = usbActivityRepository
                    .findByAgentIdAndTimestampAfterOrderByTimestampDesc(agentId, startDate);

            List<USBActivityDTO> usbDTOs = usbHistory.stream()
                    .map(this::convertToUSBDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(true, "USB history retrieved", usbDTOs));
        } catch (Exception e) {
            log.error("❌ Failed to get USB history for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get USB history: " + e.getMessage()));
        }
    }

    private LocalDateTime calculateStartDate(String period) {
        return switch (period.toUpperCase()) {
            case "WEEK" -> LocalDateTime.now().minusWeeks(1);
            case "MONTH" -> LocalDateTime.now().minusMonths(1);
            case "ALL" -> LocalDateTime.of(1970, 1, 1, 0, 0);
            default -> LocalDateTime.now().toLocalDate().atStartOfDay(); // TODAY
        };
    }

    private USBActivityDTO convertToUSBDTO(USBActivityLog log) {
        USBActivityDTO dto = new USBActivityDTO();
        dto.setId(log.getId());
        dto.setAgentId(log.getAgentId());
        dto.setTimestamp(log.getTimestamp());
        dto.setAction(log.getAction());
        dto.setDeviceName(log.getDeviceName());
        dto.setVendorId(log.getVendorId());
        dto.setProductId(log.getProductId());
        dto.setSerialNumber(log.getSerialNumber());
        return dto;
    }

    @GetMapping("/agents/{agentId}/capabilities")
    public ResponseEntity<ApiResponse<Map<String, List<PolicyCapabilityDTO>>>> getAgentCapabilities(
            @PathVariable(name = "agentId") Long agentId, HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // Get ONLY capabilities that this agent actually has
            Map<String, List<PolicyCapabilityDTO>> capabilitiesByCategory = agentService
                    .getCapabilitiesByCategory(agentId);

            // Filter out empty categories and ensure we only return what agent has
            Map<String, List<PolicyCapabilityDTO>> filteredCapabilities = new HashMap<>();
            for (Map.Entry<String, List<PolicyCapabilityDTO>> entry : capabilitiesByCategory.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    filteredCapabilities.put(entry.getKey(), entry.getValue());
                }
            }

            log.info("🔄 Returning {} categories with {} total capabilities for agent {}",
                    filteredCapabilities.size(),
                    filteredCapabilities.values().stream().mapToInt(List::size).sum(),
                    agentId);

            return ResponseEntity.ok(new ApiResponse<>(true, "Agent capabilities retrieved", filteredCapabilities));
        } catch (Exception e) {
            log.error("❌ Failed to get agent capabilities for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get agent capabilities: " + e.getMessage()));
        }
    }

    // In AdminController.java - Add new file policy endpoints
    @PostMapping("/file-policy/create")
    public ResponseEntity<ApiResponse<String>> createFilePolicy(@RequestBody CreateFilePolicyRequest request,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // Create individual policies for each selected operation
            int successCount = 0;
            List<String> errors = new ArrayList<>();

            for (Long agentId : request.getAgentIds()) {
                try {
                    // Create path-based policies for each operation
                    for (String operation : request.getOperations()) {
                        String policyCode = "FILE_BLOCK_" + operation.toUpperCase() + "_PATHS";
                        agentService.activateCapability(agentId, policyCode, request.getPaths());
                    }

                    // Also create global operation blocking if needed
                    if ("BLOCK".equals(request.getAction())) {
                        for (String operation : request.getOperations()) {
                            String globalPolicyCode = "FILE_BLOCK_" + operation.toUpperCase();
                            agentService.activateCapability(agentId, globalPolicyCode, "");
                        }
                    }

                    successCount++;
                } catch (Exception e) {
                    errors.add("Agent " + agentId + ": " + e.getMessage());
                }
            }

            String message = String.format("File policy created for %d agents", successCount);
            if (!errors.isEmpty()) {
                message += ". Errors: " + String.join("; ", errors);
            }

            log.info("✅ File policy created - Operations: {}, Paths: {}, Action: {}, Success: {}",
                    request.getOperations(), request.getPaths(), request.getAction(), successCount);
            return ResponseEntity.ok(new ApiResponse<>(true, message));
        } catch (Exception e) {
            log.error("❌ Failed to create file policy: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to create file policy: " + e.getMessage()));
        }
    }

    @PostMapping("/agents/{agentId}/browse")
    public ResponseEntity<ApiResponse<String>> requestFileBrowse(
            @PathVariable Long agentId,
            @RequestParam(required = false) String path,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        AgentCommand cmd = new AgentCommand();
        cmd.setAgentId(agentId);
        cmd.setCommand("FILE_BROWSE");
        cmd.setPath(path != null ? path : "");
        cmd.setProcessed(false);
        agentCommandRepository.save(cmd);
        try {
            Map<String, Object> pending = new HashMap<>();
            pending.put("agentId", agentId);
            pending.put("pending", true);
            pending.put("path", path == null ? "" : path);
            simpMessagingTemplate.convertAndSend("/topic/admin/agent/" + agentId + "/browse-status", pending);
        } catch (Exception ignored) {

        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Browse request sent"));
    }

    @GetMapping("/agents/{agentId}/browse-files")
    public ResponseEntity<ApiResponse<List<FileSystemItem>>> browseAgentFiles(
            @PathVariable Long agentId,
            @RequestParam(required = false) String path,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // 1) If there is already a cached browse response for this agent, return it
            FileBrowseResponseDTO cached = agentService.getBrowseResponse(agentId);
            if (cached != null) {
                // If path param provided, only return if it matches the cached currentPath
                if (path == null || path.isEmpty() || path.equals(cached.getCurrentPath())) {
                    // convert DTO items to FileSystemItem model used by AdminController
                    List<FileSystemItem> items = cached.getItems().stream().map(it -> {
                        FileSystemItem f = new FileSystemItem();
                        f.setName(it.getName());
                        f.setFullPath(it.getFullPath());
                        f.setDirectory(it.isDirectory());
                        f.setSize(it.getSize());
                        return f;
                    }).collect(Collectors.toList());

                    return ResponseEntity.ok(new ApiResponse<>(true, "File system browsed (cached)", items));
                }
                // else cached exists but path mismatch — proceed to request new browse
            }

            // 2) No cached response — enqueue command for agent and return pending.
            // Create command and save
            AgentCommand cmd = new AgentCommand();
            cmd.setAgentId(agentId);
            cmd.setCommand("FILE_BROWSE");
            cmd.setPath(path != null ? path : " ");
            cmd.setProcessed(false);
            // Save via repository
            agentCommandRepository.save(cmd);

            // Respond to UI that request has been sent. UI should poll
            // /agents/{id}/browse-result
            return ResponseEntity.ok(new ApiResponse<>(false, "Browse request sent (pending)"));

        } catch (Exception e) {
            log.error("❌ Failed to browse agent files: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to browse files: " + e.getMessage()));
        }
    }

    @GetMapping("/agents/{agentId}/validate-path")
    public ResponseEntity<ApiResponse<PathValidationResponse>> validateAgentPath(
            @PathVariable Long agentId,
            @RequestParam String path,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // This would call the Rust agent to check if path exists
            boolean exists = agentService.validateAgentPath(agentId, path);
            PathValidationResponse response = new PathValidationResponse(path, exists);
            return ResponseEntity.ok(new ApiResponse<>(true, "Path validated", response));
        } catch (Exception e) {
            log.error("❌ Failed to validate path: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to validate path: " + e.getMessage()));
        }
    }

    // Request DTOs
    @Data
    public static class CreateFilePolicyRequest {
        private List<String> operations; // ["READ", "WRITE", "CREATE", "DELETE", "RENAME", "MOVE", "COPY", "OPEN"]
        private String paths; // JSON array of paths: "[\"C:\\\\Users\", \"D:\\\\Confidential\"]"
        private String action; // "BLOCK" or "ALLOW"
        private List<Long> agentIds;

        // Getters and setters
        public List<String> getOperations() {
            return operations;
        }

        public void setOperations(List<String> operations) {
            this.operations = operations;
        }

        public String getPaths() {
            return paths;
        }

        public void setPaths(String paths) {
            this.paths = paths;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public List<Long> getAgentIds() {
            return agentIds;
        }

        public void setAgentIds(List<Long> agentIds) {
            this.agentIds = agentIds;
        }
    }

    @Data
    public static class PathValidationResponse {
        private String path;
        private boolean exists;

        public PathValidationResponse(String path, boolean exists) {
            this.path = path;
            this.exists = exists;
        }

        // Getters
        public String getPath() {
            return path;
        }

        public boolean isExists() {
            return exists;
        }
    }

    @Data
    public static class FileSystemItem {
        private String name;
        private String fullPath;
        private boolean isDirectory;
        private long size;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFullPath() {
            return fullPath;
        }

        public void setFullPath(String fullPath) {
            this.fullPath = fullPath;
        }

        public boolean isDirectory() {
            return isDirectory;
        }

        public void setDirectory(boolean directory) {
            isDirectory = directory;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }

    @GetMapping("/alerts/pending")
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getPendingAlerts(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        List<AlertDTO> alerts = alertService.getPendingAlerts();
        return ResponseEntity.ok(new ApiResponse<>(true, "Pending alerts retrieved", alerts));
    }

    // ADD THIS NEW ENDPOINT to get ALL alerts for the "Alerts" tab
    @GetMapping("/alerts/all")
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getAllAlerts(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        List<AlertDTO> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(new ApiResponse<>(true, "All alerts retrieved", alerts));
    }

    @GetMapping("/alerts/{id}")
    public ResponseEntity<ApiResponse<AlertDTO>> getAlertById(@PathVariable(name = "id") String id,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            Long alertId = null;
            Alert alert = null;

            if (id.startsWith("ALT-")) {
                alert = alertRepository.findByAlertCode(id)
                        .orElse(null);

                if (alert == null) {
                    // Try parsing the numeric part if search by code fails
                    try {
                        alertId = Long.parseLong(id.substring(4));
                        alert = alertRepository.findById(alertId).orElse(null);
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else {
                try {
                    alertId = Long.parseLong(id);
                    alert = alertRepository.findById(alertId).orElse(null);
                } catch (NumberFormatException ignored) {
                }
            }

            if (alert == null) {
                throw new RuntimeException("Alert not found with ID: " + id);
            }

            AlertDTO alertDTO = AlertDTO.fromEntity(alert);
            return ResponseEntity.ok(new ApiResponse<>(true, "Alert retrieved successfully", alertDTO));

        } catch (Exception e) {
            log.error("❌ Failed to get alert {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get alert: " + e.getMessage()));
        }
    }

    /**
     * Get count of high threat agents (threat score ≥ 70) from OCR status
     * This is already being called by your frontend
     */
    @GetMapping("/ocr/high-threat-agents/count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHighThreatAgentsCount(HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // Get all OCR statuses with threat score ≥ 70
            List<OcrDashboardStatsDTO> highThreatAgents = ocrService.getLatestStatusForAllAgents()
                    .stream()
                    .filter(agent -> agent.getCurrentThreatScore() >= 70)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("count", highThreatAgents.size());
            response.put("agents", highThreatAgents);
            response.put("timestamp", LocalDateTime.now().toString());

            log.info("📊 High threat agents count: {}", highThreatAgents.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "High threat count retrieved", response));

        } catch (Exception e) {
            log.error("❌ Failed to get high threat count: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get count: " + e.getMessage()));
        }
    }

    /**
     * Get OCR high threat alerts for the Alerts tab
     * This endpoint is called by your frontend's loadOcrHighThreatAlerts() function
     */
    @GetMapping("/ocr/high-threat-alerts")
    public ResponseEntity<ApiResponse<List<AlertDTO>>> getOcrHighThreatAlerts(
            @RequestParam(defaultValue = "20") int limit,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            List<Alert> alerts = alertRepository.findRecentOcrHighThreatAlerts(limit);
            List<AlertDTO> alertDTOs = alerts.stream()
                    .map(this::convertToAlertDTO)
                    .collect(Collectors.toList());

            log.info("📊 Retrieved {} OCR high threat alerts", alertDTOs.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "OCR high threat alerts retrieved", alertDTOs));

        } catch (Exception e) {
            log.error("❌ Failed to get OCR high threat alerts: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get alerts: " + e.getMessage()));
        }
    }

    // Helper method to convert Alert to AlertDTO
    private AlertDTO convertToAlertDTO(Alert alert) {
        AlertDTO dto = new AlertDTO();
        dto.setId(alert.getId());
        dto.setAgentId(alert.getAgent().getId());
        dto.setAgentName(alert.getAgent().getHostname());
        dto.setUsername(alert.getAgent().getUsername());
        dto.setAlertType(alert.getAlertType());
        dto.setDescription(alert.getDescription());
        dto.setDeviceInfo(alert.getDeviceInfo());
        dto.setFileDetails(alert.getFileDetails());
        dto.setSeverity(alert.getSeverity());
        dto.setStatus(alert.getStatus());
        dto.setActionTaken(alert.getActionTaken());
        dto.setCreatedAt(alert.getCreatedAt());
        return dto;
    }

    @GetMapping("/agents-with-capability/{policyCode}")
    public ResponseEntity<ApiResponse<List<AgentPolicyStatusDTO>>> getAgentsWithCapability(
            @PathVariable String policyCode, HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        try {
            // Call the new service method
            List<AgentPolicyStatusDTO> agentStatuses = agentService.getAgentPolicyStatuses(policyCode);
            return ResponseEntity.ok(new ApiResponse<>(true, "Agents retrieved successfully", agentStatuses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get agents: " + e.getMessage()));
        }
    }

    @PostMapping("/alerts/{id}/decision")
    public ResponseEntity<ApiResponse<AlertDTO>> handleAlertDecision(@PathVariable(name = "id") String id,
            @RequestParam(name = "decision") String decision, HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            Long alertId = null;
            Alert alert = null;

            if (id.startsWith("ALT-")) {
                alert = alertRepository.findByAlertCode(id).orElse(null);
                if (alert == null) {
                    try {
                        alertId = Long.parseLong(id.substring(4));
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    alertId = alert.getId();
                }
            } else {
                try {
                    alertId = Long.parseLong(id);
                } catch (NumberFormatException ignored) {
                }
            }

            if (alertId == null) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid alert ID format"));
            }

            AlertDTO updatedAlert = alertService.handleDecision(alertId, decision);
            return ResponseEntity.ok(new ApiResponse<>(true, "Decision processed", updatedAlert));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to process decision: " + e.getMessage()));
        }
    }

    @PostMapping("/alerts/create-test")
    public ResponseEntity<ApiResponse<Alert>> createTestAlert(@RequestParam(name = "agentId") Long agentId,
            @RequestParam(name = "alertType") String alertType, @RequestParam(name = "description") String description,
            @RequestParam(name = "deviceInfo") String deviceInfo,
            @RequestParam(name = "fileDetails") String fileDetails, HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        User agent = userService.findById(agentId).orElseThrow(() -> new RuntimeException("Agent not found"));

        Alert alert = alertService.createAlert(agent, alertType, description, deviceInfo, fileDetails);
        return ResponseEntity.ok(new ApiResponse<>(true, "Test alert created", alert));
    }

    // THIS ENDPOINT for the Pie Chart
    @GetMapping("/stats/alerts-by-severity")
    public ResponseEntity<ApiResponse<List<AlertStatsDTO>>> getAlertsBySeverity(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        List<AlertStatsDTO> stats = alertService.getAlertSummaryBySeverity();
        return ResponseEntity.ok(new ApiResponse<>(true, "Alert stats by severity retrieved", stats));
    }

    // THIS ENDPOINT for the Bar Chart
    @GetMapping("/stats/alerts-by-date")
    public ResponseEntity<ApiResponse<List<AlertsByDateDTO>>> getAlertsByDate(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        List<AlertsByDateDTO> stats = alertService.getAlertSummaryByDate();
        return ResponseEntity.ok(new ApiResponse<>(true, "Alert stats by date retrieved", stats));
    }

    // ADD THIS NEW ENDPOINT
    @PostMapping("/update-policy-data")
    public ResponseEntity<ApiResponse<String>> updatePolicyData(@RequestBody UpdatePolicyDataRequest request,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            agentService.updatePolicyData(request.getAgentId(), request.getPolicyCode(), request.getPolicyData());
            return ResponseEntity.ok(new ApiResponse<>(true, "Policy data updated successfully"));
        } catch (Exception e) {
            log.error("❌ Failed to update policy data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to update data: " + e.getMessage()));
        }
    }

    private Policy convertToPolicy(AgentCapability capability, User agent) {
        Policy policy = new Policy();
        policy.setId(capability.getId()); // Use capability ID
        policy.setPolicyCode(capability.getCapabilityCode());
        policy.setName(capability.getName() + " - " + agent.getHostname());
        policy.setDescription(capability.getDescription());
        policy.setCategory(capability.getCategory());
        policy.setPolicyType(capability.getAction() + "_" + capability.getCategory());
        policy.setAction(capability.getAction());
        policy.setTarget(capability.getTarget());
        policy.setSeverity(capability.getSeverity());
        policy.setIsActive(capability.getIsActive());

        // Store agent info for the frontend
        policy.setAgentId(agent.getId());
        policy.setAgentHostname(agent.getHostname());

        return policy;
    }

    private boolean isAdminAuthenticated(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    // --- Agent-specific Data Views (Migrated from PythonClientController) ---

    @GetMapping("/agents/{agentId}/logs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAgentLogs(
            @PathVariable Long agentId,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        String deviceId = getDeviceIdForAgent(agent);
        List<Map<String, Object>> logs = pythonClientService.getDeviceLogs(deviceId).stream()
                .map(log -> {
                    Map<String, Object> logMap = new HashMap<>();
                    logMap.put("id", log.getId());
                    logMap.put("deviceId", log.getDeviceId());
                    logMap.put("logType", log.getLogType());
                    logMap.put("logContent", log.getLogContent());
                    logMap.put("timestamp", log.getTimestamp());
                    logMap.put("fileSize", log.getFileSize());
                    return logMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Agent logs retrieved", logs));
    }

    @GetMapping("/agents/{agentId}/web-history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAgentWebHistory(
            @PathVariable Long agentId,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        String deviceId = getDeviceIdForAgent(agent);
        List<Map<String, Object>> urls = pythonClientService.getDeviceUrlHistory(deviceId).stream()
                .map(url -> {
                    Map<String, Object> urlMap = new HashMap<>();
                    urlMap.put("id", url.getId());
                    urlMap.put("deviceId", url.getDeviceId());
                    urlMap.put("urls", url.getUrls());
                    urlMap.put("blockedCount", url.getBlockedCount());
                    urlMap.put("suspiciousCount", url.getSuspiciousCount());
                    urlMap.put("totalVisits", url.getTotalVisits());
                    urlMap.put("timestamp", url.getTimestamp());
                    return urlMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Agent URL history retrieved", urls));
    }

    @GetMapping("/agents/{agentId}/app-usage")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAgentAppUsage(
            @PathVariable Long agentId,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        String deviceId = getDeviceIdForAgent(agent);
        List<Map<String, Object>> appUsage = pythonClientService.getDeviceAppUsageHistory(deviceId).stream()
                .map(app -> {
                    Map<String, Object> appMap = new HashMap<>();
                    appMap.put("id", app.getId());
                    appMap.put("deviceId", app.getDeviceId());
                    appMap.put("currentApp", app.getCurrentApp());
                    appMap.put("currentSessionDuration", app.getCurrentSessionDuration());
                    appMap.put("totalAppsTracked", app.getTotalAppsTracked());
                    appMap.put("totalTimeTracked", app.getTotalTimeTracked());
                    appMap.put("activeUsageTime", app.getActiveUsageTime());
                    appMap.put("topApps", app.getTopApps());
                    appMap.put("categoryBreakdown", app.getCategoryBreakdown());
                    appMap.put("timestamp", app.getTimestamp());
                    return appMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Agent app usage retrieved", appUsage));
    }

    @PostMapping("/api/agent/app-usage/{agentId}")
    public ResponseEntity<ApiResponse<String>> receiveAppUsage(
            @PathVariable Long agentId,
            @RequestBody AppUsageData appUsageData,
            HttpSession session) {

    try {
        // Convert the entire AppUsageData to JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        String payloadJson = objectMapper.writeValueAsString(appUsageData);
        
        // Create and save log matching your model
        AppUsageLog Applog = new AppUsageLog();
        Applog.setAgentId(agentId);
        Applog.setReceivedAt(LocalDateTime.now());
        Applog.setCurrentApp(appUsageData.getCurrentApp());
        Applog.setActiveUsageTime(appUsageData.getActiveUsageTime());
        Applog.setPayloadJson(payloadJson);  // Store everything as JSON
        
        appUsageLogRepository.save(Applog);
        
        log.info("📥 Received app usage data from agent {}: current_app={}, active_time={}", 
                 agentId, appUsageData.getCurrentApp(), appUsageData.getActiveUsageTime());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "App usage data received", null));
    } catch (Exception e) {
        log.error("❌ Failed to save app usage data from agent {}: {}", agentId, e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to save: " + e.getMessage(), null));
    }
}

    @GetMapping("/agents/{agentId}/certificates")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAgentCertificates(
            @PathVariable Long agentId,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        String deviceId = getDeviceIdForAgent(agent);
        List<Map<String, Object>> certificates = pythonClientService.getDeviceCertificates(deviceId).stream()
                .map(cert -> {
                    Map<String, Object> certMap = new HashMap<>();
                    certMap.put("id", cert.getId());
                    certMap.put("deviceId", cert.getDeviceId());
                    certMap.put("certificateId", cert.getCertificateId());
                    certMap.put("uploadedAt", cert.getUploadedAt());
                    certMap.put("generated", cert.getGenerated());
                    certMap.put("validUntil", cert.getValidUntil());
                    certMap.put("issuer", cert.getIssuer());
                    certMap.put("recipient", cert.getRecipient());
                    certMap.put("device", cert.getDevice());
                    certMap.put("securityMetrics", cert.getSecurityMetrics());
                    certMap.put("detailedAnalysis", cert.getDetailedAnalysis());
                    certMap.put("recommendations", cert.getRecommendations());
                    certMap.put("signature", cert.getSignature());
                    return certMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Agent certificates retrieved", certificates));
    }

    @PostMapping("/agents/{agentId}/certificates/generate")
    public ResponseEntity<ApiResponse<String>> generateCertificateForAgent(
            @PathVariable Long agentId,
            HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        String deviceId = getDeviceIdForAgent(agent);
        try {
            certificateGenerationService.generateCertificateForDeviceNow(deviceId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Certificate generation triggered for agent: " + agentId));
        } catch (Exception e) {
            log.error("❌ Failed to generate certificate: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Certificate generation failed: " + e.getMessage()));
        }
    }

    @PostMapping("/certificates/generate-all")
    public ResponseEntity<ApiResponse<String>> generateCertificatesForAllAgents(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Admin access required"));
        }
        try {
            certificateGenerationService.generateCertificatesFromRecentUrls();
            return ResponseEntity.ok(new ApiResponse<>(true, "Certificate generation triggered for all agents"));
        } catch (Exception e) {
            log.error("❌ Failed to generate certificates: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Certificate generation failed: " + e.getMessage()));
        }
    }

    private String getDeviceIdForAgent(User agent) {
        String deviceId = agent.getMacAddress();
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = agent.getHostname();
        }
        return deviceId;
    }

}