package com.ma.dlp.controller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ma.dlp.dto.ExplorerNodeDto;
import com.ma.dlp.service.AgentClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/api/admin/explorer")
@Slf4j
public class ExplorerController {

    @Autowired
    private AgentClient agentClient;

    private static final Logger log =
            LoggerFactory.getLogger(ExplorerController.class);
    /**
     * Get all drives
     * GET /admin/api/explorer/drives
     */
    @GetMapping("/drives")
    public Mono<ResponseEntity<List<ExplorerNodeDto>>> getDrives() {
        log.info("GET /drives");

        return agentClient.get("/api/v1/drives")
                .map(json -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(json);
                        JsonNode data = root.get("data");

                        List<ExplorerNodeDto> nodes = new ArrayList<>();

                        if (data != null && data.isArray()) {
                            for (JsonNode d : data) {
                                ExplorerNodeDto node = new ExplorerNodeDto();
                                node.setId(d.get("id").asLong());
                                node.setName(d.get("name").asText());
                                node.setType("folder");   // drives are folders
                                node.setExpanded(false);
                                node.setChildren(new ArrayList<>());
                                nodes.add(node);
                            }
                        }

                        return ResponseEntity.ok(nodes);

                    } catch (Exception e) {
                        log.error("Failed to parse drives JSON from agent", e);
                        return ResponseEntity.internalServerError()
                                .body(Collections.emptyList());
                    }
                });
    }


    /**
     * Get node info by ID
     * GET /admin/api/explorer/nodes/{id}
     */
    @GetMapping("/nodes/{id}")
    public Mono<ResponseEntity<String>> getNode(@PathVariable Long id) {
        log.info("GET /nodes/{}", id);
        return agentClient.get("/api/v1/nodes/" + id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get node children
     * GET /admin/api/explorer/nodes/{id}/children
     */
    @GetMapping("/nodes/{id}/children")
    public Mono<ResponseEntity<String>> getNodeChildren(@PathVariable Long id) {
        log.info("GET /nodes/{}/children", id);
        return agentClient.get("/api/v1/nodes/" + id + "/children")
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



    @PostMapping("/nodes/{id}/expand")
    public Mono<ResponseEntity<List<ExplorerNodeDto>>> expandNode(@PathVariable Long id) {
        return agentClient.post("/api/v1/nodes/" + id + "/expand")
                .map(this::parseChildren);
    }

    @PostMapping("/nodes/{id}/collapse")
    public Mono<ResponseEntity<String>> collapseNode(@PathVariable Long id) {
        return agentClient.post("/api/v1/nodes/" + id + "/collapse")
                .map(ResponseEntity::ok);
    }

    /**
     * Local search
     * GET /admin/api/explorer/search
     */
    @GetMapping("/search")
    public Mono<ResponseEntity<String>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long parentId) {
        log.info("GET /search?query={}&parentId={}", query, parentId);

        StringBuilder uri = new StringBuilder("/api/v1/search/local");
        if (query != null || parentId != null) {
            uri.append("?");
            if (query != null) uri.append("query=").append(query);
            if (query != null && parentId != null) uri.append("&");
            if (parentId != null) uri.append("parent_id=").append(parentId);
        }

        return agentClient.get(uri.toString())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    private ResponseEntity<List<ExplorerNodeDto>> parseChildren(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode childrenNode = root.path("data").path("children");

            List<ExplorerNodeDto> children = new ArrayList<>();

            if (childrenNode.isArray()) {
                for (JsonNode c : childrenNode) {
                    ExplorerNodeDto node = new ExplorerNodeDto();
                    node.setId(c.get("id").asLong());
                    node.setName(c.get("name").asText());

                    String type = c.get("type").asText();
                    node.setType(type.equals("directory") ? "folder" : "file");

                    node.setExpanded(false);
                    node.setChildren(new ArrayList<>());
                    children.add(node);
                }
            }

            return ResponseEntity.ok(children);

        } catch (Exception e) {
            log.error("Failed to parse expand response", e);
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

}