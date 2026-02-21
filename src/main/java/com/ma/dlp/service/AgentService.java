package com.ma.dlp.service;//package com.ma.dlp.service;

import com.drew.metadata.Age;
import com.ma.dlp.Repository.AgentCapabilityRepository;
import com.ma.dlp.Repository.AgentCommandRepository;
import com.ma.dlp.Repository.PolicyRepository;
import com.ma.dlp.Repository.UserRepository;
import com.ma.dlp.component.WebSocketNotifier;
import com.ma.dlp.controller.AdminController;
// Make sure this is imported
import com.ma.dlp.dto.*;
import com.ma.dlp.model.*;
import lombok.Data;
import org.aspectj.weaver.loadtime.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Service
public class AgentService {

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private AgentCapabilityRepository agentCapabilityRepository;

    @Autowired
    private AgentCommandRepository agentCommandRepository;

    // at top of class
    @Autowired
    private WebSocketNotifier webSocketNotifier;


    private static final Logger log = LoggerFactory.getLogger(AgentService.class);


    public final ConcurrentHashMap<String, Long> agentTokens = new ConcurrentHashMap<>();
    private final Map<String, PendingAgent> pendingAgents = new ConcurrentHashMap<>();
    // In service/AgentService.java
    private static final Set<String> NON_REMOVABLE_CAPABILITIES = Set.of(
            "POLICY_OCR_MONITOR"
    );

    private final Map<Long, FileBrowseResponseDTO> browseCache = new ConcurrentHashMap<>();
    private final Map<Long, SortedMap<Integer, List<com.ma.dlp.dto.FileSystemItemDTO>>> browseChunks = new ConcurrentHashMap<>();

    // In AgentService.java - Update saveAgentCapabilities method
    @Transactional
    public void saveAgentCapabilities(Long agentId, List<PolicyCapabilityDTO> capabilities) {
        log.error("🔥 saveAgentCapabilities HIT for agentId={}", agentId);
        log.error("🔥 Incoming capabilities from Rust = {}",
                capabilities.stream().map(PolicyCapabilityDTO::getCode).toList());
//        try {
            syncPoliciesWithCapabilities(capabilities);
//        } catch (Exception e) {
//            log.error("CRITICAL: Failed to sync capabilities to main policies table: {}", e.getMessage(), e);
            // We continue anyway, so the agent can still get capabilities
//        }
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found: " + agentId));

        // Step 1: Get current capabilities from the agent (what Rust agent actually has NOW)
        Set<String> currentCapabilityCodes = capabilities.stream()
                .map(PolicyCapabilityDTO::getCode)
                .collect(Collectors.toSet());

        // Step 2: Get existing capabilities from database
        List<AgentCapability> existingCapabilities = agentCapabilityRepository.findByAgentId(agentId);

        // Step 3: REMOVE capabilities that are no longer in the Rust agent
        List<AgentCapability> capabilitiesToRemove = existingCapabilities.stream()
                .filter(existing -> !currentCapabilityCodes.contains(existing.getCapabilityCode()))
                .collect(Collectors.toList());

        if (!capabilitiesToRemove.isEmpty()) {
//            agentCapabilityRepository.deleteAll(capabilitiesToRemove);
            for (AgentCapability cap : capabilitiesToRemove) {
                log.error("🔥 REMOVING capability = {}", cap.getCapabilityCode());
                // Structural capabilities are NEVER deleted
                if (NON_REMOVABLE_CAPABILITIES.contains(cap.getCapabilityCode())) {

                    // Mark inactive but keep capability
                    if (Boolean.TRUE.equals(cap.getIsActive())) {
                        cap.setIsActive(false);
                        agentCapabilityRepository.save(cap);
                    }

                    log.info("🔒 Kept structural capability '{}' for agent {} (set inactive)",
                            cap.getCapabilityCode(), agentId);

                } else {
                    // Dynamic capabilities can be removed
                    agentCapabilityRepository.delete(cap);

                    log.info("🗑️ Removed dynamic capability '{}' from agent {}",
                            cap.getCapabilityCode(), agentId);
                }
            }

            log.info("🗑️ Removed {} old capabilities from agent {}: {}",
                    capabilitiesToRemove.size(), agentId,
                    capabilitiesToRemove.stream()
                            .map(AgentCapability::getCapabilityCode)
                            .collect(Collectors.toList()));
        }

        // Step 4: Add new capabilities that don't exist yet
        Set<String> existingCodes = existingCapabilities.stream()
                .map(AgentCapability::getCapabilityCode)
                .collect(Collectors.toSet());

        for (PolicyCapabilityDTO capDto : capabilities) {
            if (!existingCodes.contains(capDto.getCode())) {
                AgentCapability agentCap = new AgentCapability();
                agentCap.setAgent(agent);
                agentCap.setCapabilityCode(capDto.getCode());
                agentCap.setName(capDto.getName());
                agentCap.setDescription(capDto.getDescription());
                agentCap.setCategory(capDto.getCategory());
                agentCap.setAction(capDto.getAction());
                agentCap.setTarget(capDto.getTarget());
                agentCap.setSeverity(capDto.getSeverity());
                agentCap.setIsActive(false);

                agentCapabilityRepository.save(agentCap);
                log.info("✅ Discovered and saved new capability '{}' for agent {}", capDto.getCode(), agent.getUsername());
            }
        }

        log.info("✅ Agent capability sync complete for agent: {}. Current: {}, Removed: {}",
                agent.getUsername(), capabilities.size(), capabilitiesToRemove.size());
    }

