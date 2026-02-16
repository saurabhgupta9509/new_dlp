package com.ma.dlp.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ma.dlp.model.BlockedUrlEntity;

@Repository
public interface BlockedUrlRepository extends JpaRepository<BlockedUrlEntity, Long> {
    
    List<BlockedUrlEntity> findByActiveTrue();
    
    List<BlockedUrlEntity> findByGlobalTrueAndActiveTrue();
    
    List<BlockedUrlEntity> findByDeviceId(String deviceId);
    
    List<BlockedUrlEntity> findByDeviceIdAndActiveTrue(String deviceId);
    
    List<BlockedUrlEntity> findByUserId(String userId);
    
    List<BlockedUrlEntity> findByUserIdAndActiveTrue(String userId);
    
    List<BlockedUrlEntity> findByCategory(String category);
    
    List<BlockedUrlEntity> findByCategoryAndActiveTrue(String category);
    
    Optional<BlockedUrlEntity> findByUrlPattern(String urlPattern);
    
    boolean existsByUrlPattern(String urlPattern);
    
    @Query("SELECT b FROM BlockedUrlEntity b WHERE b.active = true AND " +
           "(b.global = true OR b.deviceId = :deviceId OR b.userId = :userId)")
    List<BlockedUrlEntity> findApplicableForDevice(
            @Param("deviceId") String deviceId,
            @Param("userId") String userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM BlockedUrlEntity b WHERE b.urlPattern = :urlPattern")
    void deleteByUrlPattern(@Param("urlPattern") String urlPattern);
    
    List<BlockedUrlEntity> findAllByOrderByUpdatedAtDesc();
}