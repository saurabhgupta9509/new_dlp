package com.ma.dlp.Repository;

import com.ma.dlp.model.AgentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentSessionRepository extends JpaRepository<AgentSession, Long> {
    List<AgentSession> findByAgentIdOrderByLoginTimeDesc(Long agentId);
    Optional<AgentSession> findFirstByAgentIdAndLogoutTimeIsNullOrderByLoginTimeDesc(Long agentId);
}
