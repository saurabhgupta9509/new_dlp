package com.ma.dlp.RestStatsController;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ma.dlp.StatDTO.PolicyDTO;
import com.ma.dlp.dto.ApiResponse;
import com.ma.dlp.model.AgentCapability;
import com.ma.dlp.model.User;
import com.ma.dlp.service.AgentService;
import com.ma.dlp.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin")
public class agentView {

    @Autowired
    private AgentService agentService;
    @Autowired
    private UserService userService;

    private static final Logger log = LoggerFactory.getLogger(agentView.class);

    @GetMapping("/agents/{agentId}/policies")
    public ResponseEntity<ApiResponse<List<PolicyDTO>>> getAgentPolicies(
            @PathVariable("agentId") Long agentId,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            // Get the agent
            User agent = userService.findById(agentId)
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            // Get active capabilities for this agent
            List<AgentCapability> capabilities = agentService.getActiveCapabilities(agentId);

            // Convert to PolicyDTOs
            List<PolicyDTO> policies = capabilities.stream()
                    .map(cap -> {
                        PolicyDTO dto = new PolicyDTO();
                        dto.setId(cap.getId());
                        dto.setPolicyCode(cap.getCapabilityCode());
                        dto.setName(cap.getName());
                        dto.setDescription(cap.getDescription());
                        dto.setCategory(cap.getCategory());
                        dto.setPolicyType(cap.getAction() + "_" + cap.getCategory());
                        dto.setAction(cap.getAction());
                        dto.setTarget(cap.getTarget());
                        dto.setSeverity(cap.getSeverity());
                        dto.setIsActive(cap.getIsActive());
                        dto.setPolicyData(cap.getPolicyData());
                        dto.setAgentId(agentId);
                        dto.setAgentHostname(agent.getHostname());
                        return dto;
                    })
                    .collect(Collectors.toList());

            log.info("📋 Retrieved {} active policies for agent {}", policies.size(), agentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Agent policies retrieved", policies));

        } catch (Exception e) {
            log.error("❌ Failed to get agent policies for agent {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get agent policies: " + e.getMessage()));
        }
    }

    private boolean isAdminAuthenticated(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

}
