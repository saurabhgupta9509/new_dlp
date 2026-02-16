package com.ma.dlp.Repository;

import com.ma.dlp.model.AgentCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentCommandRepository extends JpaRepository<AgentCommand, Long> {

    Optional<AgentCommand> findFirstByAgentIdAndProcessedFalseOrderByCreatedAtAsc(Long agentId);
}
