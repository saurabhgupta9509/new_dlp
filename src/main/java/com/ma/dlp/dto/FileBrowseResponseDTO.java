package com.ma.dlp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for receiving chunked browse responses from Rust agent.
 * MATCHES Rust -> Spring Boot -> WebSocket -> Frontend
 */
@Data
public class FileBrowseResponseDTO {

    // ----- METADATA -----

    @JsonProperty("agentId")
    private Long agentId;

    @JsonProperty("currentPath")
    private String currentPath;

    @JsonProperty("parentPath")
    private String parentPath;

    @JsonProperty("partial")
    private Boolean partial;

    @JsonProperty("complete")
    private Boolean complete;

    @JsonProperty("chunkId")
    private Integer chunkId;

    // ----- ACTUAL FILES -----

    @JsonProperty("items") // Rust sends "items"
    private List<FileSystemItemDTO> items;


    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public Integer getChunkId() {
        return chunkId;
    }

    public void setChunkId(Integer chunkId) {
        this.chunkId = chunkId;
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public List<FileSystemItemDTO> getItems() {
        return items;
    }

    public void setItems(List<FileSystemItemDTO> items) {
        this.items = items;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public Boolean getPartial() {
        return partial;
    }

    public void setPartial(Boolean partial) {
        this.partial = partial;
    }
}
