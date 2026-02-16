package com.ma.dlp.dto;


import lombok.Data;

@Data
public class DeviceRegisterRequest {
    private String deviceId;
    private String userId;
    private String deviceName;
    private String platform;
    private String monitorVersion;
    private String firstSeen;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(String firstSeen) {
        this.firstSeen = firstSeen;
    }

    public String getMonitorVersion() {
        return monitorVersion;
    }

    public void setMonitorVersion(String monitorVersion) {
        this.monitorVersion = monitorVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