    // In AgentService.java - Add file system methods
    public List<AdminController.FileSystemItem> browseAgentFileSystem(Long agentId, String path) {
        // For now, return mock file system data
        List<AdminController.FileSystemItem> items = new ArrayList<>();

        if (path == null || path.isEmpty()) {
            // Root directories
            items.add(createFileItem("C:", "C:\\", true, 0));
            items.add(createFileItem("D:", "D:\\", true, 0));
            items.add(createFileItem("Users", "C:\\Users", true, 0));
            items.add(createFileItem("Windows", "C:\\Windows", true, 0));
            items.add(createFileItem("Program Files", "C:\\Program Files", true, 0));
        } else {
            // Simulate subdirectories
            items.add(createFileItem("Documents", path + "\\Documents", true, 0));
            items.add(createFileItem("Downloads", path + "\\Downloads", true, 0));
            items.add(createFileItem("Desktop", path + "\\Desktop", true, 0));
            items.add(createFileItem("file1.txt", path + "\\file1.txt", false, 1024));
            items.add(createFileItem("file2.doc", path + "\\file2.doc", false, 2048));
        }

        return items;
    }

    public boolean validateAgentPath(Long agentId, String path) {
        // For now, simulate path validation
        return path != null && !path.trim().isEmpty();
    }

    public String calculateRuntimeState(User agent) {

        // OFFLINE: no heartbeat in last 60 sec
        if (agent.getLastHeartbeat() == null ||
                agent.getLastHeartbeat().before(
                        new Date(System.currentTimeMillis() - 10_000)
                )) {
            return "OFFLINE";
        }

        // LOCKED
        if (Boolean.TRUE.equals(agent.getScreenLocked())) {
            return "LOCKED";
        }

        // IDLE
        if (Boolean.FALSE.equals(agent.getOcrActive())) {
            return "IDLE";
        }

        // ONLINE
        return "ONLINE";
    }


    private AdminController.FileSystemItem createFileItem(String name, String fullPath, boolean isDirectory, long size) {
        AdminController.FileSystemItem item = new AdminController.FileSystemItem();
        item.setName(name);
        item.setFullPath(fullPath);
        item.setDirectory(isDirectory);
        item.setSize(size);
        return item;
    }

    private void syncPoliciesWithCapabilities(List<PolicyCapabilityDTO> capabilities) {
        for (PolicyCapabilityDTO cap : capabilities) {
            // Check if a policy with this code already exists
            if (policyRepository.findByPolicyCode(cap.getCode()).isEmpty()) {
                // If it doesn't exist, create it
                Policy newPolicy = new Policy();
                newPolicy.setPolicyCode(cap.getCode());
                newPolicy.setName(cap.getName());
                newPolicy.setDescription(cap.getDescription());
                newPolicy.setCategory(cap.getCategory());

                // --- THESE ARE THE MISSING LINES ---
                newPolicy.setAction(cap.getAction());
                newPolicy.setTarget(cap.getTarget());
                newPolicy.setSeverity(cap.getSeverity());
                newPolicy.setPolicyType(cap.getAction() + "_" + cap.getCategory());
                newPolicy.setIsActive(false); // Master policy is a template, not active
                // ------------------------------------

                policyRepository.save(newPolicy);
                log.info("✅ Created new master policy from agent capability: {}", cap.getCode());
            }
        }
    }

    /**
     * Activate a capability for an agent
     */
    public void activateCapability(Long agentId, String capabilityCode, String policyData) {
        AgentCapability capability = agentCapabilityRepository
                .findByAgentIdAndCapabilityCode(agentId, capabilityCode)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Capability %s not found for agent %d", capabilityCode, agentId)));

        // --- THIS IS THE NEW CHECK ---
        if (Boolean.TRUE.equals(capability.getIsActive())) {
            // Get the agent's name to make the error message more user-friendly
            String agentName = capability.getAgent() != null ? capability.getAgent().getHostname() : "ID " + agentId;
            throw new IllegalStateException("Policy is already active for agent: " + agentName);
        }
        // --- END OF NEW CHECK ---

        capability.setIsActive(true);
        capability.setAssignedAt(new Date());
        capability.setPolicyData(policyData);
        agentCapabilityRepository.save(capability);

        log.info("✅ Activated capability '{}' for agent: {}", capabilityCode, agentId);
    }

