package com.ma.dlp.dto;


import lombok.Data;

@Data
public class HeartbeatRequest {
    private String deviceId;
    private String timestamp;
    private String status;
    private Boolean monitoring;
}
