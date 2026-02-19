package com.ma.dlp.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class AgentClient {

    @Autowired
    private WebClient agentWebClient;

    @Value("${agent.timeout.read:10000}")
    private int readTimeout;

    /* ===================== GET ===================== */
    public Mono<String> get(String path) {
        return agentWebClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    /* ===================== POST (NO BODY) ===================== */
    public Mono<String> post(String path) {
        return agentWebClient.post()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    /* ===================== POST (WITH BODY) ===================== */
    public Mono<String> post(String path, Object body) {
        return agentWebClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    public Mono<String> postRaw(String path, String rawJson) {
        return agentWebClient.post()
                .uri(path)
                .header("Content-Type", "application/json")
                .bodyValue(rawJson)   // 🔥 raw JSON untouched
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    public Mono<String> postNoBody(String path) {
        return agentWebClient.post()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    /* ===================== DELETE ===================== */
    public Mono<String> delete(String path) {
        return agentWebClient.delete()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(readTimeout));
    }

    /**
     * Check if Agent is alive
     */
    public Mono<Boolean> ping() {
        return agentWebClient.get()
                .uri("/api/v1/ping")
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false);
    }
}
