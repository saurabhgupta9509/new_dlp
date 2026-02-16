package com.ma.dlp.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ma.dlp.model.PhishingAlertEntity;

@Repository
public interface PhishingAlertRepository extends JpaRepository<PhishingAlertEntity, Long> {
    
    Optional<PhishingAlertEntity> findByEmailKey(String emailKey);
    
    boolean existsByEmailKey(String emailKey);
    
    List<PhishingAlertEntity> findBySenderContainingIgnoreCase(String sender);
    
    List<PhishingAlertEntity> findBySubjectContainingIgnoreCase(String subject);
    
    List<PhishingAlertEntity> findBySource(String source);
    
    List<PhishingAlertEntity> findByMlScoreGreaterThan(Double score);
    
    List<PhishingAlertEntity> findByMlPrediction(String prediction);
    
    List<PhishingAlertEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<PhishingAlertEntity> findByCreatedAtBefore(LocalDateTime date); // Added this method
    
    List<PhishingAlertEntity> findByCreatedAtAfter(LocalDateTime date);
    
    @Query("SELECT p FROM PhishingAlertEntity p WHERE p.createdAt >= :since")
    List<PhishingAlertEntity> findRecentAlerts(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(p) FROM PhishingAlertEntity p WHERE p.createdAt >= :start AND p.createdAt < :end")
    Long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT p.sender, COUNT(p) as count FROM PhishingAlertEntity p GROUP BY p.sender ORDER BY count DESC")
    List<Object[]> countBySender();
    
    List<PhishingAlertEntity> findAllByOrderByCreatedAtDesc();
}