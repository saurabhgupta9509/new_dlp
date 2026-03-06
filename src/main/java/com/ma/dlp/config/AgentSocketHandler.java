package com.ma.dlp.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ma.dlp.controller.AdminController;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class AgentSocketHandler extends TextWebSocketHandler {

    private final Map<Long, WebSocketSession> agentSessions = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger log = LoggerFactory.getLogger(AgentSocketHandler.class);
    public AgentSocketHandler(SimpMessagingTemplate messagingTemplate) {
            this.messagingTemplate = messagingTemplate;
        }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;

        if (query == null) {
            log.error("❌ Agent connected without agentId!");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        Long agentId = extractAgentId(query);

        if (agentId == null) {
            log.error("❌ Invalid agentId in query!");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        agentSessions.put(agentId, session);
        log.info("✅ Agent connected: {}", agentId);
        
        // Send CONNECTED confirmation
        String connectedMsg = String.format(
            "{\"type\":\"CONNECTED\",\"agentId\":%d,\"timestamp\":%d}",
            agentId, System.currentTimeMillis()
        );
        session.sendMessage(new TextMessage(connectedMsg));
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.debug("📩 Message from agent: {}", payload);
        
        try {
            // Parse the message
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(payload);
            
            // Forward to browser clients via STOMP if needed
            String type = json.path("type").asText();
            if ("POLICY_UPDATE".equals(type)) {
                // Forward to admin dashboard
                messagingTemplate.convertAndSend("/topic/admin/policy-updates", payload);
            } else if ("HEARTBEAT".equals(type)) {
                // Just acknowledge, no need to forward
                log.debug("💓 Heartbeat received from agent");
            }
            
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
        }
    }

    public void sendToAgent(Long agentId, String payload) {
        WebSocketSession session = agentSessions.get(agentId);

        if (session == null) {
            log.warn("❌ No session found for agent: {}", agentId);
            return;
        }

        if (!session.isOpen()) {
            log.warn("❌ Session closed for agent: {}", agentId);
            agentSessions.remove(agentId);
            return;
        }

        try {
            session.sendMessage(new TextMessage(payload));
            log.debug("📤 Sent message to agent: {}", agentId);
        } catch (IOException e) {
            log.error("Failed to send to agent {}: {}", agentId, e.getMessage());
        }
    }

  private Long extractAgentId(String query) {
        for (String param : query.split("&")) {
            if (param.startsWith("agentId=")) {
                try {
                    return Long.parseLong(param.split("=")[1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}