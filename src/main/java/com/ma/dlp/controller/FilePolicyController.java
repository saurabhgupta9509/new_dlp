package com.ma.dlp.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ma.dlp.Repository.FilePolicyRepository;
import com.ma.dlp.dto.ApplyPolicyRequestDTO;
import com.ma.dlp.dto.PolicyResponse;
import com.ma.dlp.model.FilePolicy;
import com.ma.dlp.service.AgentClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/policies")
@Slf4j
public class FilePolicyController {

    @Autowired
    private AgentClient agentClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FilePolicyRepository filePolicyRepository;

    private static final Logger log =
            LoggerFactory.getLogger(FilePolicyController.class);
    /**
     * GET /admin/api/policies - Show ONLY active policies
     */
    @GetMapping()
    public ResponseEntity<Map<String, Object>> listPolicies() {
        log.info("GET /policies");

        // Get ONLY active policies
        List<FilePolicy> activePolicies = filePolicyRepository.findByIsActiveTrue();
        log.info("Found {} active policies", activePolicies.size());

        List<PolicyResponse> responses = activePolicies.stream()
                .map(policy -> {
                    PolicyResponse response = new PolicyResponse();
                    response.setId(policy.getId());                    // Database ID
                    response.setNodeId(policy.getNodeId());            // Node ID
                    response.setPolicyId(policy.getAgentPolicyId()); // Rust Agent Policy ID
                    response.setScope(policy.getScope());
                    response.setAction(policy.getAction());

                    try {
                        Map<String, Boolean> ops = objectMapper.readValue(
                                policy.getOperations(),
                                Map.class
                        );
                        response.setOperations(ops);
                    } catch (Exception e) {
                        response.setOperations(new HashMap<>());
                    }

                    response.setCreatedBy(policy.getCreatedBy());
                    response.setComment(policy.getComment());
                    response.setConfirmed(policy.getConfirmed());
                    response.setCreatedAt(policy.getCreatedAt());

                    return response;
                })
                .collect(Collectors.toList());

        // Return with consistent format
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("data", responses);
        responseMap.put("count", responses.size());

        return ResponseEntity.ok(responseMap);
    }

