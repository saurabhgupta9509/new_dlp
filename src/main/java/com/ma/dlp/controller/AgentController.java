package com.ma.dlp.controller;

import com.ma.dlp.Repository.AgentCommandRepository;
import com.ma.dlp.Repository.FileEventLogRepository;
import com.ma.dlp.Repository.UserRepository;
import com.ma.dlp.Repository.WebHistoryLogRepository;
import com.ma.dlp.dto.*;
import com.ma.dlp.model.*;
import com.ma.dlp.service.*;
import lombok.Data;
import com.ma.dlp.dto.WebHistoryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import com.ma.dlp.dto.FileEventDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AgentCommandRepository agentCommandRepository;

    private final WebHistoryLogRepository webHistoryLogRepository;
    private final FileEventLogRepository fileEventLogRepository;
    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private WebsocketMessageService websocketMessageService;

    public AgentController(WebHistoryLogRepository webHistoryLogRepository,
            FileEventLogRepository fileEventLogRepository) {
        this.webHistoryLogRepository = webHistoryLogRepository;
        this.fileEventLogRepository = fileEventLogRepository;

    }

    private String cleanToken(String authHeader) {
        if (authHeader != null) {
            // Remove ALL occurrences of "Bearer " (case-insensitive)
            String cleaned = authHeader.replaceAll("(?i)Bearer ", "");
            return cleaned.trim();
        }
        return authHeader;
    }

    @GetMapping("/commands/{agentId}")
    public ResponseEntity<Map<String, Object>> getPendingCommand(@RequestHeader("Authorization") String token,
            @PathVariable(name = "agentId") Long agentId) {

        String cleanToken = cleanToken(token);
        log.info("🔐 Clean token: {}", cleanToken);
        log.info("🔐 Clean token length: {}", cleanToken.length());

        if (!agentService.validateToken(cleanToken, agentId)) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Object> resp = new HashMap<>();

        Optional<AgentCommand> cmd = agentCommandRepository
                .findFirstByAgentIdAndProcessedFalseOrderByCreatedAtAsc(agentId);

        if (cmd.isEmpty()) {
            resp.put("pending", false);
            return ResponseEntity.ok(resp);
        }

        AgentCommand c = cmd.get();

        resp.put("pending", true);
        resp.put("command", c.getCommand());
        resp.put("path", c.getPath());

        // do NOT mark as processed yet — Rust agent marks it after success

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/agents/{agentId}/browse-files")
    public ResponseEntity<?> browseFiles(
            @PathVariable(name = "agentId") Long agentId,
            @RequestParam(name = "path", required = false) String path) {
        log.info("📁 Browse requested for agent {} on path '{}'", agentId, path);

        FileBrowseResponseDTO cached = agentService.getBrowseResponse(agentId);

        if (cached != null && cached.getItems() != null && !cached.getItems().isEmpty()) {
            log.info("📁 Returning cached browse response for agent {}", agentId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "pending", false,
                    "data", cached.getItems()));
        }

        // enqueue command for agent to pick up
        agentService.sendBrowseCommand(agentId, path != null ? path : "");
        log.info("📤 Browse command queued for agent {} — waiting for agent response...", agentId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "pending", true,
                "data", List.of()));
    }

    @PostMapping("/file-browse-response")
    public ResponseEntity<ApiResponse<String>> receiveBrowseResponse(
            @RequestHeader("Authorization") String token,
            @RequestBody FileBrowseResponseDTO response) {

        Long agentId = response.getAgentId();
        if (agentId == null) {
            return ResponseEntity.status(400).body(new ApiResponse<>(false, "agentId missing"));
        }

        if (!agentService.validateToken(token, agentId)) {
            return ResponseEntity.status(401).body(new ApiResponse<>(false, "Invalid token"));
        }

        agentService.storeBrowseResponse(agentId, response);
        // broadcast over websocket so admin UI receives partials and final
        try {
            websocketMessageService.sendBrowseUpdate(agentId, response);

            Map<String, Object> payload = new HashMap<>();
            payload.put("agentId", response.getAgentId());
            payload.put("currentPath", response.getCurrentPath());
            payload.put("parentPath", response.getParentPath());
            payload.put("items", response.getItems());
            payload.put("partial", response.getPartial() != null ? response.getPartial() : false);
            payload.put("complete", response.getComplete() != null ? response.getComplete() : false);

            String dest = String.format("/topic/agent/%d/browse", agentId);
            simpMessagingTemplate.convertAndSend(dest, payload);
            log.info("🔔 Broadcasted browse response to topic {} (partial={}, complete={})", dest,
                    payload.get("partial"), payload.get("complete"));
        } catch (Exception e) {
            log.error("Failed to broadcast browse response: {}", e.getMessage(), e);
        }

        if (Boolean.TRUE.equals(response.getComplete())) {
            agentService.markBrowseCommandProcessed(agentId);
            log.info("Marked browse command processed for agent {}", agentId);
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "Browse response received"));
    }

    // @PostMapping("/login")
    // public ResponseEntity<ApiResponse<AgentAuthResponse>> agentLogin(@RequestBody
    // AgentLoginRequest request) {
    // try {
    // AgentAuthResponse response;
    //
    // if (request.getHostname() != null && request.getMacAddress() != null) {
    // response = agentService.authenticateAgent(request.getHostname(),
    // request.getMacAddress() ,request.getIpAddress() );
    // } else if (request.getUsername() != null && request.getPassword() != null) {
    // response = agentService.loginWithCredentials(
    // request.getUsername(),
    // request.getPassword(),
    // request.getHostname(), // Optional - update if provided
    // request.getMacAddress(), // Optional - update if provided
    // request.getIpAddress());
    // } else {
    // return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid
    // login request"));
    // }
    //
    // return ResponseEntity.ok(new ApiResponse<>(true, "Agent authenticated",
    // response));
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().body(new ApiResponse<>(false,
    // "Authentication failed: " + e.getMessage()));
    // }
    // }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AgentAuthResponse>> agentLogin(@RequestBody AgentLoginRequest request) {
        try {
            log.info("🔐 LOGIN REQUEST received - Username: {}, Has password: {}, Hostname: {}, MAC: {}",
                    request.getUsername(),
                    request.getPassword() != null,
                    request.getHostname(),
                    request.getMacAddress(),
                    request.getIpAddress());

            // Call AgentService login method
            AgentAuthResponse response = agentService.loginWithCredentials(
                    request.getUsername(),
                    request.getPassword(),
                    request.getHostname(),
                    request.getMacAddress(),
                    request.getIpAddress());

            return ResponseEntity.ok(new ApiResponse<>(true, "Agent authenticated successfully", response));

        } catch (Exception e) {
            log.error("❌ Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/capabilities")
    public ResponseEntity<ApiResponse<String>> reportCapabilities(@RequestHeader("Authorization") String token,
            @RequestBody CapabilityReportRequest request) {

        if (!agentService.validateToken(token, request.getAgentId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, "Invalid token"));
        }

        try {
            agentService.saveAgentCapabilities(request.getAgentId(), request.getCapabilities());
            return ResponseEntity.ok(new ApiResponse<>(true, "Capabilities reported successfully"));
        } catch (Exception e) {
            log.error("❌ Failed to report capabilities for agent {}: {}", request.getAgentId(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to report capabilities: " + e.getMessage()));
        }
    }

    @GetMapping("/active-policies")
    public ResponseEntity<ApiResponse<AgentPoliciesResponse>> getActivePolicies(
            @RequestHeader("Authorization") String token, @RequestParam(name = "agentId") Long agentId) {

        // 1. You check if the token is valid
        if (!agentService.validateToken(token, agentId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, "Invalid token"));
        }

        try {
            // 2. NEW: You check the agent's status in the database
            User agent = userService.findById(agentId).orElseThrow(() -> new RuntimeException("Agent not found"));
            if (agent.getStatus() != User.UserStatus.ACTIVE) {
                log.warn("Agent {} is not active, rejecting policy request.", agentId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "Agent account is not active."));
            }

            // Get active capabilities and convert to policies
            List<AgentCapability> activeCapabilities = agentService.getActiveCapabilities(agentId);
            // --- SYNTHETIC OCR POLICY ---
            AgentCapability ocrCapability = new AgentCapability();
            // Use the correct constant from the Rust agent for the toggle state
            ocrCapability.setCapabilityCode("OCR_MONITOR");
            ocrCapability.setIsActive(true);
            ocrCapability.setCategory("OCR");
            ocrCapability.setName("OCR Screen Monitoring");
            ocrCapability.setDescription("Dynamically controlled OCR status");
            ocrCapability.setAction("MONITOR");
            ocrCapability.setTarget("SCREEN_CONTENT");
            ocrCapability.setSeverity("MEDIUM");

            activeCapabilities.add(ocrCapability);

            log.info("📢 OCR Monitoring is OFF for agent {}.", agentId);

            List<PolicyCapabilityDTO> policyDTOs = activeCapabilities.stream().map(this::convertCapabilityToPolicyDTO) // Call
                                                                                                                       // the
                                                                                                                       // correct
                                                                                                                       // DTO
                                                                                                                       // converter
                    .collect(Collectors.toList());

            AgentPoliciesResponse response = new AgentPoliciesResponse();
            response.setAgentId(agentId);
            response.setPolicies(policyDTOs);
            response.setTimestamp(System.currentTimeMillis());

            log.info("📋 Returning {} active policies to agent {}", policyDTOs.size(), agentId);

            return ResponseEntity.ok(new ApiResponse<>(true, "Active policies retrieved", response));
        } catch (Exception e) {
            log.error("❌ Failed to get active policies for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get active policies: " + e.getMessage()));
        }
    }

    private PolicyCapabilityDTO convertCapabilityToPolicyDTO(AgentCapability capability) {
        PolicyCapabilityDTO dto = new PolicyCapabilityDTO();
        dto.setCode(capability.getCapabilityCode());
        dto.setName(capability.getName());
        dto.setDescription(capability.getDescription());
        dto.setCategory(capability.getCategory());
        dto.setAction(capability.getAction());
        dto.setTarget(capability.getTarget());
        dto.setSeverity(capability.getSeverity());
        dto.setIsActive(capability.getIsActive()); // This is the field Rust needs
        dto.setPolicyData(capability.getPolicyData() != null ? capability.getPolicyData() : "");
        return dto;
    }

    @PostMapping("/alerts")
    public ResponseEntity<ApiResponse<String>> submitAlert(@RequestHeader("Authorization") String token,
            @RequestBody AgentAlertRequest alertRequest) {

        if (!agentService.validateToken(token, alertRequest.getAgentId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, "Invalid token"));
        }

        try {
            User agent = userService.findById(alertRequest.getAgentId())
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            // Update agent's hostname if provided
            if (alertRequest.getAgentHostname() != null && !alertRequest.getAgentHostname().isEmpty()) {
                agent.setHostname(alertRequest.getAgentHostname());
                userService.save(agent);
            }

            Alert alert = new Alert();
            alert.setAgent(agent);
            alert.setAlertType(alertRequest.getAlertType());
            alert.setDescription(alertRequest.getDescription());
            alert.setDeviceInfo(alertRequest.getDeviceInfo());
            alert.setFileDetails(alertRequest.getFileDetails());
            alert.setSeverity(alertRequest.getSeverity());
            alert.setStatus("PENDING");
            alert.setActionTaken(alertRequest.getActionTaken());

            alertService.saveAlert(alert);

            return ResponseEntity.ok(new ApiResponse<>(true, "Alert received successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to save alert: " + e.getMessage()));
        }
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<ApiResponse<String>> heartbeat(@RequestHeader("Authorization") String token,
            @RequestParam(name = "agentId") Long agentId) {

        // Check 1: Is the token valid?
        if (!agentService.validateToken(token, agentId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, "Invalid token"));
        }

        try {
            // Check 2: Is the agent's account ACTIVE?
            User agent = userService.findById(agentId)
                    .orElseThrow(() -> new RuntimeException("Agent not found with ID: " + agentId));

            if (agent.getStatus() != User.UserStatus.ACTIVE) {
                log.warn("Agent {} is not active, rejecting heartbeat.", agentId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "Agent account is not active."));
            }

            // If both checks pass, proceed
            agentService.updateHeartbeat(agentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Heartbeat received"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Heartbeat failed: " + e.getMessage()));
        }
    }

    @PostMapping("/usb-alert")
    public ResponseEntity<ApiResponse<String>> submitUSBAlert(@RequestHeader("Authorization") String token,
            @RequestBody USBAlertRequest usbAlert) {

        if (!agentService.validateToken(token, usbAlert.getAgentId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, "Invalid token"));
        }

        try {
            User agent = userService.findById(usbAlert.getAgentId())
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            String description = String.format(
                    "USB Device Detected: %s - %d files analyzed, %d file types, %d suspicious files. Total size: %d bytes",
                    usbAlert.getDeviceInfo().getDriveLetter(), usbAlert.getFileAnalysis().getTotalFiles(),
                    usbAlert.getFileAnalysis().getFileTypes().size(),
                    usbAlert.getFileAnalysis().getSuspiciousFiles().size(), usbAlert.getFileAnalysis().getTotalSize());

            String fileDetails = String.format("File Types: %s | Suspicious Files: %s",
                    usbAlert.getFileAnalysis().getFileTypes().toString(),
                    usbAlert.getFileAnalysis().getSuspiciousFiles().toString());

            Alert alert = new Alert();
            alert.setAgent(agent);
            alert.setAlertType("USB_INSERTION");
            alert.setDescription(description);
            alert.setDeviceInfo(usbAlert.getDeviceInfo().toString());
            alert.setFileDetails(fileDetails);
            alert.setSeverity("HIGH");
            alert.setStatus("PENDING");
            alert.setActionTaken(usbAlert.getActionTaken());

            alertService.saveAlert(alert);

            return ResponseEntity.ok(new ApiResponse<>(true, "USB alert received"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to process USB alert: " + e.getMessage()));
        }
    }

    @PostMapping("/web-history-detailed")
    public ResponseEntity<ApiResponse<String>> logDetailedWebHistory(@RequestBody WebHistoryDetailedRequest request) {

        Long agentId = request.getAgentId();
        if (agentId == null) {
            return ResponseEntity.status(400).body(new ApiResponse<>(false, "Agent ID is missing", null));
        }

        try {
            WebHistoryLog logEntry = new WebHistoryLog();
            logEntry.setAgentId(agentId);
            logEntry.setUrl(request.getUrl());
            logEntry.setBrowser(request.getBrowser());
            logEntry.setVisitTimestamp(LocalDateTime.now());
            logEntry.setAction(request.getAction());
            logEntry.setBlocked(request.isBlocked());
            logEntry.setFileInfo(request.getFileInfo());

            webHistoryLogRepository.save(logEntry);

            log.info("📝 Detailed web history logged for agent {}: {} - {}", agentId, request.getAction(),
                    request.getUrl());
            return ResponseEntity.ok(new ApiResponse<>(true, "Detailed history logged successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to log detailed web history for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to log detailed history: " + e.getMessage()));
        }
    }

    @PostMapping("/web-alerts")
    public ResponseEntity<ApiResponse<String>> submitWebAlert(@RequestHeader("Authorization") String token,
            @RequestBody WebAlertRequest alertRequest) {

        if (!agentService.validateToken(token, alertRequest.getAgentId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, "Invalid token"));
        }

        try {
            User agent = userService.findById(alertRequest.getAgentId())
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            Alert alert = new Alert();
            alert.setAgent(agent);
            alert.setAlertType(alertRequest.getType());
            alert.setDescription(alertRequest.getDetails());
            alert.setSeverity("MEDIUM"); // Default for web alerts
            alert.setStatus("PENDING");
            alert.setActionTaken("DETECTED");

            alertService.saveAlert(alert);

            return ResponseEntity.ok(new ApiResponse<>(true, "Web alert received successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to save web alert: " + e.getMessage()));
        }
    }

    @PostMapping("/web-history")
    public ResponseEntity<ApiResponse<String>> logWebHistory(@RequestBody WebHistoryRequest request) {

        // 1. Get Agent ID directly from the request body
        Long agentId = request.getAgentId();
        if (agentId == null) {
            return ResponseEntity.status(400).body(new ApiResponse<>(false, "Agent ID is missing", null));
        }

        // 2. Save each log entry to the database
        for (WebLogDto logDto : request.getLogs()) {
            WebHistoryLog logEntry = new WebHistoryLog();
            logEntry.setAgentId(agentId);
            logEntry.setUrl(logDto.url());
            logEntry.setBrowser(logDto.browser());

            // FIX: Directly use the String timestamp, no conversion needed
            logEntry.setVisitTimestamp(logDto.visitTimestamp());

            webHistoryLogRepository.save(logEntry);
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "History logged successfully", null));
    }

    @PostMapping("/file-events")
    public ResponseEntity<ApiResponse<String>> logFileEvents( // Renamed to plural
            @RequestBody FileEventRequest request) { // This request now contains a List

        Long agentId = request.getAgentId();
        if (agentId == null) {
            return ResponseEntity.status(400).body(new ApiResponse<>(false, "Agent ID is missing", null));
        }

        try {
            List<FileEventLog> logEntries = new ArrayList<>();
            for (FileEventDTO event : request.getEvents()) { // Loop over the list
                FileEventLog logEntry = new FileEventLog();
                logEntry.setAgentId(agentId);
                logEntry.setOperation(event.getOperation());
                logEntry.setFilePath(event.getFilePath());
                logEntry.setFileExtension(event.getFileExtension());
                logEntry.setFileSize(event.getFileSize());
                logEntry.setProcessName(event.getProcessName());
                logEntry.setProcessId(event.getProcessId());
                logEntry.setUserId(event.getUserId());
                logEntry.setTimestamp(LocalDateTime.now()); // Or parse from DTO
                logEntry.setBlocked(event.isBlocked());
                logEntry.setReason(event.getReason());
                logEntries.add(logEntry);
            }

            fileEventLogRepository.saveAll(logEntries); // Save all at once

            log.info("📁 {} file events logged for agent {}", logEntries.size(), agentId);

            return ResponseEntity.ok(new ApiResponse<>(true, "File events logged successfully", null));
        } catch (Exception e) {
            log.error("❌ Failed to log file events for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to log file events: " + e.getMessage()));
        }
    }

    @GetMapping("/file-policies/{agentId}")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getFilePolicies(
            @PathVariable(name = "agentId") Long agentId, @RequestHeader("Authorization") String token) {

        if (!agentService.validateToken(token, agentId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, "Invalid token"));
        }

        try {
            // Get active file capabilities for this agent
            List<AgentCapability> activeCapabilities = agentService.getActiveCapabilities(agentId);

            // Filter for file-related capabilities and organize by policy type
            Map<String, List<String>> filePolicies = new HashMap<>();

            for (AgentCapability capability : activeCapabilities) {
                if ("FILE".equals(capability.getCategory())) {
                    String policyCode = capability.getCapabilityCode();
                    String policyData = capability.getPolicyData();

                    // Parse policy data and organize by policy type
                    if (policyData != null && !policyData.trim().isEmpty()) {
                        try {
                            List<String> items = Arrays.asList(policyData.split(","));

                            // Map policy codes to the appropriate policy type keys
                            if (policyCode.contains("BLOCK_EXTENSIONS")) {
                                if (policyCode.contains("CREATE")) {
                                    filePolicies.put("block_extensions_create", items);
                                } else if (policyCode.contains("WRITE")) {
                                    filePolicies.put("block_extensions_write", items);
                                } else if (policyCode.contains("DELETE")) {
                                    filePolicies.put("block_extensions_delete", items);
                                } else if (policyCode.contains("READ")) {
                                    filePolicies.put("block_extensions_read", items);
                                }
                            } else if (policyCode.contains("BLOCK_PATHS")) {
                                if (policyCode.contains("CREATE")) {
                                    filePolicies.put("block_paths_create", items);
                                } else if (policyCode.contains("WRITE")) {
                                    filePolicies.put("block_paths_write", items);
                                } else if (policyCode.contains("DELETE")) {
                                    filePolicies.put("block_paths_delete", items);
                                } else if (policyCode.contains("READ")) {
                                    filePolicies.put("block_paths_read", items);
                                }
                            } else if (policyCode.equals("FILE_READONLY_EXTENSIONS")) {
                                filePolicies.put("readonly_extensions", items);
                            } else if (policyCode.equals("FILE_READONLY_PATHS")) {
                                filePolicies.put("readonly_paths", items);
                            } else if (policyCode.contains("BLOCK_CREATE")) {
                                filePolicies.put("block_create", items);
                            } else if (policyCode.contains("BLOCK_WRITE")) {
                                filePolicies.put("block_write", items);
                            } else if (policyCode.contains("BLOCK_DELETE")) {
                                filePolicies.put("block_delete", items);
                            } else if (policyCode.contains("BLOCK_READ")) {
                                filePolicies.put("block_read", items);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse policy data for {}: {}", policyCode, policyData);
                        }
                    }
                }
            }

            log.info("📋 Returning file policies for agent {}: {} policy types", agentId, filePolicies.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "File policies retrieved", filePolicies));

        } catch (Exception e) {
            log.error("❌ Failed to get file policies for agent {}: {}", agentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get file policies: " + e.getMessage()));
        }
    }

    private String buildFileEventDescription(FileEventDTO event) {
        String action = event.isBlocked() ? "BLOCKED" : "DETECTED";
        return String.format("File %s %s: %s (Process: %s, User: %s)", event.getOperation(), action,
                event.getFilePath(), event.getProcessName(), event.getUserId());
    }

    private String buildFileEventDeviceInfo(FileEventDTO event) {
        return String.format("Process: %s (PID: %d), Extension: %s, Size: %s", event.getProcessName(),
                event.getProcessId(), event.getFileExtension(),
                event.getFileSize() != null ? event.getFileSize() + " bytes" : "N/A");
    }

    private String buildFileEventDetails(FileEventDTO event) {
        return String.format("Operation: %s, Path: %s, Extension: %s, Timestamp: %s, Reason: %s", event.getOperation(),
                event.getFilePath(), event.getFileExtension(), event.getTimestamp(),
                event.getReason() != null ? event.getReason() : "N/A");
    }

    private String determineFileEventSeverity(FileEventDTO event) {
        if (event.isBlocked()) {
            return "HIGH";
        }

        // High severity for sensitive operations
        switch (event.getOperation()) {
            case "DELETE":
            case "WRITE":
                return "MEDIUM";
            default:
                return "LOW";
        }
    }

    private List<PolicyCapabilityDTO> convertCapabilitiesToPolicies(List<AgentCapability> capabilities) {
        return capabilities.stream().map(this::convertCapabilityToPolicyDTO).collect(Collectors.toList());
    }

    @Data
    public static class AgentActivePoliciesResponse {
        private Long agentId;
        private List<PolicyCapabilityDTO> policies; // <-- This is a list of DTOs
        private Long timestamp;

    }

}
