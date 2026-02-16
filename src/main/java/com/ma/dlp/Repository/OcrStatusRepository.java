package com.ma.dlp.Repository;

import com.ma.dlp.model.OcrStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OcrStatusRepository extends JpaRepository<OcrStatus, Long> {

    Optional<OcrStatus> findTopByAgentIdOrderByUpdatedAtDesc(Long agentId);

    // @Query("SELECT s FROM OcrStatus s WHERE s.updatedAt = " +
    //         "(SELECT MAX(s2.updatedAt) FROM OcrStatus s2 WHERE s2.agentId = s.agentId)")
//@Query("""
//SELECT new com.ma.dlp.dto.OcrDashboardStatsDTO(
//    s.agentId,
//    s.agentHostname,
//    s.ocrEnabled,
//    s.threatScore,
//    s.violationsLast24h,
//    s.lastScreenshotTime
//)
//FROM OcrStatus s
//WHERE s.updatedAt IN (
//    SELECT MAX(s2.updatedAt) FROM OcrStatus s2 GROUP BY s2.agentId
//)
//""")
    // @Query("""
    //     SELECT s FROM OcrStatus s
    //     WHERE s.updatedAt = (
    //         SELECT MAX(s2.updatedAt)
    //         FROM OcrStatus s2
    //         WHERE s2.agentId = s.agentId
    //     )
    // """)
    // List<OcrStatus> findLatestForAllAgents();

    @Query("""
                SELECT s FROM OcrStatus s
                WHERE s.updatedAt IS NOT NULL
                AND s.updatedAt = (
                    SELECT MAX(s2.updatedAt)
                    FROM OcrStatus s2
                    WHERE s2.agentId = s.agentId
                        AND s2.updatedAt IS NOT NULL
                )
            """)
    List<OcrStatus> findLatestForAllAgents();

    @Query("""
        SELECT s FROM OcrStatus s
        WHERE s.agentId IN :agentIds
          AND s.updatedAt = (
              SELECT MAX(s2.updatedAt)
              FROM OcrStatus s2
              WHERE s2.agentId = s.agentId
          )
    """)
        List<OcrStatus> findLatestForAgents(@Param("agentIds") List<Long> agentIds);



    // Count OCR enabled agents
    @Query("""
                SELECT COUNT(DISTINCT s.agentId)
                FROM OcrStatus s
                WHERE s.ocrEnabled = true
                  AND s.updatedAt = (
                      SELECT MAX(s2.updatedAt)
                      FROM OcrStatus s2
                      WHERE s2.agentId = s.agentId
                  )
            """)
    long countActiveOcrAgents();

    long countByOcrEnabledTrue();

    @Query("""
            SELECT COUNT(DISTINCT s.agentId)
            FROM OcrStatus s
            WHERE s.ocrEnabled = true
            """)
    long countDistinctAgentIdByOcrEnabledTrue();

    @Query("SELECT DISTINCT s.agentId FROM OcrStatus s")
    List<Long> findAllAgentIds();

    @Modifying
    @Query("DELETE FROM OcrStatus o WHERE o.agent.id = :userId")
    void deleteByAgentUserId(@Param("userId") Long userId);
}
