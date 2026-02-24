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

/**
 * Proxies file-explorer API calls from the admin UI to the correct
 * Rust agent HTTP server. Every endpoint requires ?agentId=<id> so the
 * backend knows which agent machine to talk to.
 */
@RestController
@RequestMapping("/api/admin/explorer")
@Slf4j
public class ExplorerController {

    @Autowired
    private AgentClient agentClient;

    private static final Logger log = LoggerFactory.getLogger(ExplorerController.class);

    // ─────────────────────────── GET /drives ──────────────────────────────────

    /**
     * GET /api/admin/explorer/drives?agentId={id}
     * Returns all drives visible on the specified agent machine.
     */
    @GetMapping("/drives")
    public Mono<ResponseEntity<List<ExplorerNodeDto>>> getDrives(
            @RequestParam Long agentId) {

        log.info("GET /drives  agentId={}", agentId);

        return agentClient.getForAgent(agentId, "/api/v1/drives")
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
                                node.setType("folder");
                                node.setExpanded(false);
                                node.setChildren(new ArrayList<>());
                                nodes.add(node);
                            }
                        }
                        return ResponseEntity.ok(nodes);

                    } catch (Exception e) {
                        log.error("Failed to parse drives JSON from agent {}", agentId, e);
                        return ResponseEntity.internalServerError()
                                .<List<ExplorerNodeDto>>body(Collections.emptyList());
                    }
                })
                .onErrorReturn(ResponseEntity.internalServerError()
                        .<List<ExplorerNodeDto>>body(Collections.emptyList()));
    }

    // ─────────────────────────── GET /nodes/{id} ──────────────────────────────

    /**
     * GET /api/admin/explorer/nodes/{id}?agentId={agentId}
     */
    @GetMapping("/nodes/{id}")
    public Mono<ResponseEntity<String>> getNode(
            @RequestParam Long agentId,
            @PathVariable Long id) {

        log.info("GET /nodes/{}  agentId={}", id, agentId);
        return agentClient.getForAgent(agentId, "/api/v1/nodes/" + id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ─────────────────────── GET /nodes/{id}/children ─────────────────────────

    /**
     * GET /api/admin/explorer/nodes/{id}/children?agentId={agentId}
     */
    @GetMapping("/nodes/{id}/children")
    public Mono<ResponseEntity<String>> getNodeChildren(
            @RequestParam Long agentId,
            @PathVariable Long id) {

        log.info("GET /nodes/{}/children  agentId={}", id, agentId);
        return agentClient.getForAgent(agentId, "/api/v1/nodes/" + id + "/children")
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ─────────────────────── POST /nodes/{id}/expand ──────────────────────────

    /**
     * POST /api/admin/explorer/nodes/{id}/expand?agentId={agentId}
     */
    @PostMapping("/nodes/{id}/expand")
    public Mono<ResponseEntity<List<ExplorerNodeDto>>> expandNode(
            @RequestParam Long agentId,
            @PathVariable Long id) {

        log.info("POST /nodes/{}/expand  agentId={}", id, agentId);
        return agentClient.postForAgent(agentId, "/api/v1/nodes/" + id + "/expand")
                .map(this::parseChildren);
    }

    // ─────────────────────── POST /nodes/{id}/collapse ────────────────────────

    /**
     * POST /api/admin/explorer/nodes/{id}/collapse?agentId={agentId}
     */
    @PostMapping("/nodes/{id}/collapse")
    public Mono<ResponseEntity<String>> collapseNode(
            @RequestParam Long agentId,
            @PathVariable Long id) {

        log.info("POST /nodes/{}/collapse  agentId={}", id, agentId);
        return agentClient.postForAgent(agentId, "/api/v1/nodes/" + id + "/collapse")
                .map(ResponseEntity::ok);
    }

    // ──────────────────────────── GET /search ────────────────────────────────

    /**
     * GET /api/admin/explorer/search?agentId={agentId}&query={q}&parentId={p}
     */
    @GetMapping("/search")
    public Mono<ResponseEntity<String>> search(
            @RequestParam Long agentId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long parentId) {

        log.info("GET /search  agentId={}  query={}  parentId={}", agentId, query, parentId);

        StringBuilder uri = new StringBuilder("/api/v1/search/local");
        if (query != null || parentId != null) {
            uri.append("?");
            if (query != null)
                uri.append("query=").append(query);
            if (query != null && parentId != null)
                uri.append("&");
            if (parentId != null)
                uri.append("parent_id=").append(parentId);
        }

        return agentClient.getForAgent(agentId, uri.toString())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    // ─────────────────────────── helpers ─────────────────────────────────────

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