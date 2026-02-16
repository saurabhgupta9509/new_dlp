package com.ma.dlp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Matches each file entry sent by Rust agent.
 */
@Data
public class FileSystemItemDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("full_path")
    private String fullPath;

    @JsonProperty("is_dir")
    private boolean isDirectory;

    @JsonProperty("size")
    private long size;

    @JsonProperty("modified")
    private String modified;

    @JsonProperty("extension")
    private String extension;


    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
