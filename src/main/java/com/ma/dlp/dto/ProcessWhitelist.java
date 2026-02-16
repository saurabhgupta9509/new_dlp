package com.ma.dlp.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProcessWhitelist {
    private Long id;
    private String processName;
    private String processPath;
    private String description;
    private List<String> allowedPaths;
    private Boolean systemWide;
}