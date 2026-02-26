package com.ma.dlp.component;

import com.ma.dlp.dto.FileBrowseResponseDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebSocketNotifier {

    private final SimpMessagingTemplate template;

    public WebSocketNotifier(SimpMessagingTemplate template) {
        this.template = template;
    }

    /**
     * Send a browse response to subscribed UIs.
     * 
     * @param dto      file browse DTO from agent
     * @param partial  whether this is a partial chunk (true) or full (false)
     * @param complete whether browse operation completed
     */
    public void notifyBrowseResponse(FileBrowseResponseDTO dto, boolean partial, boolean complete) {
        if (dto == null || dto.getAgentId() == null)
            return;

        Map<String, Object> payload = new HashMap<>();
        payload.put("agentId", dto.getAgentId());
        payload.put("currentPath", dto.getCurrentPath());
        payload.put("parentPath", dto.getParentPath());
        payload.put("items", dto.getItems()); // list of FileSystemItemDTO
        payload.put("partial", partial);
        payload.put("complete", complete);

        String dest = "/topic/agent/" + dto.getAgentId() + "/browse";
        template.convertAndSend(dest, payload);
    }

    public void notifyPolicyUpdate(Long agentId) {
        Map<String, Object> signal = new HashMap<>();
        signal.put("agentId", agentId);
        signal.put("type", "POLICY_UPDATE");
        signal.put("timestamp", System.currentTimeMillis());

        String dest = "/topic/agent/" + agentId + "/signals";
        template.convertAndSend(dest, signal);
    }
}