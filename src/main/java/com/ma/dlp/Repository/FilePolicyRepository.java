package com.ma.dlp.Repository;

import com.ma.dlp.model.FilePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface FilePolicyRepository  extends JpaRepository<FilePolicy, Long> {

    // ✅ Get ONLY active policies
    List<FilePolicy> findByIsActiveTrue();

    // ✅ Get active policy for specific node
    List<FilePolicy> findByNodeIdAndIsActiveTrue(Long nodeId);

    List<FilePolicy> findByAgentSyncedFalse();
    List<FilePolicy> findByIsActiveTrueAndAgentSyncedTrue(); // For startup sync

    // Get FIRST active policy for a node (returns Optional)
    @Query("SELECT p FROM FilePolicy p WHERE p.nodeId = :nodeId AND p.isActive = true ORDER BY p.createdAt DESC")
    Optional<FilePolicy> findFirstByNodeIdAndActive(@Param("nodeId") Long nodeId);

    // Deactivate ALL policies for a node
    @Modifying
    @Query("UPDATE FilePolicy p SET p.isActive = false WHERE p.nodeId = :nodeId AND p.isActive = true")
    int deactivateByNodeId(@Param("nodeId") Long nodeId);

    // Check if node has any active policy
    @Query("SELECT COUNT(p) > 0 FROM FilePolicy p WHERE p.nodeId = :nodeId AND p.isActive = true")
    boolean existsByNodeIdAndActive(@Param("nodeId") Long nodeId);

    // Count active policies for node
    @Query("SELECT COUNT(p) FROM FilePolicy p WHERE p.nodeId = :nodeId AND p.isActive = true")
    int countActiveByNodeId(@Param("nodeId") Long nodeId);

}