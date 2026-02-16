package com.ma.dlp.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PendingAgent {
    private String pendingId;
    private String hostname;
    private String macAddress;
    private Date requestTime;
    private String ipAddress;


    public PendingAgent(String pendingId, String hostname, String macAddress , String ipAddress) {
        this.pendingId = pendingId;
        this.hostname = hostname;
        this.macAddress = macAddress;
        this.requestTime = new Date();
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getPendingId() {
        return pendingId;
    }

    public void setPendingId(String pendingId) {
        this.pendingId = pendingId;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }
}