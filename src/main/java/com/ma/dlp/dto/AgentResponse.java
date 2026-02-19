package com.ma.dlp.dto;

import lombok.Data;

@Data
class AgentResponse<T> {
    private Boolean success;
    private String message;
    private T data;
}