    /**
     * Deactivate a capability for an agent
     */
    public void deactivateCapability(Long agentId, String capabilityCode) {
        AgentCapability capability = agentCapabilityRepository
                .findByAgentIdAndCapabilityCode(agentId, capabilityCode)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Capability %s not found for agent %d", capabilityCode, agentId)));

        capability.setIsActive(false);
        agentCapabilityRepository.save(capability);

        log.info("✅ Deactivated capability '{}' for agent: {}", capabilityCode, agentId);
    }

    /**
     * Get active capabilities for an agent
     */
    // public List<AgentCapability> getActiveCapabilities(Long agentId) {
    //     return agentCapabilityRepository.findActiveCapabilitiesByAgentId(agentId);
    // }
            // In AgentService.java
        public List<AgentCapability> getActiveCapabilities(Long agentId) {
            // Try different queries to see which one works
            List<AgentCapability> caps1 = agentCapabilityRepository.findByAgentId(agentId);
            System.out.println("All capabilities for agent " + agentId + ": " + caps1.size());
            
            List<AgentCapability> caps2 = agentCapabilityRepository.findByAgentIdAndIsActiveTrue(agentId);
            System.out.println("Active capabilities for agent " + agentId + ": " + caps2.size());
            
            return caps2; // Return the active ones
        }

    /**
     * Get all capabilities for an agent
     */
    public List<AgentCapability> getAllCapabilities(Long agentId) {
        return agentCapabilityRepository.findByAgentId(agentId);
    }


    public AgentDTO getAgentById(Long agentId) {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        // Use constructor instead of builder
        AgentDTO dto = new AgentDTO();
        dto.setId(agent.getId());
        dto.setHostname(agent.getUsername()); // or appropriate field
        dto.setStatus(agent.getStatus().toString());
        dto.setUsername(agent.getUsername());
        dto.setMacAddress(agent.getMacAddress());
        dto.setIpAddress(agent.getIpAddress());
        dto.setLastHeartbeat(agent.getLastHeartbeat());
        dto.setLastLogin(agent.getLastLogin());

        return dto;
    }

    /**
     * Get all agents policy statuses
     */
    public List<AgentPolicyStatusDTO> getAgentPolicyStatuses(String capabilityCode) {
        // 1. Find all capability records for this policy
        List<AgentCapability> capabilities = agentCapabilityRepository.findByCapabilityCode(capabilityCode);

        // 2. Convert them into the new DTO
        return capabilities.stream().map(cap -> {
            AgentPolicyStatusDTO dto = new AgentPolicyStatusDTO();
            dto.setAgentId(cap.getAgent().getId());
            dto.setHostname(cap.getAgent().getHostname());
            dto.setAgentStatus(cap.getAgent().getStatus().toString());
            dto.setIsPolicyActive(cap.getIsActive());
            return dto;
        }).collect(Collectors.toList());
    }


    public List<User> getAgentsWithCapability(String capabilityCode) {
        List<AgentCapability> capabilities = agentCapabilityRepository.findByCapabilityCode(capabilityCode);

        // Convert the list of AgentCapability objects to a distinct list of User objects
        return capabilities.stream()
                .map(AgentCapability::getAgent)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get capabilities grouped by category for dashboard
     */
    // In AgentService.java - Fix getCapabilitiesByCategory method
    // In AgentService.java - Fix the getCapabilitiesByCategory method
    public Map<String, List<PolicyCapabilityDTO>> getCapabilitiesByCategory(Long agentId) {
        Map<String, List<PolicyCapabilityDTO>> capabilitiesByCategory = new HashMap<>();

        try {
            // Get ONLY the capabilities this agent actually has from database
            List<AgentCapability> agentCapabilities = agentCapabilityRepository.findByAgentId(agentId);

            if (agentCapabilities.isEmpty()) {
                log.info("Agent {} has no capabilities registered", agentId);
                return capabilitiesByCategory; // Return empty map
            }

            // Group by category and convert to DTOs
            Map<String, List<AgentCapability>> capabilitiesByCategoryRaw = agentCapabilities.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(AgentCapability::getCategory));

            for (Map.Entry<String, List<AgentCapability>> entry : capabilitiesByCategoryRaw.entrySet()) {
                List<PolicyCapabilityDTO> dtos = entry.getValue().stream()
                        .map(this::convertToPolicyCapabilityDTO) // This should work now
                        .collect(Collectors.toList());
                capabilitiesByCategory.put(entry.getKey(), dtos);
            }

            log.info("📊 Agent {} has {} capabilities across {} categories",
                    agentId, agentCapabilities.size(), capabilitiesByCategory.size());

        } catch (Exception e) {
            log.error("Error getting capabilities for agent {}: {}", agentId, e.getMessage());
        }

        return capabilitiesByCategory;
    }

