package com.ma.dlp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record WebLogDto(
        @JsonProperty("url") String url,
        @JsonProperty("browser") String browser,
        @JsonProperty("timestamp") LocalDateTime visitTimestamp,
        @JsonProperty("action") String action,
        @JsonProperty("blocked") boolean blocked,
        @JsonProperty("fileInfo") String fileInfo
) {
    // Default constructor for Jackson
    public WebLogDto {
        if (action == null) {
            action = "BROWSE"; // Default value
        }
        if (fileInfo == null) {
            fileInfo = ""; // Default empty string
        }
    }

    // Constructor for backward compatibility (if you still need the old one)
    public WebLogDto(String url, String browser, LocalDateTime visitTimestamp) {
        this(url, browser, visitTimestamp, "BROWSE", false, "");
    }
}