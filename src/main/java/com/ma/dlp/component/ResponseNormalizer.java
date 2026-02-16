package com.ma.dlp.component;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ma.dlp.model.OcrStatus;
import com.ma.dlp.model.OcrViolation;

@Component
public class ResponseNormalizer {

    public Map<String, Object> normalizeOcrAgent(OcrStatus entity) {
        Map<String, Object> result = new HashMap<>();

        // Include both camelCase and snake_case
        result.put("agentId", entity.getAgentId());
        result.put("agent_id", entity.getAgentId());

        result.put("agentHostname", entity.getAgentHostname());
        result.put("agent_hostname", entity.getAgentHostname());

        result.put("ocrEnabled", entity.isOcrEnabled());
        result.put("ocr_enabled", entity.isOcrEnabled());

        result.put("currentThreatScore", entity.getThreatScore());
        result.put("current_threat_score", entity.getThreatScore());

        result.put("violationsLast24h", entity.getViolationsLast24h());
        result.put("violations_last_24h", entity.getViolationsLast24h());

        return result;
    }

    public Map<String, Object> normalizeViolation(OcrViolation  entity) {
        Map<String, Object> result = new HashMap<>();

        result.put("id", entity.getId());
        result.put("agentId", entity.getAgentId());
        result.put("agent_id", entity.getAgentId());

        result.put("ruleType", entity.getRuleType());
        result.put("rule_type", entity.getRuleType());

        result.put("matchedText", entity.getMatchedText());
        result.put("matched_text", entity.getMatchedText());

        result.put("confidence", entity.getConfidence());
        result.put("contextConfidence", entity.getContextConfidence());
        result.put("context_confidence", entity.getContextConfidence());

        result.put("screenshotPath", entity.getScreenshotPath());
        result.put("screenshot_path", entity.getScreenshotPath());

        return result;
    }
}