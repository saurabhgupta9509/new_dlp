package com.ma.dlp.dto;

import lombok.Data;

@Data
public class AgentAuthResponse {
    private Long userId;
    private Long agentId;
    private String username;
    private String password;
    private String status;
    private String token;


    public AgentAuthResponse(Long agentId, String username, String password, String status, String token ) {
        this.agentId = agentId;
        this.userId = agentId;
        this.username = username;
        this.password = password;
        this.status = status;

        this.token = token;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
        this.userId = agentId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getAgentId() {
        return agentId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }
}