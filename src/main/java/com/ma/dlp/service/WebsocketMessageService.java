package com.ma.dlp.service;
import com.ma.dlp.dto.FileBrowseResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebsocketMessageService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendBrowseUpdate(Long agentId, FileBrowseResponseDTO dto) {
        messagingTemplate.convertAndSend(
                "/topic/agent/" + agentId + "/browse",
                dto
        );
    }
}
