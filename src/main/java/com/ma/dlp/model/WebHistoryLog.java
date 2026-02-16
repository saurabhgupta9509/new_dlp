package com.ma.dlp.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "web_history_log")
public class WebHistoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(nullable = false)
    private String browser;

    @Column(name = "visit_timestamp", nullable = false)
    private LocalDateTime visitTimestamp; // This should be String

    private String action; // "BROWSE", "DOWNLOAD", "UPLOAD", "BLOCKED_ACCESS"
    private boolean blocked;

    @Column(name = "file_info")
    private String fileInfo;

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

     public LocalDateTime  getVisitTimestamp() { return visitTimestamp; }
    public void setVisitTimestamp(LocalDateTime  visitTimestamp) { this.visitTimestamp = visitTimestamp; }
//    x: Remove the Instant getter/setter and keep only String

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public String getFileInfo() { return fileInfo; }
    public void setFileInfo(String fileInfo) { this.fileInfo = fileInfo; }


}