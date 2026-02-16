package com.ma.dlp.controller;

import com.ma.dlp.Repository.OcrViolationRepository;
import com.ma.dlp.dto.ApiResponse;
import com.ma.dlp.dto.DashboardStatsDTO;
import com.ma.dlp.model.User;
import com.ma.dlp.service.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardStatsController {

    private static final Logger log = LoggerFactory.getLogger(DashboardStatsController.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private OcrViolationRepository ocrviolationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private OcrService ocrService;

    // Dashboard main statistics endpoint
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            DashboardStatsDTO stats = new DashboardStatsDTO();

            // 1. Total Alerts (with percentage change from last week)
            Long totalAlerts = alertService.getTotalAlertsCount();
            Long lastWeekAlerts = alertService.getAlertsCountSince(LocalDateTime.now().minusWeeks(1));
            Long previousWeekAlerts = alertService.getAlertsCountBetween(
                    LocalDateTime.now().minusWeeks(2),
                    LocalDateTime.now().minusWeeks(1)
            );

            stats.setTotalAlerts(totalAlerts != null ? totalAlerts : 0L); // FIX: totalAlerts, not totalAlents
            stats.setAlertsPercentageChange(calculatePercentageChange(previousWeekAlerts, lastWeekAlerts));

            // 2. Active Agents (agents with recent heartbeat)
            int activeAgentsCount = userService.countActiveAgents();
            stats.setActiveAgents(activeAgentsCount);
            stats.setNewAgentsToday(userService.countNewAgentsToday());

            // 3. OCR Violations - Use a simpler approach
            try {
                // Count OCR violations from the last week
                Long ocrViolationsLastWeek = ocrviolationRepository.countByTimestampAfter(
                        LocalDateTime.now().minusWeeks(1));
                Long ocrViolationsPreviousWeek = ocrviolationRepository.countByTimestampBetween(
                        LocalDateTime.now().minusWeeks(2),
                        LocalDateTime.now().minusWeeks(1));

                stats.setOcrViolations(ocrViolationsLastWeek != null ? ocrViolationsLastWeek : 0L);
                stats.setOcrViolationsPercentageChange(
                        calculatePercentageChange(ocrViolationsPreviousWeek, ocrViolationsLastWeek));
            } catch (Exception e) {
                log.warn("Could not fetch OCR violations: {}", e.getMessage());
                stats.setOcrViolations(0L);
                stats.setOcrViolationsPercentageChange(0);
            }

            // 4. Active Policies
            int activePoliciesCount = agentService.countActivePolicies();
            int pendingReviewPolicies = agentService.countPendingReviewPolicies();
            stats.setActivePolicies(activePoliciesCount);
            stats.setPendingReviewPolicies(pendingReviewPolicies);

            log.info("📊 Dashboard stats generated: TotalAlerts={}, ActiveAgents={}",
                    stats.getTotalAlerts(), stats.getActiveAgents());

            return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard stats retrieved", stats));

        } catch (Exception e) {
            log.error("❌ Failed to get dashboard stats: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get dashboard stats: " + e.getMessage()));
        }
    }

    // Alert breakdown by type
    @GetMapping("/alert-breakdown")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAlertBreakdown(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            Map<String, Object> breakdown = new HashMap<>();

            // Get counts by alert type
            Long usbViolations = alertService.countAlertsByType("USB");
            Long fileViolations = alertService.countAlertsByType("FILE");
            Long networkViolations = alertService.countAlertsByType("NETWORK");

            // For OCR violations, use the repository directly
            Long ocrViolations = 0L;
            try {
                // You'll need to autowire OcrViolationRepository or add method to OcrService
                ocrViolations = ocrviolationRepository.count();
            } catch (Exception e) {
                log.warn("Could not count OCR violations: {}", e.getMessage());
            }

            breakdown.put("usbViolations", usbViolations != null ? usbViolations : 0);
            breakdown.put("fileViolations", fileViolations != null ? fileViolations : 0);
            breakdown.put("networkViolations", networkViolations != null ? networkViolations : 0);
            breakdown.put("ocrViolations", ocrViolations);

            return ResponseEntity.ok(new ApiResponse<>(true, "Alert breakdown retrieved", breakdown));

        } catch (Exception e) {
            log.error("❌ Failed to get alert breakdown: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get alert breakdown: " + e.getMessage()));
        }
    }

    // Alert trends for the last 7 days
    @GetMapping("/alert-trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAlertTrends(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            Map<String, Object> trends = new HashMap<>();

            // Get daily counts for last 7 days
            List<Object[]> dailyCounts = alertService.getAlertsByDay(7);

            Map<String, Long> dailyMap = new HashMap<>();
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

            // Initialize with zeros
            for (String day : days) {
                dailyMap.put(day, 0L);
            }

            // Fill with actual data
            if (dailyCounts != null) {
                for (Object[] result : dailyCounts) {
                    if (result != null && result.length >= 2) {
                        String day = result[0] != null ? result[0].toString() : "";
                        Long count = 0L;
                        if (result[1] instanceof Number) {
                            count = ((Number) result[1]).longValue();
                        }
                        if (!day.isEmpty()) {
                            dailyMap.put(day, count);
                        }
                    }
                }
            }

            trends.put("days", days);
            trends.put("counts", dailyMap.values().toArray());

            // Calculate percentage change
            Long thisWeekTotal = dailyMap.values().stream().mapToLong(Long::longValue).sum();
            Long lastWeekTotal = alertService.getAlertsCountBetween(
                    LocalDateTime.now().minusWeeks(2),
                    LocalDateTime.now().minusWeeks(1)
            );

            int percentageChange = calculatePercentageChange(lastWeekTotal, thisWeekTotal);
            trends.put("percentageChange", percentageChange);

            return ResponseEntity.ok(new ApiResponse<>(true, "Alert trends retrieved", trends));

        } catch (Exception e) {
            log.error("❌ Failed to get alert trends: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get alert trends: " + e.getMessage()));
        }
    }

    // Recent activity feed
    @GetMapping("/recent-activity")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentActivity(HttpSession session) {
        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Admin access required"));
        }

        try {
            List<Map<String, Object>> activities = alertService.getRecentActivity(10); // Last 10 activities

            return ResponseEntity.ok(new ApiResponse<>(true, "Recent activity retrieved", activities));

        } catch (Exception e) {
            log.error("❌ Failed to get recent activity: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to get recent activity: " + e.getMessage()));
        }
    }

    private int calculatePercentageChange(Long previous, Long current) {
        if (previous == null || previous == 0) {
            return current == null ? 0 : 100;
        }
        if (current == null) {
            return -100;
        }
        return (int) (((current - previous) / (double) previous) * 100);
    }

    private boolean isAdminAuthenticated(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }
}