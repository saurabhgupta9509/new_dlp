package com.ma.dlp.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ma.dlp.model.PartialAccessEntity;

@Repository
public interface PartialAccessRepository extends JpaRepository<PartialAccessEntity, Long> {
    
    List<PartialAccessEntity> findByActiveTrue();
    
    List<PartialAccessEntity> findByGlobalTrueAndActiveTrue();
    
    List<PartialAccessEntity> findByDeviceId(String deviceId);
    
    List<PartialAccessEntity> findByDeviceIdAndActiveTrue(String deviceId);
    
    List<PartialAccessEntity> findByUserId(String userId);
    
    List<PartialAccessEntity> findByUserIdAndActiveTrue(String userId);
    
    List<PartialAccessEntity> findByCategory(String category);
    
    List<PartialAccessEntity> findByCategoryAndActiveTrue(String category);
    
    Optional<PartialAccessEntity> findByUrlPattern(String urlPattern);
    
    boolean existsByUrlPattern(String urlPattern);
    
    @Query("SELECT p FROM PartialAccessEntity p WHERE p.active = true AND " +
           "(p.global = true OR p.deviceId = :deviceId OR p.userId = :userId)")
    List<PartialAccessEntity> findApplicableForDevice(
            @Param("deviceId") String deviceId,
            @Param("userId") String userId);
    
    @Query("SELECT p FROM PartialAccessEntity p WHERE " +
           "LOWER(p.urlPattern) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.domain) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<PartialAccessEntity> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM PartialAccessEntity p ORDER BY p.uploadAttempts DESC, p.downloadAttempts DESC")
    List<PartialAccessEntity> findByMostAttempts();
    
    @Query("SELECT p FROM PartialAccessEntity p ORDER BY p.updatedAt DESC")
    List<PartialAccessEntity> findAllByOrderByUpdatedAtDesc();
    
    @Query("SELECT COUNT(p) FROM PartialAccessEntity p WHERE p.active = true")
    long countActive();
    
    @Query("SELECT SUM(p.uploadAttempts) FROM PartialAccessEntity p")
    Long sumUploadAttempts();
    
    @Query("SELECT SUM(p.downloadAttempts) FROM PartialAccessEntity p")
    Long sumDownloadAttempts();
    
    @Query("SELECT p.category, COUNT(p) FROM PartialAccessEntity p WHERE p.active = true GROUP BY p.category")
    List<Object[]> countByCategory();
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PartialAccessEntity p WHERE p.urlPattern = :urlPattern")
    void deleteByUrlPattern(@Param("urlPattern") String urlPattern);
    
    @Modifying
    @Transactional
    @Query("UPDATE PartialAccessEntity p SET p.uploadAttempts = 0, p.downloadAttempts = 0")
    void resetAllAttempts();
}