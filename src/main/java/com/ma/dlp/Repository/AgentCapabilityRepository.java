package com.ma.dlp.Repository;

import com.ma.dlp.model.AgentCapability;
import org.aspectj.weaver.loadtime.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentCapabilityRepository extends JpaRepository<AgentCapability, Long> {

    List<AgentCapability> findByAgent_Id(Long agentId);

    List<AgentCapability> findByAgent_IdAndIsActiveTrue(Long agentId);

    Optional<AgentCapability> findByAgent_IdAndCapabilityCode(@Param("agentId") Long agentId,
            @Param("capabilityCode") String capabilityCode);

    boolean existsByAgent_IdAndCapabilityCode(@Param("agentId") Long agentId,
            @Param("capabilityCode") String capabilityCode);

    @Query("SELECT ac FROM AgentCapability ac WHERE ac.agent.id = :agentId AND ac.isActive = true")
    List<AgentCapability> findActiveCapabilitiesByAgentId(@Param("agentId") Long agentId);

    @Modifying
    @Query("DELETE FROM AgentCapability ac WHERE ac.agent.id = :agentId")
    void deleteByAgentId(@Param("agentId") Long agentId);

    @Query("SELECT DISTINCT ac.category FROM AgentCapability ac WHERE ac.agent.id = :agentId")
    List<String> findDistinctCategoriesByAgentId(@Param("agentId") Long agentId);

    List<AgentCapability> findByCapabilityCode(String capabilityCode);

    @Query("""
                SELECT COUNT(DISTINCT ac.agent.id)
                FROM AgentCapability ac
                WHERE ac.capabilityCode = :capabilityCode
            """)
    long countAgentsWithCapability(@Param("capabilityCode") String capabilityCode);;

    @Query("""
                SELECT DISTINCT ac.agent.id
                FROM AgentCapability ac
                WHERE ac.capabilityCode = :capabilityCode
            """)
    List<Long> findAllAgentIdsWithCapability(@Param("capabilityCode") String capabilityCode);

    @Query("""
                SELECT DISTINCT ac.agent.id
                FROM AgentCapability ac
                WHERE ac.capabilityCode = :capabilityCode
            """)
    List<Long> findAgentIdsWithCapability(@Param("capabilityCode") String capabilityCode);

    boolean existsByAgent_IdAndCapabilityCodeAndIsActiveTrue(
            @Param("agentId") Long agentId,
            @Param("capabilityCode") String capabilityCode);

    // ADD THIS METHOD:
    Long countByIsActiveTrue();

}