    // In AgentService.java - Add this method
    private PolicyCapabilityDTO convertToPolicyCapabilityDTO(AgentCapability capability) {
        PolicyCapabilityDTO dto = new PolicyCapabilityDTO();
        dto.setCode(capability.getCapabilityCode());
        dto.setName(capability.getName());
        dto.setDescription(capability.getDescription());
        dto.setCategory(capability.getCategory());
        dto.setAction(capability.getAction());
        dto.setTarget(capability.getTarget());
        dto.setSeverity(capability.getSeverity());
        dto.setIsActive(capability.getIsActive());
        dto.setPolicyData(capability.getPolicyData());
        return dto;
    }

    /**
     * Updates the data for an existing policy assignment
     * without changing its active status.
     */
    @Transactional
    public void updatePolicyData(Long agentId, String capabilityCode, String policyData) {
        AgentCapability capability = agentCapabilityRepository
                .findByAgentIdAndCapabilityCode(agentId, capabilityCode)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Capability %s not found for agent %d", capabilityCode, agentId)));

        // Set the new data
        capability.setPolicyData(policyData);
        agentCapabilityRepository.save(capability);

        log.info("✅ Updated policy data for '{}' on agent: {}", capabilityCode, agentId);
    }


    // ADD THIS NEW HELPER METHOD
    private PolicyCapabilityDTO convertToDto(AgentCapability entity) {
        PolicyCapabilityDTO dto = new PolicyCapabilityDTO();
        dto.setCode(entity.getCapabilityCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setAction(entity.getAction());
        dto.setTarget(entity.getTarget());
        dto.setSeverity(entity.getSeverity());
        dto.setIsActive(entity.getIsActive()); // Pass the status to the DTO

        dto.setPolicyData(entity.getPolicyData() != null ? entity.getPolicyData() : "");

        return dto;
    }


//    public AgentAuthResponse authenticateAgent(String hostname, String macAddress , String ipAddress) {
//        List<User> existingAgents = userRepository.findAllByMacAddress(macAddress);
//        User agent;
//
//        if (!existingAgents.isEmpty()) {
//            agent = existingAgents.stream()
//                    .max(Comparator.comparing(User::getLastLogin,
//                            Comparator.nullsFirst(Comparator.naturalOrder())))
//                    .orElse(existingAgents.get(0));
//            log.info("🔄 Existing agent found: {}", agent.getUsername());
//
////            // ✅ Update IP address if it's provided
////            if (ipAddress != null && !ipAddress.trim().isEmpty()) {
////                agent.setIpAddress(ipAddress);
////            }
//            // ✅ UPDATE agent info on login
//                        agent.setHostname(hostname);
//                        agent.setMacAddress(macAddress);
//                        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
//                            agent.setIpAddress(ipAddress);
//                        }
//            if (agent.getPlainPassword() == null) {
//                String newPassword = generateSecurePassword();
//                agent.setPlainPassword(newPassword);
//                agent.setPassword(passwordEncoder.encode(newPassword));
//                userRepository.save(agent);
//                log.info("🔑 Fixed null password for existing agent: {}", agent.getUsername());
//            }
//        } else {
////            agent = createAgent(hostname, macAddress, null, null ,null);
////            log.info("✅ New agent created: {}", agent.getUsername());
//
//            String username = "agent_" + hostname.toLowerCase().replace(" ", "_");
//            Optional<User> agentByUsername = userRepository.findByUsername(username);
//
//            if (agentByUsername.isPresent()) {
//                agent = agentByUsername.get();
//                // ✅ UPDATE agent info
//                agent.setHostname(hostname);
//                agent.setMacAddress(macAddress);
//                if (ipAddress != null && !ipAddress.trim().isEmpty()) {
//                    agent.setIpAddress(ipAddress);
//                }
//                userRepository.save(agent);
//                log.info("✅ Updated existing agent with hostname/mac/ip: {}", username);
//            } else {
//                // Create new agent if not found
//                agent = createAgent(hostname, macAddress, null, null, ipAddress);
//                log.info("✅ New agent created with login info: {}", hostname);
//            }
//        }
//
//        agent.setLastLogin(new Date());
//        String token = generateToken();
//        agent.setToken(token);
//        userRepository.save(agent);
//
////        String token = generateToken();
//        agentTokens.put(token, agent.getId());  // ✅ FIXED
//        agentTokens.put("Bearer " + token, agent.getId());
//        return new AgentAuthResponse(
//                agent.getId(),
//                agent.getUsername(),
//                agent.getPlainPassword(),
//                "ACTIVE",
//                token
//        );
//    }

    public AgentAuthResponse createAgentDirectly(String hostname, String macAddress, String username, String customPassword , String ipAddress , String email) {
        try {
            User agent = createAgent(hostname, macAddress, username, customPassword , ipAddress,email);

            String token = generateToken();
            agentTokens.put(token, agent.getId());  // ✅ FIXED

            log.info("✅ Admin directly created agent: {} (MAC: {})", hostname, macAddress);
            log.info("🔑 Agent credentials - Username: {}, Password: {}", agent.getUsername(), agent.getPlainPassword());

            return new AgentAuthResponse(
                    agent.getId(),
                    agent.getUsername(),
                    agent.getPlainPassword(),
                    "ACTIVE",
                    token
            );
        } catch (Exception e) {
            log.error("❌ Failed to create agent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create agent: " + e.getMessage());
        }
    }

    public void clearAgentToken(Long agentId) {
        // Remove all tokens for this agent from cache
        List<String> tokensToRemove = new ArrayList<>();
        for (Map.Entry<String, Long> entry : agentTokens.entrySet()) {
            if (entry.getValue().equals(agentId)) {
                tokensToRemove.add(entry.getKey());
            }
        }
        tokensToRemove.forEach(agentTokens::remove);

        // Clear token from database
        userRepository.findById(agentId).ifPresent(agent -> {
            agent.setToken(null);
            userRepository.save(agent);
        });

        log.info("🔐 Cleared tokens for agent: {}", agentId);
    }


//    public boolean validateToken(String token, Long agentId) {
//        if (token == null || token.isBlank()) return false;
//
//        log.info("🔐 Validating token for agent {}: '{}'", agentId, token);
//
//        // Accept both "Bearer <token>" and "<token>"
////        String cleaned = token.replace("Bearer", "").trim();
//        String cleaned = token.replaceAll("(?i)^Bearer\\s+", "").trim();
//        log.info("🔐 Cleaned token: '{}'", cleaned);
//
//        // Check if token exists in agentTokens store
//        Long storedAgentId = agentTokens.get(cleaned);
//        boolean isValid = storedAgentId != null && storedAgentId.equals(agentId);
//
////        return storedAgentId != null && storedAgentId.equals(agentId);
//        log.info("🔐 Token validation result: {}", isValid);
//        log.info("🔐 Stored agent ID for token: {}", storedAgentId);
//
//        return isValid;
//    }

public boolean validateToken(String token, Long agentId) {
    if (token == null || token.isBlank()) return false;

    log.info("🔐 Validating token for agent {}: '{}'", agentId, token);

    // Always clean the token the same way
    String cleaned = cleanToken(token);

    // Check BOTH with and without "Bearer " prefix
    Long storedAgentId = agentTokens.get(cleaned);

    // Also check if token was stored with "Bearer " prefix
    if (storedAgentId == null && cleaned.startsWith("Bearer ")) {
        String withoutBearer = cleaned.substring(7).trim();
        storedAgentId = agentTokens.get(withoutBearer);
    }

    // 🔴 NEW: If not found in cache, check database
    if (storedAgentId == null) {
        Optional<User> agentOpt = userRepository.findById(agentId);
        if (agentOpt.isPresent() && cleaned.equals(agentOpt.get().getToken())) {
            // Token found in database, add to cache
            storedAgentId = agentId;
            agentTokens.put(cleaned, agentId);
            log.info("🔐 Token loaded from database and cached");
        }
    }

    boolean isValid = storedAgentId != null && storedAgentId.equals(agentId);

    log.info("🔐 Token validation result: {}", isValid);
    log.info("🔐 Stored agent ID for token: {}", storedAgentId);

    return isValid;
}

//    public String cleanToken(String token) {
//        if (token == null) return null;
//        // Remove "Bearer " prefix if present
//        return token.replaceAll("(?i)^Bearer\\s+", "").trim();
//    }

    public String cleanToken(String tokenHeader) {
        if (tokenHeader == null) return null;
        if (tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7).trim();
        }
        return tokenHeader.trim();
    }



//    public AgentAuthResponse loginWithCredentials(String username, String password ,String hostname, String macAddress, String ipAddress) {
//
//        Optional<User> agentOpt = userRepository.findByUsername(username);
//        if (agentOpt.isEmpty())  {
//            throw new RuntimeException("Agent not found");
//        }
//
//        User agent = agentOpt.get();
//
//        if (!passwordEncoder.matches(password, agent.getPassword())) {
//            throw new RuntimeException("Invalid credentials");
//        }
//
//        if (agent.getStatus() != User.UserStatus.ACTIVE) {
//            throw new RuntimeException("Agent account is not active");
//        }
//
//        agent.setHostname(hostname);
//        agent.setMacAddress(macAddress);
//        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
//            agent.setIpAddress(ipAddress);
//        }
//
//        // Fix null plainPassword if needed
//        if (agent.getPlainPassword() == null) {
//            String newPassword = generateSecurePassword();
//            agent.setPlainPassword(newPassword);
//            log.info("🔑 Fixed null plainPassword for agent: {}", agent.getUsername());
//        }
//
//
//        String token = generateToken();
//        agent.setToken(token);// ✅ FIXED
//
//        agent.setLastLogin(new Date());
//
//        userRepository.save(agent);
//
//        // Store in memory cache
//        agentTokens.put(token, agent.getId());
//
//        // Also store with "Bearer " prefix for compatibility
//        agentTokens.put("Bearer " + token, agent.getId());
//
//        log.info("✅ Agent logged in with credentials: {} from {} (MAC: {}, IP: {})", username , hostname, macAddress, ipAddress);
//        log.info("🔐 Token generated and saved for agent {}: {}", agent.getId(), token);
//
//        return new AgentAuthResponse(
//                agent.getId(),
//                agent.getUsername(),
//                agent.getPlainPassword(),
//                agent.getStatus().toString(),
//                token
//        );
//    }

