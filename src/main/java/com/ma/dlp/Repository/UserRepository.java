package com.ma.dlp.Repository;

import com.ma.dlp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(User.UserRole role);
    List<User> findByStatus(User.UserStatus status);
    List<User> findAllByMacAddress(String macAddress);

    List<User> findByIpAddress(String ipAddress);

    @Query("SELECT u FROM User u WHERE u.macAddress IS NULL OR u.macAddress = ''")
    List<User> findAgentsWithoutMacAddress();

    @Query("SELECT u FROM User u WHERE u.role = 'AGENT'")
    List<User> findAllAgents();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'AGENT' AND u.lastHeartbeat >= :activeSince")
    Long countActiveAgents(@Param("activeSince") LocalDateTime activeSince);


    // NEW: Count active agents (heartbeat within last 2 minutes)
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'AGENT' AND u.status = 'ACTIVE' AND u.lastHeartbeat >= :activeSince")
    Long countActiveAgents(@Param("activeSince") Date activeSince);

    // NEW: Count new agents created today
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'AGENT' AND DATE(u.createdAt) = CURRENT_DATE")
    Long countNewAgentsToday();
}
