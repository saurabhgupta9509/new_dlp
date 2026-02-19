package com.ma.dlp.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExplorerNodeDto {
    private Long id;
    private String name;
    private String type;        // "folder" or "file"
    private boolean expanded;
    private List<ExplorerNodeDto> children;

    public List<ExplorerNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<ExplorerNodeDto> children) {
        this.children = children;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
