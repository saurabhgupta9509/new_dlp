package com.ma.dlp.Repository;

import com.ma.dlp.model.OcrViolation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OcrViolationRepository extends JpaRepository<OcrViolation, Long> {

    List<OcrViolation> findByAgentIdOrderByTimestampDesc(Long agentId);

    @Query("SELECT COUNT(v) FROM OcrViolation v WHERE v.agentId = :agentId AND v.timestamp >= :since")
    Long countByAgentIdSince(Long agentId, LocalDateTime since);


    List<OcrViolation> findByCertificateIdOrderByTimestampDesc(Long certificateId);

    @Query("SELECT COUNT(v) FROM OcrViolation v WHERE v.timestamp >= :since")
    Long countAllSince(LocalDateTime since);

    @Modifying
    @Transactional
    @Query("UPDATE OcrViolation v SET v.certificateId = :certificateId WHERE v.id IN :violationIds")
    int assignViolationsToCertificate(@Param("certificateId") Long certificateId, @Param("violationIds") List<Long> violationIds);

    List<OcrViolation> findByCertificateId(Long id);

    Long countByTimestampAfter(LocalDateTime timestamp);
    Long countByTimestampBetween(LocalDateTime start, LocalDateTime end);

     // Add this method for recent violations
    @Query("SELECT v FROM OcrViolation v ORDER BY v.timestamp DESC LIMIT :limit")
    List<OcrViolation> findRecentViolations(@Param("limit") int limit);
}
