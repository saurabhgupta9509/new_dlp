package com.ma.dlp.component;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ma.dlp.Repository.FilePolicyRepository;
import com.ma.dlp.Repository.PolicyRepository;
import com.ma.dlp.dto.ApplyPolicyRequestDTO;
import com.ma.dlp.model.FilePolicy;
import com.ma.dlp.service.AgentClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgentSyncService {

    @Autowired
    private AgentClient agentClient;

    @Autowired
    private FilePolicyRepository policyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(AgentSyncService.class);

    @PostConstruct
    public void syncPoliciesOnStartup() {
        log.info("Syncing policies with Rust Agent on startup...");

        // Find all active policies that are synced with agent
        List<FilePolicy> activePolicies = policyRepository.findByIsActiveTrueAndAgentSyncedTrue();

        log.info("Found {} active policies to sync with agent", activePolicies.size());

        activePolicies.forEach(policy -> {
            try {
                // Convert Policy to ApplyPolicyRequestDTO
                ApplyPolicyRequestDTO request = convertToRequest(policy);

                // Send to Rust Agent (retry logic)
                agentClient.post("/api/v1/policies/apply", request)
                        .subscribe(response -> {
                            try {
                                // Extract agent policy ID from response
                                JsonNode json = objectMapper.readTree(response);
                                JsonNode dataNode = json.get("data");

                                Long newAgentPolicyId = null;
                                if (dataNode != null && dataNode.has("policy_id")) {
                                    newAgentPolicyId = dataNode.get("policy_id").asLong();
                                }

                                if (newAgentPolicyId != null) {
                                    // Update agent policy ID
                                    policy.setAgentPolicyId(newAgentPolicyId);
                                    policy.setAgentSynced(true);
                                    policyRepository.save(policy);
                                    log.info("Resynced policy {} with agent, new agentPolicyId: {}",
                                            policy.getId(), newAgentPolicyId);
                                } else {
                                    log.warn("Could not extract policy_id from agent response for policy {}",
                                            policy.getId());
                                }
                            } catch (Exception e) {
                                log.error("Failed to parse agent response for policy {}", policy.getId(), e);
                            }
                        }, error -> {
                            log.error("Failed to sync policy {} with agent: {}", policy.getId(), error.getMessage());
                            policy.setAgentSynced(false);
                            policyRepository.save(policy);
                        });

            } catch (Exception e) {
                log.error("Failed to convert policy {} to request: {}", policy.getId(), e.getMessage());
            }
        });
    }

    private ApplyPolicyRequestDTO convertToRequest(FilePolicy policy) throws JsonProcessingException {
        ApplyPolicyRequestDTO request = new ApplyPolicyRequestDTO();
        request.setNode_id(policy.getNodeId());
        request.setScope(policy.getScope());
        request.setAction(policy.getAction());

        // Parse operations JSON
        if (policy.getOperations() != null) {
            Map<String, Boolean> operations = objectMapper.readValue(
                    policy.getOperations(),
                    Map.class
            );
            request.setOperations(operations);
        } else {
            request.setOperations(new HashMap<>());
        }

        request.setCreated_by(policy.getCreatedBy());
        request.setComment(policy.getComment());
        request.setConfirmed(policy.getConfirmed());
        request.setTimestamp(policy.getCreatedAt());

        return request;
    }
}