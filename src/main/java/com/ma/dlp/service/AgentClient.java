package com.ma.dlp.service;

import com.ma.dlp.Repository.UserRepository;
import com.ma.dlp.model.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * HTTP client for communicating with the Rust agent's local HTTP server (port 8081).
 *
 * Supports per-agent routing: callers provide an agentId, this class looks up
 * the agent's stored IP address and builds a request to http://{agentIp}:8081.
 */
@Service
@Slf4j
public class AgentClient {

    private static final Logger log = LoggerFactory.getLogger(AgentClient.class);
    private static final int AGENT_HTTP_PORT = 8081;

    @Autowired
    private UserRepository userRepository;

    @Value("${agent.base-url:http://127.0.0.1:8081}")
    private String fallbackBaseUrl;

    @Value("${agent.timeout.read:10000}")
    private int readTimeout;

    // ───────────────────────────── WebClient factory ──────────────────────────

    /** Build a 10 MB-capable WebClient for a specific base URL. */
    private WebClient buildClient(String baseUrl) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .build();
    }

    /**
     * Resolve the base URL for a given agentId.
     * Looks up the agent's IP address in the database and returns
     * http://{ip}:8081. Falls back to the configured agent.base-url if
     * the agent is not found or has no IP stored.
     */
    public String resolveBaseUrl(Long agentId) {
        if (agentId == null) {
            log.warn("[AgentClient] No agentId provided, using fallback base URL: {}", fallbackBaseUrl);
            return fallbackBaseUrl;
        }
        return userRepository.findById(agentId)
                .map(User::getIpAddress)
                .filter(ip -> ip != null && !ip.isBlank())
                .map(ip -> "http://" + ip + ":" + AGENT_HTTP_PORT)
                .orElseGet(() -> {
                    log.warn("[AgentClient] Agent {} has no IP stored, using fallback: {}", agentId, fallbackBaseUrl);
                    return fallbackBaseUrl;
                });
    }

    // ───────────────────────────── Per-agent GET ──────────────────────────────

    public Mono<String> getForAgent(Long agentId, String path) {
        String baseUrl = resolveBaseUrl(agentId);
        log.debug("[AgentClient] GET {} -> {}{}", path, baseUrl, path);
        return buildClient(baseUrl)
                .get().uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout))
                .doOnError(e -> log.error("[AgentClient] GET {} failed for agent {}: {}", path, agentId, e.getMessage()));
    }

    // ───────────────────────────── Per-agent POST ─────────────────────────────

    public Mono<String> postForAgent(Long agentId, String path) {
        String baseUrl = resolveBaseUrl(agentId);
        return buildClient(baseUrl)
                .post().uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout))
                .doOnError(e -> log.error("[AgentClient] POST {} failed for agent {}: {}", path, agentId, e.getMessage()));
    }

    public Mono<String> postForAgent(Long agentId, String path, Object body) {
        String baseUrl = resolveBaseUrl(agentId);
        return buildClient(baseUrl)
                .post().uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout))
                .doOnError(e -> log.error("[AgentClient] POST {} failed for agent {}: {}", path, agentId, e.getMessage()));
    }

    // ───────────────────────────── Per-agent DELETE ───────────────────────────

    public Mono<String> deleteForAgent(Long agentId, String path) {
        String baseUrl = resolveBaseUrl(agentId);
        return buildClient(baseUrl)
                .delete().uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout))
                .doOnError(e -> log.error("[AgentClient] DELETE {} failed for agent {}: {}", path, agentId, e.getMessage()));
    }

    // ───────────────────────────── Legacy (no agentId) ────────────────────────
    // These use the fallback base URL. Kept for backward compatibility.

    public Mono<String> get(String path) {
        return buildClient(fallbackBaseUrl).get().uri(path)
                .retrieve().bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    public Mono<String> post(String path) {
        return buildClient(fallbackBaseUrl).post().uri(path)
                .retrieve().bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    public Mono<String> post(String path, Object body) {
        return buildClient(fallbackBaseUrl).post().uri(path)
                .bodyValue(body).retrieve().bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    public Mono<String> postRaw(String path, String rawJson) {
        return buildClient(fallbackBaseUrl).post().uri(path)
                .header("Content-Type", "application/json")
                .bodyValue(rawJson).retrieve().bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    public Mono<String> postNoBody(String path) {
        return buildClient(fallbackBaseUrl).post().uri(path)
                .retrieve().bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    public Mono<String> delete(String path) {
        return buildClient(fallbackBaseUrl).delete().uri(path)
                .retrieve().bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    /** Ping a specific agent by ID. */
    public Mono<Boolean> pingAgent(Long agentId) {
        String baseUrl = resolveBaseUrl(agentId);
        return buildClient(baseUrl).get().uri("/api/v1/ping")
                .retrieve().toBodilessEntity()
                .map(r -> r.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false);
    }

    /** Ping the fallback/default agent. */
    public Mono<Boolean> ping() {
        return buildClient(fallbackBaseUrl).get().uri("/api/v1/ping")
                .retrieve().toBodilessEntity()
                .map(r -> r.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false);
    }
}
