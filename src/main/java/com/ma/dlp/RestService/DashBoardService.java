package com.ma.dlp.RestService;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ma.dlp.Repository.AgentCapabilityRepository;
import com.ma.dlp.Repository.AlertRepository;
import com.ma.dlp.Repository.UserRepository;
import com.ma.dlp.StatDTO.DashboardStatsDTO;
import com.ma.dlp.model.User;
@Service
public class DashBoardService {
    private final UserRepository userRepository;
    private final AlertRepository alertRepository;
    private final AgentCapabilityRepository agentCapabilityRepository;
    private final SimpMessagingTemplate messagingTemplate;
    public DashBoardService(UserRepository userRepository,
                            AlertRepository alertRepository,
                            AgentCapabilityRepository agentCapabilityRepository,
                            SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.alertRepository = alertRepository;
        this.agentCapabilityRepository = agentCapabilityRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public DashboardStatsDTO getStats() {

        long totalAgents = userRepository.count();

        long pendingAlerts = alertRepository.countByStatus("PENDING");

        long totalActivePolicies = agentCapabilityRepository.countByIsActiveTrue();

        List<User> agents = userRepository.findAll();

        long onlineAgents = agents.stream()
                .filter(agent -> agent.getLastHeartbeat() != null &&
                        (System.currentTimeMillis() - agent.getLastHeartbeat().getTime()) <= 5000)
                .count();
        
        return new DashboardStatsDTO(
                totalAgents,
                onlineAgents,
                pendingAlerts,
                totalActivePolicies
        );
    }
    
    @Scheduled(fixedRate = 5000)
    public void pushDashboardUpdate() {
        DashboardStatsDTO stats = getStats();
        messagingTemplate.convertAndSend("/topic/dashboard-stats", stats);
    }
}