package com.ma.dlp.Repository;

import com.ma.dlp.model.OcrLiveData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OcrLiveDataRepository extends JpaRepository<OcrLiveData, Long> {

    List<OcrLiveData> findTop50ByAgentIdOrderByTimestampDesc(Long agentId);

    List<OcrLiveData> findByAgentIdAndTimestampAfterOrderByTimestampDesc(Long agentId, LocalDateTime after);
}
