package com.ma.dlp.config;
import com.ma.dlp.service.AgentClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupListener {

    @Autowired
    private AgentClient agentClient;

    private static final Logger log =
            LoggerFactory.getLogger(StartupListener.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("   ADMIN SERVER STARTED SUCCESSFULLY");
        System.out.println("=".repeat(60));
        System.out.println("Spring Boot: http://localhost:8090/admin");
        System.out.println("Rust Agent: http://127.0.0.1:8080");
        System.out.println("\nAVAILABLE ENDPOINTS:");
        System.out.println("  GET  /admin/api/explorer/drives");
        System.out.println("  GET  /admin/api/explorer/nodes/{id}");
        System.out.println("  GET  /admin/api/explorer/nodes/{id}/children");
        System.out.println("  POST /admin/api/policies/preview");
        System.out.println("  POST /admin/api/policies/dry-run");
        System.out.println("  POST /admin/api/policies/validate");
        System.out.println("  POST /admin/api/policies/apply");
        System.out.println("  GET  /admin/api/policies");
        System.out.println("  GET  /admin/api/policies/{id}/status");
        System.out.println("  GET  /admin/api/policies/health");
        System.out.println("\nWebSocket: ws://localhost:8090/admin/ws");
        System.out.println("=".repeat(60) + "\n");

        // Test Agent connection
        agentClient.ping().subscribe(connected -> {
            if (connected) {
                log.info("✅ Connected to Rust Agent");
            } else {
                log.warn("⚠️  Rust Agent not reachable. Is it running?");
            }
        });
    }
}