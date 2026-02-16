package com.ma.dlp.controller;

import com.ma.dlp.dto.FileBrowseResponseDTO;
import com.ma.dlp.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class AgentBrowseController {


    @Autowired
    private AgentService agentService;
    @PostMapping("/agent/{agentId}/browse-result")
    public ResponseEntity<?> handleBrowseResult(
            @PathVariable Long agentId,
            @RequestBody FileBrowseResponseDTO dto) {

        agentService.storeBrowseResponse(agentId, dto);

        return ResponseEntity.ok().build();
    }

}
