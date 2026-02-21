package com.ma.dlp.service;

import com.ma.dlp.Repository.OcrStatusRepository;
import com.ma.dlp.Repository.UserRepository;
import com.ma.dlp.dto.ApiResponse;
import com.ma.dlp.model.User;
import com.ma.dlp.RestService.DashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private DashBoardService dashBoardService;
    @Autowired
    private UserRepository userRepository;
    private OcrStatusRepository ocrStatusRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

//    // NEW: Method to find user by username OR email (for admin login)
//    public Optional<User> findByUsernameOrEmail(String loginInput) {
//        if (loginInput == null || loginInput.trim().isEmpty()) {
//            return Optional.empty();
//        }
//
//        // Check if it's an email format
//        if (loginInput.contains("@")) {
//            Optional<User> byEmail = userRepository.findByEmail(loginInput);
//            if (byEmail.isPresent()) {
//                return byEmail;
//            }
//        }
//        // Try username
//        return userRepository.findByUsername(loginInput);
//    }

    public Optional<User> findByUsernameOrEmail(String loginInput) {
        if (loginInput == null || loginInput.trim().isEmpty()) {
            return Optional.empty();
        }

        // Email format: gmail.com, yahoo.in, company.co, etc.
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        // If valid email → search by email
        if (loginInput.matches(emailRegex)) {
            return userRepository.findByEmail(loginInput);
        }

        // Otherwise → treat as username
        return userRepository.findByUsername(loginInput);
    }


    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllAgents() {
        return userRepository.findByRole(User.UserRole.AGENT);
    }

    public List<User> getAllAdmins() {
        return userRepository.findByRole(User.UserRole.ADMIN);
    }

    public User updateUserStatus(Long userId, User.UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Clear ALL related entities
        user.getOcrStatuses().clear();

        if (user.getOcrLiveData() != null) {
            user.getOcrLiveData().clear();
        }

        if (user.getOcrSecurityCertificates() != null) {
            user.getOcrSecurityCertificates().clear();
        }

        if (user.getAlerts() != null) {
            user.getAlerts().clear();
        }

        userRepository.save(user); // Save to persist all removals

        // Now delete the user
        userRepository.delete(user);
    }

//    public void deleteUser(Long userId) {
//        // First delete related ocr_status records
//        ocrStatusRepository.deleteByAgentUserId(userId);
//
//        // Then delete the user
//        userRepository.deleteById(userId);
//    }


    public List<User> findByMacAddress(String macAddress) {
        return userRepository.findAllByMacAddress(macAddress);
    }

    public User createAgent(String hostname, String macAddress) {
        List<User> existingAgents = userRepository.findAllByMacAddress(macAddress);
        if (!existingAgents.isEmpty()) {
            return existingAgents.get(0);
        }

        User agent = new User();
        agent.setUsername("agent_" + System.currentTimeMillis());
        agent.setPassword(passwordEncoder.encode(generateRandomPassword()));
        agent.setRole(User.UserRole.AGENT);
        agent.setStatus(User.UserStatus.ACTIVE);
        agent.setHostname(hostname);
        agent.setMacAddress(macAddress);
        agent.setLastHeartbeat(new Date());

        return userRepository.save(agent);
    }

    public void updateHeartbeat(Long agentId) {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        agent.setLastHeartbeat(new Date());
        userRepository.save(agent);

        dashBoardService.pushDashboardUpdate();
    }

    @PostConstruct
    public void createDefaultAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("globaladmin@maximusatlas.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.UserRole.ADMIN);
            admin.setStatus(User.UserStatus.ACTIVE);
            admin.setCreatedAt(new Date());
            userRepository.save(admin);
            System.out.println("✅ Default admin user created: admin/admin123");
        }
    }

    // In UserService.java - add password reset method
    public ApiResponse<String> resetAgentPassword(Long agentId) {
        try {
            User agent = userRepository.findById(agentId)
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            String newPassword = generateRandomPassword();
            agent.setPassword(passwordEncoder.encode(newPassword));
            agent.setPlainPassword(newPassword);
            userRepository.save(agent);

            return new ApiResponse<>(true, "Password reset successfully", newPassword);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Password reset failed: " + e.getMessage());
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    public int countActiveAgents() {
        LocalDateTime activeSince = LocalDateTime.now().minusMinutes(2);
        LocalDateTime activeSinceDate = LocalDateTime.from(activeSince.atZone(ZoneId.systemDefault()).toInstant());

        Long count = userRepository.countActiveAgents(activeSinceDate);
        return count != null ? count.intValue() : 0;
    }

    // NEW: Count new agents created today
    public int countNewAgentsToday() {
        Long count = userRepository.countNewAgentsToday();
        return count != null ? count.intValue() : 0;
    }

}