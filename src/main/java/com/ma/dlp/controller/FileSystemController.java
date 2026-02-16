package com.ma.dlp.controller;

import com.ma.dlp.Repository.FileSystemPermissionRepository;
import com.ma.dlp.Repository.GlobalRuleRepository;
import com.ma.dlp.dto.ApiResponse;
import com.ma.dlp.model.*;
import com.ma.dlp.service.AdvancedFilePolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/file-system")
public class FileSystemController {

    @Autowired
    private AdvancedFilePolicyService policyService;

    @Autowired
    private FileSystemPermissionRepository permissionRepository;

    @Autowired
    private GlobalRuleRepository globalRuleRepository;

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<PolicyDecision>> evaluateOperation(@RequestBody FileOperationRequest request) {
        try {
            PolicyDecision decision = policyService.evaluateFileOperation(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Operation evaluated", decision));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Evaluation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<FileSystemPermission>> getPermission(@RequestParam String path) {
        try {
            List<FileSystemPermission> permissions = permissionRepository.findByPath(path);
            if (permissions.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "No permission found", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Permission found", permissions.get(0)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to get permission: " + e.getMessage()));
        }
    }

    @PostMapping("/permissions")
    public ResponseEntity<ApiResponse<String>> savePermission(@RequestBody FileSystemPermission permission) {
        try {
            permissionRepository.save(permission);
            return ResponseEntity.ok(new ApiResponse<>(true, "Permission saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to save permission: " + e.getMessage()));
        }
    }

    @DeleteMapping("/permissions")
    public ResponseEntity<ApiResponse<String>> deletePermission(@RequestParam String path) {
        try {
            List<FileSystemPermission> permissions = permissionRepository.findByPath(path);
            if (!permissions.isEmpty()) {
                permissionRepository.deleteAll(permissions);
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Permission deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to delete permission: " + e.getMessage()));
        }
    }

    @GetMapping("/global-rules")
    public ResponseEntity<ApiResponse<List<GlobalRule>>> getGlobalRules() {
        try {
            // List<GlobalRule> rules = globalRuleRepository.findAllByOrderByPriorityDesc();
            List<GlobalRule> rules = List.of(); // Empty list for now
            return ResponseEntity.ok(new ApiResponse<>(true, "Global rules retrieved", rules));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to get global rules"));
        }
    }

    @PostMapping("/global-rules")
    public ResponseEntity<ApiResponse<String>> addGlobalRule(@RequestBody GlobalRule rule) {
        try {
            globalRuleRepository.save(rule);
            return ResponseEntity.ok(new ApiResponse<>(true, "Global rule added successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to add global rule: " + e.getMessage()));
        }
    }

    @DeleteMapping("/global-rules/{id}")
    public ResponseEntity<ApiResponse<String>> deleteGlobalRule(@PathVariable Long id) {
        try {
            globalRuleRepository.deleteById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Global rule deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to delete global rule: " + e.getMessage()));
        }
    }

    @PostMapping("/enforcement/enable")
    public ResponseEntity<ApiResponse<String>> enableEnforcement() {
        // This would communicate with agents to enable enforcement
        return ResponseEntity.ok(new ApiResponse<>(true, "DLP enforcement enabled"));
    }

    @PostMapping("/enforcement/disable")
    public ResponseEntity<ApiResponse<String>> disableEnforcement() {
        // This would communicate with agents to disable enforcement
        return ResponseEntity.ok(new ApiResponse<>(true, "DLP enforcement disabled"));
    }

    // File system tree endpoints (simplified for demo)
    @GetMapping("/root")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRootDirectories() {
        try {
            // This would scan the actual file system in production
            List<Map<String, Object>> rootDirs = List.of(
                    createFileTreeNode("C:\\", "C Drive", "DIRECTORY", true),
                    createFileTreeNode("D:\\", "D Drive", "DIRECTORY", true),
                    createFileTreeNode("/", "Root", "DIRECTORY", true)
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "Root directories", rootDirs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to get root directories"));
        }
    }

    @GetMapping("/children")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getChildren(@RequestParam String path) {
        try {
            // Mock data - in production, this would scan the actual file system
            List<Map<String, Object>> children = List.of(
                    createFileTreeNode(path + "Windows", "Windows", "DIRECTORY", true),
                    createFileTreeNode(path + "Program Files", "Program Files", "DIRECTORY", true),
                    createFileTreeNode(path + "Users", "Users", "DIRECTORY", true),
                    createFileTreeNode(path + "boot.ini", "boot.ini", "FILE", false)
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "Children retrieved", children));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to get children"));
        }
    }

    private Map<String, Object> createFileTreeNode(String path, String name, String type, boolean hasChildren) {
        Map<String, Object> node = new HashMap<>();
        node.put("path", path);
        node.put("name", name);
        node.put("type", type);
        node.put("hasChildren", hasChildren);
        node.put("hasPermissions", false); // This would check actual permissions
        return node;
    }
}