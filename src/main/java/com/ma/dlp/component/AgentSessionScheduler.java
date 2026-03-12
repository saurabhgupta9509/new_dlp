package com.ma.dlp.component;

import com.ma.dlp.Repository.AgentSessionRepository;
import com.ma.dlp.Repository.UserRepository;
import com.ma.dlp.model.AgentSession;
import com.ma.dlp.service.AgentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class AgentSessionScheduler {

    private static final Logger log = LoggerFactory.getLogger(AgentSessionScheduler.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired private AgentSessionRepository agentSessionRepository;

    // Runs every 2 minutes
    @Scheduled(fixedDelay = 120_000)
    public void closeInactiveSessions() {
        Date cutoff = new Date(System.currentTimeMillis() - 60_000); // 60s threshold

        // Find all open sessions
        List<AgentSession> openSessions = agentSessionRepository
                .findAll().stream()
                .filter(s -> s.getLogoutTime() == null)
                .toList();

        for (AgentSession session : openSessions) {
            // Check if agent's lastHeartbeat is older than cutoff
            userRepository.findById(session.getAgentId()).ifPresent(agent -> {
                if (agent.getLastHeartbeat() == null ||
                        agent.getLastHeartbeat().before(cutoff)) {

                    Date logoutTime = agent.getLastHeartbeat() != null
                            ? agent.getLastHeartbeat()
                            : new Date();

                    session.setLogoutTime(logoutTime);
                    session.setDurationSeconds(
                            (logoutTime.getTime() - session.getLoginTime().getTime()) / 1000
                    );
                    agentSessionRepository.save(session);
                    log.info("📝 Session closed for agent {} at {}",
                            session.getAgentId(), logoutTime);
                }
            });
        }
    }
}