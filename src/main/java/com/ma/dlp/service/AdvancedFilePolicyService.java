package com.ma.dlp.service;

import com.ma.dlp.Repository.FileSystemPermissionRepository;
import com.ma.dlp.Repository.GlobalRuleRepository;
import com.ma.dlp.Repository.ProcessWhitelistRepository;
import com.ma.dlp.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class AdvancedFilePolicyService {

    @Autowired
    private FileSystemPermissionRepository permissionRepository;

    @Autowired
    private ProcessWhitelistRepository processWhitelistRepository;

    @Autowired
    private GlobalRuleRepository globalRuleRepository;

    public PolicyDecision evaluateFileOperation(FileOperationRequest request) {
        PolicyDecision decision = new PolicyDecision();

        // 1. Check global rules first (highest priority)
        GlobalRule globalRule = evaluateGlobalRules(request);
        if (globalRule != null) {
            decision.setAllowed(globalRule.getType() == GlobalRule.RuleType.ALLOW);
            decision.setRuleType("GLOBAL");
            decision.setRuleDescription(globalRule.getDescription());
            return decision;
        }

        // 2. Check process whitelist
        if (isProcessWhitelisted(request)) {
            decision.setAllowed(true);
            decision.setRuleType("PROCESS_WHITELIST");
            decision.setRuleDescription("Process is whitelisted");
            return decision;
        }

        // 3. Check specific file/folder permissions
        FileSystemPermission permission = evaluatePathPermissions(request);
        if (permission != null) {
            boolean operationAllowed = permission.getAllowedOperations().stream()
                    .anyMatch(op -> op.name().equals(request.getOperation()));

            decision.setAllowed(operationAllowed);
            decision.setRuleType("PATH_PERMISSION");
            decision.setRuleDescription(permission.getDescription());
            return decision;
        }

        // 4. Default deny
        return decision;
    }

    private GlobalRule evaluateGlobalRules(FileOperationRequest request) {
        List<GlobalRule> rules = globalRuleRepository.findAllByOrderByPriorityDesc();

        for (GlobalRule rule : rules) {
            if (matchesPattern(request.getFilePath(), rule.getPattern())) {
                return rule;
            }
        }
        return null;
    }

    private boolean isProcessWhitelisted(FileOperationRequest request) {
        List<ProcessWhitelist> whitelists = processWhitelistRepository.findByProcessName(request.getProcessName());

        return whitelists.stream().anyMatch(whitelist ->
                whitelist.getSystemWide() ||
                        whitelist.getAllowedPaths().stream().anyMatch(path ->
                                request.getFilePath().startsWith(path)
                        )
        );
    }

    private FileSystemPermission evaluatePathPermissions(FileOperationRequest request) {
        List<FileSystemPermission> permissions = permissionRepository.findRelevantPermissions(request.getFilePath());

        return permissions.stream()
                .filter(permission -> permissionMatches(permission, request))
                .findFirst()
                .orElse(null);
    }

    private boolean permissionMatches(FileSystemPermission permission, FileOperationRequest request) {
        // Check if path matches
        boolean pathMatches = request.getFilePath().startsWith(permission.getPath());
        if (!pathMatches) return false;

        // Check if user is allowed (if specified)
        if (permission.getAllowedUsers() != null && !permission.getAllowedUsers().isEmpty() &&
                !permission.getAllowedUsers().contains(request.getUserId())) {
            return false;
        }

        // Check if process is allowed (if specified)
        if (permission.getAllowedProcesses() != null && !permission.getAllowedProcesses().isEmpty() &&
                !permission.getAllowedProcesses().contains(request.getProcessName())) {
            return false;
        }

        return true;
    }

    private boolean matchesPattern(String filePath, String pattern) {
        try {
            // Try regex match first
            return Pattern.compile(pattern).matcher(filePath).matches();
        } catch (Exception e) {
            // Fallback to simple contains
            return filePath.contains(pattern);
        }
    }
}