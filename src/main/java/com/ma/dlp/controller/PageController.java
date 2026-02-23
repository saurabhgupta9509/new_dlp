package com.ma.dlp.controller;

import com.ma.dlp.RestService.AlertStatsService;
import com.ma.dlp.RestService.DashBoardService;
import com.ma.dlp.RestService.ManageAgentStatsService;
import com.ma.dlp.model.User;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
public class PageController {

    @Autowired
    private DashBoardService dashBoardService;

    @Autowired
    private AlertStatsService alertStatsService;

    @Autowired
    private ManageAgentStatsService manageAgentStatsService;
    // Helper method to check if user is authenticated
    private boolean isAuthenticated(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        return currentUser != null;
    }

    // Helper method to add user data to model
    private void addUserToModel(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser != null && currentUser.getUsername() != null) {
            // Format name: "john.doe" -> "John Doe"
            String formattedName = formatUserName(currentUser.getUsername());
            model.addAttribute("userName", formattedName);

            // Get initials: "john.doe" -> "JD"
            String initials = getInitials(currentUser.getUsername());
            model.addAttribute("userInitials", initials);

            // Format role: "ADMIN" -> "Admin"
            String formattedRole = formatRole(
                    currentUser.getRole() != null ? currentUser.getRole().toString() : "ADMIN");
            model.addAttribute("userRole", formattedRole);

            // Add email
            model.addAttribute("userEmail", currentUser.getEmail() != null ? currentUser.getEmail() : "");

            // Add user ID
            model.addAttribute("userId", currentUser.getId());
        } else {
            // Default values for fallback
            model.addAttribute("userName", "Admin");
            model.addAttribute("userInitials", "A");
            model.addAttribute("userRole", "Security Admin");
            model.addAttribute("userEmail", "");
            model.addAttribute("userId", 0);
        }
    }

    private String formatUserName(String username) {
        if (username == null || username.isEmpty())
            return "Admin";
        return Arrays.stream(username.split("\\."))
                .map(part -> {
                    if (part.length() > 0) {
                        return part.substring(0, 1).toUpperCase() +
                                part.substring(1).toLowerCase();
                    }
                    return "";
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    private String getInitials(String username) {
        if (username == null || username.isEmpty())
            return "A";
        return Arrays.stream(username.split("\\."))
                .map(part -> part.length() > 0 ? part.substring(0, 1).toUpperCase() : "")
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(""));
    }

    private String formatRole(String role) {
        if (role == null)
            return "Security Admin";
        return Arrays.stream(role.split("_"))
                .map(word -> {
                    if (word.length() > 0) {
                        return word.substring(0, 1).toUpperCase() +
                                word.substring(1).toLowerCase();
                    }
                    return "";
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    // Helper method for authenticated pages
    private String checkAuthAndReturn(HttpSession session, Model model, String viewName) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }
        addUserToModel(session, model);
        return viewName;
    }

    // ============= PAGE MAPPINGS =============

    @GetMapping("/dashboard")
    public String dashboardPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        model.addAttribute("stats", dashBoardService.getStats());

        return "dashboard";
    }

    @GetMapping("/manage-agents")
    public String manageAgentsPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        model.addAttribute("ManageAgentStats", manageAgentStatsService.getManageAgentStats());

        return "manage-agents";
    }

    @GetMapping("/agent-add")
    public String agentAddPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "agent-add";
    }

    @GetMapping("/agent-edit")
    public String agentEditPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "agent-edit";
    }

    @GetMapping("/agent-view")
    public String agentViewPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "agent-view";
    }

    @GetMapping("/alerts")
    public String alertsPage(HttpSession session, Model model) {
       if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        model.addAttribute("alertStats", alertStatsService.getAlertStats());
        return "alerts";
    }

    @GetMapping("/alert-details")
    public String alertsDetailsPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "alert-details";
    }

    @GetMapping("/assign-policy")
    public String assignPolicyPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "assign-policy";
    }

    @GetMapping("/audit-logs")
    public String auditLogsPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "audit-logs";

    }

    @GetMapping("/data-retention")
    public String dataRetentionPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "data-retention";
    }

    @GetMapping("/device-logs")
    public String deviceLogsPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "device-logs";
    }

    @GetMapping("/file-policies")
    public String filePoliciesPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "file-policies";
    }

    @GetMapping("/general")
    public String generalPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "general";
    }

    @GetMapping("/generate-report")
    public String generateReportPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "generate-report";
    }

    @GetMapping("/integrations")
    public String integrationsPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "integrations";
    }

    @GetMapping("/ocr-dashboard")
    public String ocrDashboardPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "ocr-dashboard";
    }

    @GetMapping("/ocr-policies")
    public String ocrPoliciesPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "ocr-policies";
    }

    @GetMapping("/permissions")
    public String permissionsPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "permissions";
    }

    @GetMapping("/policy-create")
    public String policyCreatePage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "policy-create";
    }

    @GetMapping("/policy-edit")
    public String policyEditPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "policy-edit";
    }

    @GetMapping("/policy-view")
    public String policyViewPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "policy-view";
    }

    @GetMapping("/reports")
    public String reportsPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "reports";
    }

    @GetMapping("/roles")
    public String rolesPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "roles";
    }

    @GetMapping("/security")
    public String securityPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "security";
    }

    @GetMapping("/users")
    public String usersPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/index";
        }

        addUserToModel(session, model); // 🔥 THIS WAS MISSING
        return "users";
    }

    @GetMapping("/index")
    public String indexPage() {
        return "index"; // Login page doesn't need user data
    }

    @GetMapping("/")
    public String rootRedirect(HttpSession session) {
        if (isAuthenticated(session)) {
            return "redirect:/dashboard";
        }
        return "redirect:/index";
    }
}