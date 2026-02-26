package com.ma.dlp.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AgentDetailDTO {
    private Long id;
    private String username;
    private String email;
    private String hostname;
    private String macAddress;
    private String ipAddress;
    private Date lastHeartbeat;
    private Date lastLogin;
    private Date createdAt;
    private String status;
    private String role;
    private String agentRuntimeState;
    private Integer capabilityCount;
    private Integer activePolicyCount;
    
    // Simplified OCR statuses - no recursive references
    private List<Map<String, Object>> ocrStatuses;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    
    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public Date getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(Date lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    
    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getAgentRuntimeState() { return agentRuntimeState; }
    public void setAgentRuntimeState(String agentRuntimeState) { this.agentRuntimeState = agentRuntimeState; }
    
    public Integer getCapabilityCount() { return capabilityCount; }
    public void setCapabilityCount(Integer capabilityCount) { this.capabilityCount = capabilityCount; }
    
    public Integer getActivePolicyCount() { return activePolicyCount; }
    public void setActivePolicyCount(Integer activePolicyCount) { this.activePolicyCount = activePolicyCount; }
    
    public List<Map<String, Object>> getOcrStatuses() { return ocrStatuses; }
    public void setOcrStatuses(List<Map<String, Object>> ocrStatuses) { this.ocrStatuses = ocrStatuses; }
}