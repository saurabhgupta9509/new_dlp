// User.java
package com.ma.dlp.model;

import lombok.Data;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "users")
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = true) // Email is only for admins
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    private Date lastLogin;
    private Date createdAt;

    @Column(name = "token", length = 500)
    private String token;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<PolicyAssignment> policyAssignments = new ArrayList<>();

    // Agent-specific fields
    @Column(nullable = true)
    private String hostname;

    @Column(nullable = true)
    private String macAddress;

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;
    private Date lastHeartbeat;

    // In User.java - Add transient field for plain password (not stored in DB)
    @Transient  // This field won't be persisted to database
    private String plainPassword;

    @Transient
    private Boolean screenLocked;

    @Transient
    private Boolean ocrActive;

    @Transient
    private String agentRuntimeState;

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OcrStatus> ocrStatuses = new ArrayList<>();

    // ADD THESE RELATIONSHIPS:

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OcrLiveData> ocrLiveData = new ArrayList<>();

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OcrSecurityCertificate> ocrSecurityCertificates = new ArrayList<>();

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts = new ArrayList<>();

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AgentCapability> agentCapabilities = new ArrayList<>();

    public enum UserRole {
        ADMIN, AGENT
    }

    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (status == null) {
            status = UserStatus.ACTIVE;
        } // Only set email if user is ADMIN (agents don't need email)
        if (role == UserRole.ADMIN && email == null) {
            email = username + "@admin.dlp";
        }
    }

    public boolean isEmailLogin(String loginInput) {
        return loginInput != null && loginInput.contains("@") && loginInput.contains(".");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.role.name()));
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
    }

    public List<OcrLiveData> getOcrLiveData() {
        return ocrLiveData;
    }

    public void setOcrLiveData(List<OcrLiveData> ocrLiveData) {
        this.ocrLiveData = ocrLiveData;
    }

    public List<OcrSecurityCertificate> getOcrSecurityCertificates() {
        return ocrSecurityCertificates;
    }

    public void setOcrSecurityCertificates(List<OcrSecurityCertificate> ocrSecurityCertificates) {
        this.ocrSecurityCertificates = ocrSecurityCertificates;
    }

    public List<OcrStatus> getOcrStatuses() {
        return ocrStatuses;
    }

    public void setOcrStatuses(List<OcrStatus> ocrStatuses) {
        this.ocrStatuses = ocrStatuses;
    }

    public List<AgentCapability> getAgentCapabilities() {
        return agentCapabilities;
    }

    public void setAgentCapabilities(List<AgentCapability> agentCapabilities) {
        this.agentCapabilities = agentCapabilities;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public Boolean getOcrActive() {
        return ocrActive;
    }

    public void setOcrActive(Boolean ocrActive) {
        this.ocrActive = ocrActive;
    }

    public Boolean getScreenLocked() {
        return screenLocked;
    }

    public void setScreenLocked(Boolean screenLocked) {
        this.screenLocked = screenLocked;
    }

    public String getAgentRuntimeState() {
        return agentRuntimeState;
    }

    public void setAgentRuntimeState(String agentRuntimeState) {
        this.agentRuntimeState = agentRuntimeState;
    }

    public String getPlainPassword() {
        return plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status == UserStatus.ACTIVE;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Date lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    public List<PolicyAssignment> getPolicyAssignments() {
//        return policyAssignments;
//    }
//
//    public void setPolicyAssignments(List<PolicyAssignment> policyAssignments) {
//        this.policyAssignments = policyAssignments;
//    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}