package com.ma.dlp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class AgentWebSocketConfig implements WebSocketConfigurer {

    private final AgentSocketHandler agentSocketHandler;

    public AgentWebSocketConfig(AgentSocketHandler handler) {
        this.agentSocketHandler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(agentSocketHandler, "/agent-ws")
                .setAllowedOrigins("*");
    }
}