    /**
     * POST /admin/api/policies/apply - WITH DUPLICATE PREVENTION
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyPolicy(@RequestBody ApplyPolicyRequestDTO request) {
        log.info("POST /apply - nodeId: {}", request.getNode_id());

        try {
            // ✅ CHECK 1: Does this node already have ANY active policy?
            boolean hasActivePolicy = filePolicyRepository.existsByNodeIdAndActive(request.getNode_id());

            if (hasActivePolicy) {
                // Get the LATEST active policy for this node
                Optional<FilePolicy> existingPolicyOpt = filePolicyRepository.findFirstByNodeIdAndActive(
                        request.getNode_id()
                );

                if (existingPolicyOpt.isPresent()) {
                    FilePolicy oldPolicy = existingPolicyOpt.get();

                    // Compare operations
                    Map<String, Boolean> oldOps = objectMapper.readValue(
                            oldPolicy.getOperations(), Map.class);
                    Map<String, Boolean> newOps = request.getOperations();

                    if (oldOps.equals(newOps)) {
                        // SAME POLICY ALREADY EXISTS
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                Map.of(
                                        "error", "Policy already exists",
                                        "existing_policy_id", oldPolicy.getId(),
                                        "message", "Same policy is already active for this node"
                                )
                        );
                    } else {
                        // DIFFERENT POLICY - Get ALL active policies for this node
                        List<FilePolicy> allActivePolicies = filePolicyRepository.findByNodeIdAndIsActiveTrue(
                                request.getNode_id()
                        );

                        // Return ALL existing policies for conflict resolution
                        List<Map<String, Object>> existingPolicies = allActivePolicies.stream()
                                .map(p -> {
                                    try {
                                        Map<String, Boolean> ops = objectMapper.readValue(
                                                p.getOperations(), Map.class);
                                        return Map.of(
                                                "id", p.getId(),
                                                "operations", ops,
                                                "created_at", p.getCreatedAt()
                                        );
                                    } catch (Exception e) {
                                        return Map.of("id", p.getId(), "operations", Map.of());
                                    }
                                })
                                .collect(Collectors.toList());

                        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                Map.of(
                                        "conflict", true,
                                        "existing_policies", existingPolicies,
                                        "new_operations", newOps,
                                        "message", "Found " + allActivePolicies.size() +
                                                " active policies for this node. Replace them?"
                                )
                        );
                    }
                }
            }

            // ✅ STEP 1: Create new policy in database
            FilePolicy policy = new FilePolicy();
            policy.setNodeId(request.getNode_id());
            policy.setScope(request.getScope());
            policy.setAction(request.getAction());
            policy.setOperations(objectMapper.writeValueAsString(request.getOperations()));
            policy.setCreatedBy(request.getCreated_by());
            policy.setComment(request.getComment());
            policy.setConfirmed(request.getConfirmed() != null ? request.getConfirmed() : false);
            policy.setCreatedAt(System.currentTimeMillis() / 1000);
            policy.setUpdatedAt(System.currentTimeMillis() / 1000);
            policy.setIsActive(true);

            FilePolicy savedPolicy = filePolicyRepository.save(policy);
            log.info("Saved policy to DB with ID: {}", savedPolicy.getId());

            // ✅ STEP 2: Send to Rust Agent
            String agentResponse = agentClient.post("/api/v1/policies/apply", request).block();

            // ✅ STEP 3: Parse agent response and update database
            JsonNode json = objectMapper.readTree(agentResponse);
            Long agentPolicyId = null;

            if (json.has("data") && json.get("data").has("policy_id")) {
                agentPolicyId = json.get("data").get("policy_id").asLong();
            } else if (json.has("policy_id")) {
                agentPolicyId = json.get("policy_id").asLong();
            }

            if (agentPolicyId != null) {
                savedPolicy.setAgentPolicyId(agentPolicyId);
                savedPolicy.setAgentSynced(true);
                filePolicyRepository.save(savedPolicy);
                log.info("Updated with agentPolicyId: {}", agentPolicyId);
            }

            return ResponseEntity.ok(agentResponse);

        } catch (JsonProcessingException e) {
            log.error("JSON error", e);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid JSON: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("Failed to apply policy", e);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to apply policy: " + e.getMessage())
            );
        }
    }

    /**
     * POST /admin/api/policies/replace - Replace existing policy
     */
    @PostMapping("/replace")
    public ResponseEntity<?> replacePolicy(@RequestBody Map<String, Object> request) {
        log.info("POST /policies/replace");

        try {
            Long nodeId = Long.valueOf(request.get("node_id").toString());
            Map<String, Boolean> newOperations = (Map<String, Boolean>) request.get("operations");
            String scope = (String) request.get("scope");
            String action = (String) request.get("action");

            // 1. Get ALL existing active policies for this node
            List<FilePolicy> existingPolicies = filePolicyRepository.findByNodeIdAndIsActiveTrue(nodeId);

            if (existingPolicies.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "No existing policies found for this node")
                );
            }

            // 2. Remove ALL existing policies from Rust Agent
            for (FilePolicy oldPolicy : existingPolicies) {
                if (oldPolicy.getAgentPolicyId() != null) {
                    try {
                        agentClient.delete("/api/v1/policies/" + oldPolicy.getAgentPolicyId()).block();
                    } catch (Exception e) {
                        log.warn("Failed to remove policy {} from agent: {}",
                                oldPolicy.getId(), e.getMessage());
                    }
                }
                // Deactivate in database
                oldPolicy.setIsActive(false);
                filePolicyRepository.save(oldPolicy);
            }

            // 3. Create new policy
            ApplyPolicyRequestDTO newRequest = new ApplyPolicyRequestDTO();
            newRequest.setNode_id(nodeId);
            newRequest.setScope(scope);
            newRequest.setAction(action);
            newRequest.setOperations(newOperations);
            newRequest.setCreated_by((String) request.get("created_by"));
            newRequest.setComment("Replaced existing policies: " + request.get("comment"));
            newRequest.setConfirmed(true);