    public AgentAuthResponse loginWithCredentials(String username, String password,
                                                  String hostname, String macAddress,
                                                  String ipAddress) {

        Optional<User> agentOpt = userRepository.findByUsername(username);
        if (agentOpt.isEmpty()) {
            throw new RuntimeException("Agent not found");
        }

        User agent = agentOpt.get();

        // Use the passwordEncoder from AgentService
        if (!passwordEncoder.matches(password, agent.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (agent.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("Agent account is not active");
        }

        // Update agent info
        if (hostname != null && !hostname.trim().isEmpty() &&
                (agent.getHostname() == null || agent.getHostname().trim().isEmpty())) {
            agent.setHostname(hostname);
        }

        if (macAddress != null && !macAddress.trim().isEmpty() &&
                (agent.getMacAddress() == null || agent.getMacAddress().trim().isEmpty())) {
            agent.setMacAddress(macAddress);
        }

        // ✅ FIXED: ALWAYS update IP address when provided, even if it's changing
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            // Log the IP change for debugging
            if (agent.getIpAddress() != null && !agent.getIpAddress().equals(ipAddress)) {
                log.info("🔀 Updating agent IP from {} to {}", agent.getIpAddress(), ipAddress);
            }
            agent.setIpAddress(ipAddress);
        }

        String token = generateToken();
        agent.setToken(token);
        agent.setLastLogin(new Date());
        agent.setLastHeartbeat(new Date());

        userRepository.save(agent);

        // Store in cache
        agentTokens.put(token, agent.getId());
        agentTokens.put("Bearer " + token, agent.getId());

        log.info("✅ Agent logged in: {} (ID: {})", agent.getUsername(), agent.getId(), agent.getIpAddress());

        return new AgentAuthResponse(
                agent.getId(),
                agent.getUsername(),
                agent.getPlainPassword(),
                agent.getStatus().toString(),
                token
        );
    }

    // In AgentService.java - add detailed logging to createAgent method
//    private User createAgent(String hostname, String macAddress, String customUsername, String customPassword ,String ipAddress) {
//        log.info("🔍 CREATE_AGENT DEBUG - Start:");
//        log.info("  Hostname: {}", hostname);
//        log.info("  MAC: {}", macAddress);
//        log.info("  IP Address: {}", ipAddress);
//        log.info("  Custom Username: {}", customUsername);
//        log.info("  Custom Password: {}", customPassword);
//
//        List<User> existingAgents = userRepository.findAllByMacAddress(macAddress);
//        log.info("  Existing agents found: {}", existingAgents.size());
//
//        if (!existingAgents.isEmpty()) {
//            User existingAgent = existingAgents.get(0);
//            log.info("  🔄 Returning existing agent: {}", existingAgent.getUsername());
//            log.info("  Existing agent plainPassword: {}", existingAgent.getPlainPassword());
//            // ✅ Update IP address for existing agent
//            if (ipAddress != null && !ipAddress.trim().isEmpty()) {
//                existingAgent.setIpAddress(ipAddress);
//                userRepository.save(existingAgent);
//                log.info("  Updated IP address for existing agent: {}", ipAddress);
//            }
//            return existingAgent;
//        }
//
//        String username = customUsername != null ? customUsername :
//                "agent_" + hostname.toLowerCase().replace(" ", "_");
//        log.info("  Final username: {}", username);
//
//        String plainPassword = (customPassword != null && !customPassword.trim().isEmpty()) ?
//                customPassword : generateSecurePassword();
//        log.info("  Final plainPassword: {}", plainPassword);
//
//        User agent = new User();
//        agent.setUsername(username);
//        agent.setPassword(passwordEncoder.encode(plainPassword));
//        agent.setRole(User.UserRole.AGENT);
//        agent.setStatus(User.UserStatus.ACTIVE);
//        agent.setHostname(hostname);
//        agent.setMacAddress(macAddress);
//        agent.setIpAddress(ipAddress);
//        agent.setLastHeartbeat(new Date());
//        agent.setPlainPassword(plainPassword);
//
//        log.info("  Before save - agent.plainPassword: {}", agent.getPlainPassword());
//
//        User savedAgent = userRepository.save(agent);
//
//        log.info("  After save - savedAgent.plainPassword: {}", savedAgent.getPlainPassword());
//        log.info("  ✅ Agent created successfully");
//
//        return savedAgent;
//    }

    private User createAgent(String hostname, String macAddress, String customUsername,
                             String customPassword, String ipAddress , String email) {
        log.info("🔍 CREATE_AGENT - Admin creating agent");
        log.info("  Username: {}", customUsername);
        log.info("  Hostname: {}", hostname);
        log.info("  MAC: {}", macAddress);
        log.info("  IP: {}", ipAddress);

        // 1. VALIDATE: Username and password are REQUIRED
        if (customUsername == null || customUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (customPassword == null || customPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        String username = customUsername.trim();
        String password = customPassword.trim();

        // 2. CHECK: If username already exists (prevent duplicates)
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        // 3. CREATE NEW AGENT
        User agent = new User();
        agent.setUsername(username);
        agent.setPassword(passwordEncoder.encode(password));
        agent.setPlainPassword(password); // Store plain password for agent to use
        agent.setRole(User.UserRole.AGENT);
        agent.setStatus(User.UserStatus.ACTIVE);
        agent.setEmail(email);
        // Set optional fields if provided (can be null)
        if (hostname != null && !hostname.trim().isEmpty()) {
            agent.setHostname(hostname);
        }
        if (macAddress != null && !macAddress.trim().isEmpty()) {
            agent.setMacAddress(macAddress);
        }
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            agent.setIpAddress(ipAddress);
        }

        agent.setLastHeartbeat(null);
        agent.setLastLogin(new Date());

        User savedAgent = userRepository.save(agent);
        
        log.info("  ✅ Created new agent with ID: {}", savedAgent.getId());
        log.info("  Username: {}, Password: {}", savedAgent.getUsername(), savedAgent.getPlainPassword());

        return savedAgent;
    }

    public User updateAgent(Long agentId, String username, String password, String email) {
        User agent = userRepository.findById(agentId)
            .orElseThrow(() -> new RuntimeException("Agent not found"));
        
        if (username != null && !username.trim().isEmpty()) {
            agent.setUsername(username);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            agent.setEmail(email);
        }
        
        if (password != null && !password.trim().isEmpty()) {
            // Encode the password
            agent.setPassword(passwordEncoder.encode(password));
            // Store plain password if needed (for agent auth)
            agent.setPlainPassword(password);
        }
        
        return userRepository.save(agent);
    }

    private String generateSecurePassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    public void updateHeartbeat(Long agentId) {
        userService.updateHeartbeat(agentId);
    }

    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public Map<String, List<String>> getFilePolicies(Long agentId) {
        List<AgentCapability> activeCapabilities = getActiveCapabilities(agentId);
        Map<String, List<String>> filePolicies = new HashMap<>();

        for (AgentCapability capability : activeCapabilities) {
            if ("FILE".equals(capability.getCategory()) && capability.getPolicyData() != null) {
                String policyCode = capability.getCapabilityCode();
                List<String> items = Arrays.asList(capability.getPolicyData().split(","));

                // Map policy codes to appropriate keys based on Rust implementation
                if (policyCode.contains("BLOCK_EXTENSIONS")) {
                    if (policyCode.contains("CREATE")) {
                        filePolicies.put("block_extensions_create", items);
                    } else if (policyCode.contains("WRITE")) {
                        filePolicies.put("block_extensions_write", items);
                    } else if (policyCode.contains("DELETE")) {
                        filePolicies.put("block_extensions_delete", items);
                    } else if (policyCode.contains("READ")) {
                        filePolicies.put("block_extensions_read", items);
                    } else if (policyCode.contains("COPY")) {
                        filePolicies.put("block_extensions_copy", items);
                    } else if (policyCode.contains("MOVE")) {
                        filePolicies.put("block_extensions_move", items);
                    } else if (policyCode.contains("RENAME")) {
                        filePolicies.put("block_extensions_rename", items);
                    } else if (policyCode.contains("OPEN")) {
                        filePolicies.put("block_extensions_open", items);
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
                    } else if (policyCode.contains("COPY")) {
                        filePolicies.put("block_paths_copy", items);
                    } else if (policyCode.contains("MOVE")) {
                        filePolicies.put("block_paths_move", items);
                    } else if (policyCode.contains("RENAME")) {
                        filePolicies.put("block_paths_rename", items);
                    } else if (policyCode.contains("OPEN")) {
                        filePolicies.put("block_paths_open", items);
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
                } else if (policyCode.contains("BLOCK_RENAME")) {
                    filePolicies.put("block_rename", items);
                } else if (policyCode.contains("BLOCK_COPY")) {
                    filePolicies.put("block_copy", items);
                } else if (policyCode.contains("BLOCK_MOVE")) {
                    filePolicies.put("block_move", items);
                } else if (policyCode.contains("BLOCK_OPEN")) {
                    filePolicies.put("block_open", items);
                }
                // Add other policy mappings as needed
            }
        }

        log.info("📋 Generated file policies for agent {}: {} policy types", agentId, filePolicies.size());

        // Ensure all expected keys exist to avoid null pointers in Rust
        ensureDefaultKeys(filePolicies);

        return filePolicies;
    }

    // Helper method to ensure all expected policy keys exist
    private void ensureDefaultKeys(Map<String, List<String>> filePolicies) {
        String[] expectedKeys = {
                // Global operation blocking
                "block_create", "block_read", "block_write", "block_delete",
                "block_rename", "block_copy", "block_move", "block_open",

                // Extension-based blocking
                "block_extensions_create", "block_extensions_read", "block_extensions_write",
                "block_extensions_delete", "block_extensions_rename", "block_extensions_copy",
                "block_extensions_move", "block_extensions_open",

                // Path-based blocking
                "block_paths_create", "block_paths_read", "block_paths_write",
                "block_paths_delete", "block_paths_rename", "block_paths_copy",
                "block_paths_move", "block_paths_open",

                // Read-only policies
                "readonly_extensions", "readonly_paths"
        };

        for (String key : expectedKeys) {
            filePolicies.putIfAbsent(key, new ArrayList<>());
        }
    }
    /*
     browse
     */
    public void storeBrowseResponse(Long agentId, FileBrowseResponseDTO dto) {
        if (dto == null || agentId == null) return;


        Boolean partial = dto.getPartial() != null ? dto.getPartial() : false;
        Boolean complete = dto.getComplete() != null ? dto.getComplete() : false;
        Integer chunkId = dto.getChunkId();


        if (Boolean.TRUE.equals(partial) && chunkId != null) {
            log.info("📥 Received partial chunk {} for agent {}", chunkId, agentId);
            browseChunks.compute(agentId, (k, v) -> {
                if (v == null) v = new TreeMap<>();
                v.put(chunkId, dto.getItems() != null ? dto.getItems() : Collections.emptyList());
                return v;
            });


            // streaming preview cache
            List<FileSystemItemDTO> flattened = new ArrayList<>();
            SortedMap<Integer, List<com.ma.dlp.dto.FileSystemItemDTO>> map = browseChunks.get(agentId);
            if (map != null) map.values().forEach(list -> { if (list != null) flattened.addAll(list); });


            FileBrowseResponseDTO streaming = new FileBrowseResponseDTO();
            streaming.setAgentId(agentId);
            streaming.setCurrentPath(dto.getCurrentPath());
            streaming.setParentPath(dto.getParentPath());
            streaming.setItems(flattened);
            streaming.setPartial(true);
            streaming.setComplete(false);


            browseCache.put(agentId, streaming);
            return;
        }


        if (Boolean.TRUE.equals(complete)) {
            log.info("📦 Received final browse response for agent {}", agentId);
            List<com.ma.dlp.dto.FileSystemItemDTO> finalItems = new ArrayList<>();
            SortedMap<Integer, List<com.ma.dlp.dto.FileSystemItemDTO>> map = browseChunks.remove(agentId);
            if (map != null) map.values().forEach(list -> { if (list != null) finalItems.addAll(list); });


            if (dto.getItems() != null && !dto.getItems().isEmpty()) finalItems.addAll(dto.getItems());


            FileBrowseResponseDTO finalDto = new FileBrowseResponseDTO();
            finalDto.setAgentId(agentId);
            finalDto.setCurrentPath(dto.getCurrentPath());
            finalDto.setParentPath(dto.getParentPath());
            finalDto.setItems(finalItems);
            finalDto.setPartial(false);
            finalDto.setComplete(true);
            finalDto.setChunkId(null);


            browseCache.put(agentId, finalDto);
            return;
        }


// one-shot
        browseCache.put(agentId, dto);
    }



    public FileBrowseResponseDTO getBrowseResponse(Long agentId) {
        return browseCache.get(agentId);
    }

    @Transactional
    public void markBrowseCommandProcessed(Long agentId) {
        Optional<AgentCommand> opt = agentCommandRepository
                .findFirstByAgentIdAndProcessedFalseOrderByCreatedAtAsc(agentId);

        if (opt.isEmpty()) {
            log.debug("No pending AgentCommand found to mark processed for agent {}", agentId);
            return;
        }

        AgentCommand cmd = opt.get();
        cmd.setProcessed(true);
        // if AgentCommand has setProcessedAt
        try { cmd.setProcessedAt(LocalDateTime.now()); } catch (Exception ignored) {}
        agentCommandRepository.save(cmd);
        log.info("Marked AgentCommand id={} as processed for agent {}", cmd.getId(), agentId);
    }

    @Transactional
    public AgentCommand sendBrowseCommand(Long agentId, String path) {
        AgentCommand cmd = new AgentCommand();
        cmd.setAgentId(agentId);
        cmd.setCommand("FILE_BROWSE");
        cmd.setPath(path == null ? "" : path);
        cmd.setProcessed(false);
        cmd.setCreatedAt(LocalDateTime.now());

        agentCommandRepository.save(cmd);
        log.info("📤 Enqueued browse command id={} for agent {} path='{}'", cmd.getId(), agentId, cmd.getPath());
        return cmd;
    }

    // Add these two methods at the end to implement the abstract ones:
    public int countActivePolicies() {
        Long count = agentCapabilityRepository.countByIsActiveTrue();
        return count != null ? count.intValue() : 0;
    }

    public int countPendingReviewPolicies() {
        // Implement based on your business logic
        // For example, count capabilities that are active but need review
        // or have a specific status
        return 0; // Temporary - implement as needed
    }

}

