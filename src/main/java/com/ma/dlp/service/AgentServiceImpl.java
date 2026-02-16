package com.ma.dlp.service;

import com.ma.dlp.Repository.AgentCapabilityRepository;
import com.ma.dlp.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class AgentServiceImpl extends AgentService {
    @Autowired
    private AgentCapabilityRepository agentCapabilityRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public int countActivePolicies() {
        Long count = agentCapabilityRepository.countByIsActiveTrue();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public int countPendingReviewPolicies() {
        // Assuming pending review means policies that need approval
        // You might need to implement this based on your business logic
        return 0; // Replace with actual implementation
    }
}