            // 4. Apply new policy
            FilePolicy newPolicy = new FilePolicy();
            newPolicy.setNodeId(nodeId);
            newPolicy.setScope(scope);
            newPolicy.setAction(action);
            newPolicy.setOperations(objectMapper.writeValueAsString(newOperations));
            newPolicy.setCreatedBy((String) request.get("created_by"));
            newPolicy.setComment("Replaced existing policies");
            newPolicy.setConfirmed(true);
            newPolicy.setCreatedAt(System.currentTimeMillis() / 1000);
            newPolicy.setUpdatedAt(System.currentTimeMillis() / 1000);
            newPolicy.setIsActive(true);

            FilePolicy savedPolicy = filePolicyRepository.save(newPolicy);

            // 5. Send to Rust Agent
            String agentResponse = agentClient.post("/api/v1/policies/apply", newRequest).block();

            JsonNode json = objectMapper.readTree(agentResponse);
            Long agentPolicyId = json.get("data").get("policy_id").asLong();

            savedPolicy.setAgentPolicyId(agentPolicyId);
            savedPolicy.setAgentSynced(true);
            filePolicyRepository.save(savedPolicy);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Policy replaced successfully",
                            "replaced_count", existingPolicies.size(),
                            "new_policy_id", savedPolicy.getId()
                    )
            );

        } catch (Exception e) {
            log.error("Failed to replace policy", e);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to replace policy: " + e.getMessage())
            );
        }
    }

    /**
     * POST /admin/api/policies/merge - Merge with existing policies
     */
    @PostMapping("/merge")
    public ResponseEntity<?> mergePolicy(@RequestBody Map<String, Object> request) {
        log.info("POST /policies/merge");

        try {
            Long nodeId = Long.valueOf(request.get("node_id").toString());
            Map<String, Boolean> newOperations = (Map<String, Boolean>) request.get("operations");
            String scope = (String) request.get("scope");
            String action = (String) request.get("action");

            // 1. Get ALL existing active policies for this node
            List<FilePolicy> existingPolicies = filePolicyRepository.findByNodeIdAndIsActiveTrue(nodeId);

            if (existingPolicies.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "No existing policies found for this node")
                );
            }

            // 2. Merge operations from ALL existing policies
            Map<String, Boolean> mergedOperations = new HashMap<>();

            // Start with new operations
            mergedOperations.putAll(newOperations);

            // Merge with all existing policies (OR operation)
            for (FilePolicy existing : existingPolicies) {
                try {
                    Map<String, Boolean> existingOps = objectMapper.readValue(
                            existing.getOperations(), Map.class);

                    // Merge: if any policy blocks an operation, keep it blocked
                    for (Map.Entry<String, Boolean> entry : existingOps.entrySet()) {
                        if (entry.getValue() == true) {
                            mergedOperations.put(entry.getKey(), true);
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to parse operations for policy {}", existing.getId(), e);
                }
            }

            // 3. Remove ALL existing policies
            for (FilePolicy oldPolicy : existingPolicies) {
                if (oldPolicy.getAgentPolicyId() != null) {
                    try {
                        agentClient.delete("/api/v1/policies/" + oldPolicy.getAgentPolicyId()).block();
                    } catch (Exception e) {
                        log.warn("Failed to remove policy {} from agent: {}",
                                oldPolicy.getId(), e.getMessage());
                    }
                }
                // Deactivate in database
                oldPolicy.setIsActive(false);
                filePolicyRepository.save(oldPolicy);
            }

            // 4. Create merged policy
            ApplyPolicyRequestDTO mergedRequest = new ApplyPolicyRequestDTO();
            mergedRequest.setNode_id(nodeId);
            mergedRequest.setScope(scope);
            mergedRequest.setAction(action);
            mergedRequest.setOperations(mergedOperations);
            mergedRequest.setCreated_by((String) request.get("created_by"));
            mergedRequest.setComment("Merged with existing policies: " + request.get("comment"));
            mergedRequest.setConfirmed(true);

            FilePolicy mergedPolicy = new FilePolicy();
            mergedPolicy.setNodeId(nodeId);
            mergedPolicy.setScope(scope);
            mergedPolicy.setAction(action);
            mergedPolicy.setOperations(objectMapper.writeValueAsString(mergedOperations));
            mergedPolicy.setCreatedBy((String) request.get("created_by"));
            mergedPolicy.setComment("Merged policy");
            mergedPolicy.setConfirmed(true);
            mergedPolicy.setCreatedAt(System.currentTimeMillis() / 1000);
            mergedPolicy.setUpdatedAt(System.currentTimeMillis() / 1000);
            mergedPolicy.setIsActive(true);

            FilePolicy savedPolicy = filePolicyRepository.save(mergedPolicy);

            // 5. Send merged policy to Rust Agent
            String agentResponse = agentClient.post("/api/v1/policies/apply", mergedRequest).block();

            JsonNode json = objectMapper.readTree(agentResponse);
            Long agentPolicyId = json.get("data").get("policy_id").asLong();

            savedPolicy.setAgentPolicyId(agentPolicyId);
            savedPolicy.setAgentSynced(true);
            filePolicyRepository.save(savedPolicy);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Policies merged successfully",
                            "merged_count", existingPolicies.size(),
                            "merged_operations", mergedOperations,
                            "new_policy_id", savedPolicy.getId()
                    )
            );

        } catch (Exception e) {
            log.error("Failed to merge policies", e);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to merge policies: " + e.getMessage())
            );
        }
    }

    /**
     * DELETE /admin/api/policies/node/{nodeId} - Remove ALL active policies for node
     */
    @DeleteMapping("/node/{nodeId}")
    public ResponseEntity<?> removeAllPoliciesForNode(@PathVariable Long nodeId) {
        log.info("DELETE /policies/node/{}", nodeId);

        try {
            // Get ALL active policies for this node
            List<FilePolicy> activePolicies = filePolicyRepository.findByNodeIdAndIsActiveTrue(nodeId);

            if (activePolicies.isEmpty()) {
                return ResponseEntity.ok(
                        Map.of("message", "No active policies found for node " + nodeId)
                );
            }

            int removedCount = 0;

            // Remove each policy from Rust Agent and database
            for (FilePolicy policy : activePolicies) {
                // Remove from Rust Agent
                if (policy.getAgentPolicyId() != null) {
                    try {
                        agentClient.delete("/api/v1/policies/" + policy.getAgentPolicyId()).block();
                    } catch (Exception e) {
                        log.warn("Failed to remove policy {} from agent: {}",
                                policy.getId(), e.getMessage());
                    }
                }

                // Completely delete from database
                filePolicyRepository.delete(policy);
                removedCount++;
            }

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Removed " + removedCount + " policies for node " + nodeId,
                            "removed_count", removedCount
                    )
            );

        } catch (Exception e) {
            log.error("Failed to remove policies for node", e);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to remove policies: " + e.getMessage())
            );
        }
    }

    /**
     * DELETE /admin/api/policies/{id} - Remove policy COMPLETELY
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removePolicy(@PathVariable Long id) {
        log.info("DELETE /policies/{}", id);

        try {
            // Find the policy
            Optional<FilePolicy> policyOpt = filePolicyRepository.findById(id);
            if (!policyOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            FilePolicy policy = policyOpt.get();

            // Remove from Rust Agent if it has agentPolicyId
            if (policy.getAgentPolicyId() != null) {
                try {
                    agentClient.delete("/api/v1/policies/" + policy.getAgentPolicyId()).block();
                    log.info("Removed from Rust Agent: {}", policy.getAgentPolicyId());
                } catch (Exception e) {
                    log.warn("Failed to remove from Rust Agent: {}", e.getMessage());
                }
            }

            // COMPLETELY DELETE from database (not just mark inactive)
            filePolicyRepository.delete(policy);
            log.info("Completely deleted policy ID: {}", id);

            return ResponseEntity.ok(
                    Map.of("message", "Policy removed completely", "deleted_id", id)
            );

        } catch (Exception e) {
            log.error("Failed to remove policy", e);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to remove policy: " + e.getMessage())
            );
        }
    }

    /**
     * POST /admin/api/policies/{id}/replace - Replace existing policy
     */
    @PostMapping("/{id}/replace")
    public ResponseEntity<?> replacePolicy(
            @PathVariable Long id,
            @RequestBody ApplyPolicyRequestDTO newRequest) {

        log.info("POST /policies/{}/replace", id);

        try {
            // 1. Find and remove old policy
            Optional<FilePolicy> oldPolicyOpt = filePolicyRepository.findById(id);
            if (!oldPolicyOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            FilePolicy oldPolicy = oldPolicyOpt.get();

            // Remove from Rust Agent
            if (oldPolicy.getAgentPolicyId() != null) {
                agentClient.delete("/api/v1/policies/" + oldPolicy.getAgentPolicyId()).block();
            }

            // Delete old policy completely
            filePolicyRepository.delete(oldPolicy);

            // 2. Apply new policy
            FilePolicy newPolicy = new FilePolicy();
            newPolicy.setNodeId(newRequest.getNode_id());
            newPolicy.setScope(newRequest.getScope());
            newPolicy.setAction(newRequest.getAction());
            newPolicy.setOperations(objectMapper.writeValueAsString(newRequest.getOperations()));
            newPolicy.setCreatedBy(newRequest.getCreated_by());
            newPolicy.setComment("Replaced policy " + id + ": " + newRequest.getComment());
            newPolicy.setConfirmed(true);
            newPolicy.setCreatedAt(System.currentTimeMillis() / 1000);
            newPolicy.setUpdatedAt(System.currentTimeMillis() / 1000);
            newPolicy.setIsActive(true);

            FilePolicy savedPolicy = filePolicyRepository.save(newPolicy);

            // Send to Rust Agent
            String agentResponse = agentClient.post("/api/v1/policies/apply", newRequest).block();

            JsonNode json = objectMapper.readTree(agentResponse);
            Long agentPolicyId = json.get("data").get("policy_id").asLong();

            savedPolicy.setAgentPolicyId(agentPolicyId);
            savedPolicy.setAgentSynced(true);
            filePolicyRepository.save(savedPolicy);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Policy replaced successfully",
                            "old_policy_id", id,
                            "new_policy_id", savedPolicy.getId()
                    )
            );

        } catch (Exception e) {
            log.error("Failed to replace policy", e);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to replace policy: " + e.getMessage())
            );
        }
    }

    /**
     * GET /admin/api/policies/cleanup - Clean duplicate inactive policies (Admin only)
     */
    @GetMapping("/cleanup")
    public ResponseEntity<?> cleanupDuplicatePolicies() {
        log.info("GET /policies/cleanup - Cleaning duplicates");

        try {
            // Find all nodes with multiple policies
            List<FilePolicy> allPolicies = filePolicyRepository.findAll();

            // Group by nodeId
            Map<Long, List<FilePolicy>> policiesByNode = allPolicies.stream()
                    .collect(Collectors.groupingBy(FilePolicy::getNodeId));

            int deletedCount = 0;

            // For each node, keep only the latest active policy
            for (Map.Entry<Long, List<FilePolicy>> entry : policiesByNode.entrySet()) {
                List<FilePolicy> nodePolicies = entry.getValue();

                if (nodePolicies.size() > 1) {
                    log.info("Node {} has {} policies", entry.getKey(), nodePolicies.size());

                    // Sort by creation time (newest first)
                    nodePolicies.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

                    // Keep the first (newest) active policy, delete the rest
                    boolean keptOne = false;
                    for (FilePolicy policy : nodePolicies) {
                        if (!keptOne && policy.getIsActive()) {
                            // Keep this one
                            keptOne = true;
                            log.info("Keeping policy ID: {}", policy.getId());
                        } else {
                            // Delete this duplicate
                            filePolicyRepository.delete(policy);
                            deletedCount++;
                            log.info("Deleted duplicate policy ID: {}", policy.getId());
                        }
                    }
                }
            }

            return ResponseEntity.ok(
                    Map.of("message", "Cleanup completed", "deleted_count", deletedCount)
            );

        } catch (Exception e) {
            log.error("Cleanup failed", e);
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Cleanup failed: " + e.getMessage())
            );
        }
    }

}