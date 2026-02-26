package com.ma.dlp.Repository;

import com.ma.dlp.model.AppUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppUsageLogRepository extends JpaRepository<AppUsageLog, Long> {
    List<AppUsageLog> findByAgentIdOrderByReceivedAtDesc(Long agentId);
}

