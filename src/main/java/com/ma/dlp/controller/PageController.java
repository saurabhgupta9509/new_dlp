package com.ma.dlp.controller;

import com.ma.dlp.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
public class PageController {

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
            String formattedRole = formatRole(currentUser.getRole() != null ?
                    currentUser.getRole().toString() : "ADMIN");
            model.addAttribute("userRole", formattedRole);

            // Add email
            model.addAttribute("userEmail", currentUser.getEmail() != null ?
                    currentUser.getEmail() : "");

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
        if (username == null || username.isEmpty()) return "Admin";
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
        if (username == null || username.isEmpty()) return "A";
        return Arrays.stream(username.split("\\."))
                .map(part -> part.length() > 0 ? part.substring(0, 1).toUpperCase() : "")
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(""));
    }

    private String formatRole(String role) {
        if (role == null) return "Security Admin";
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

    // ============= PAGE MAPPINGS =============

    @GetMapping("/dashboard.html")
    public String dashboardPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "dashboard";
    }

    @GetMapping("/manage-agents.html")
    public String manageAgentsPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "manage-agents";
    }

    @GetMapping("/agent-add.html")
    public String agentAddPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "agent-add";
    }

    @GetMapping("/agent-edit.html")
    public String agentEditPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "agent-edit";
    }

    @GetMapping("/alerts.html")
    public String alertsPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "alerts";
    }

    @GetMapping("/assign-policy.html")
    public String assignPolicyPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "assign-policy";
    }

    @GetMapping("/audit-logs.html")
    public String auditLogsPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "audit-logs";
    }

    @GetMapping("/data-retention.html")
    public String dataRetentionPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "data-retention";
    }

    @GetMapping("/device-logs.html")
    public String deviceLogsPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "device-logs";
    }

    @GetMapping("/file-policies.html")
    public String filePoliciesPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "file-policies";
    }

    @GetMapping("/general.html")
    public String generalPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "general";
    }

    @GetMapping("/generate-report.html")
    public String generateReportPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "generate-report";
    }

    @GetMapping("/integrations.html")
    public String integrationsPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "integrations";
    }

    @GetMapping("/ocr-dashboard.html")
    public String ocrDashboardPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "ocr-dashboard";
    }

    @GetMapping("/ocr-policies.html")
    public String ocrPoliciesPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "ocr-policies";
    }

    @GetMapping("/permissions.html")
    public String permissionsPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "permissions";
    }

    @GetMapping("/policy-create.html")
    public String policyCreatePage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "policy-create";
    }

    @GetMapping("/policy-edit.html")
    public String policyEditPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "policy-edit";
    }

    @GetMapping("/policy-view.html")
    public String policyViewPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "policy-view";
    }

    @GetMapping("/reports.html")
    public String reportsPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "reports";
    }

    @GetMapping("/roles.html")
    public String rolesPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "roles";
    }

    @GetMapping("/security.html")
    public String securityPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "security";
    }

    @GetMapping("/users.html")
    public String usersPage(HttpSession session, Model model) {
        addUserToModel(session, model);
        return "users";
    }

    @GetMapping("/index.html")
    public String indexPage() {
        return "index"; // Login page doesn't need user data
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/dashboard.html";
    }
}