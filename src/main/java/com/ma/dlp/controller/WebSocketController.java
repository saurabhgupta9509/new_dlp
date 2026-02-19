package com.ma.dlp.controller;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@Slf4j
public class WebSocketController extends TextWebSocketHandler {


    private static final Logger log =
            LoggerFactory.getLogger(WebSocketController.class);

    @Value("${agent.ws-url}")
    private String agentWsUrl;

    private WebSocketSession agentSession;
    private final ConcurrentHashMap<String, WebSocketSession> clientSessions =
            new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void connectToAgent() {
        log.info("Connecting to Agent WebSocket: {}", agentWsUrl);

        WebSocketClient client = new StandardWebSocketClient();

        client.execute(new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                agentSession = session;
                log.info("Connected to Agent WebSocket");
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                // Forward Agent messages to all connected clients
                String payload = message.getPayload();
                log.debug("Received from Agent: {}", payload);

                // Forward to all browser clients via STOMP
                messagingTemplate.convertAndSend("/topic/kernel-events", payload);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
                log.error("Agent WebSocket disconnected: {}", status);
                agentSession = null;
                // Try to reconnect after delay
                try {
                    Thread.sleep(5000);
                    connectToAgent();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) {
                log.error("Agent WebSocket error", exception);
            }
        }, agentWsUrl);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        clientSessions.put(session.getId(), session);
        log.info("Browser WebSocket connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Forward browser messages to Agent (if needed)
        if (agentSession != null && agentSession.isOpen()) {
            try {
                agentSession.sendMessage(message);
            } catch (IOException e) {
                log.error("Failed to forward message to Agent", e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        clientSessions.remove(session.getId());
        log.info("Browser WebSocket disconnected: {}", session.getId());
    }

    /**
     * STOMP endpoint for browser to connect
     */
    @MessageMapping("/kernel-events")
    @SendTo("/topic/kernel-events")
    public String handleKernelEvent(String event) {
        log.debug("Received kernel event from browser: {}", event);
        return event;
    }
}