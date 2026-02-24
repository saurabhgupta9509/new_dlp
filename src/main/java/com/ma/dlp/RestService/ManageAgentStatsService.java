package com.ma.dlp.RestService;

import com.ma.dlp.Repository.AgentCapabilityRepository;
import com.ma.dlp.Repository.AlertRepository;
import com.ma.dlp.Repository.UserRepository;
import com.ma.dlp.StatDTO.AlertStatsDTO;
import com.ma.dlp.StatDTO.DashboardStatsDTO;
import com.ma.dlp.StatDTO.ManageAgentStatsDTO;
import com.ma.dlp.model.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManageAgentStatsService {

    private final UserRepository userRepository;

    private final AgentCapabilityRepository agentCapabilityRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ManageAgentStatsService(UserRepository userRepository,
            AlertRepository alertRepository,
            AgentCapabilityRepository agentCapabilityRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.agentCapabilityRepository = agentCapabilityRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public ManageAgentStatsDTO getManageAgentStats() {

        List<User> tAgents = userRepository.findAllAgents(); // Using the specific method for agents
        long totalAgents = tAgents.size(); // Total agents count

        long onlineAgents = tAgents.stream()
                .filter(agent -> agent.getLastHeartbeat() != null &&
                        (System.currentTimeMillis() - agent.getLastHeartbeat().getTime()) <= 5000)
                .count();

        // Calculate offline agents (total agents - online agents)
        long offlineAgents = tAgents.size() - onlineAgents;

        // Calculate agents that need update
        // Option 1: Based on some criteria (e.g., agents with outdated versions)
        // long needsUpdate = tAgents.stream()
        // .filter(agent -> {
        // // Example logic: agents with version older than 1.0
        // // You can modify this based on your actual requirements
        // return agent.getVersion() != null &&
        // !agent.getVersion().equals("1.0"); // or any other condition
        // })
        // .count();

        // Option 2: If you want to use static values for demonstration/testing
        // long offlineAgents = 5; // static value
        // long needsUpdate = 3; // static value

        // Create and return the DTO
        ManageAgentStatsDTO manageAgentStats = new ManageAgentStatsDTO();
        manageAgentStats.setTotalAgents(totalAgents);
        manageAgentStats.setOnlineAgents(onlineAgents);
        manageAgentStats.setOfflineAgents(offlineAgents);
        manageAgentStats.setNeedsUpdate(1);

        return manageAgentStats;
    }

    @Scheduled(fixedRate = 5000)
    public void pushManageAgentStats() {
        ManageAgentStatsDTO manageAgentStats = getManageAgentStats();
        messagingTemplate.convertAndSend("/topic/manageAgentStats", manageAgentStats);
    }

}