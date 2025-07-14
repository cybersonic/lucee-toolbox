package org.lucee.toolbox.output.formatters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.ToolboxResult;
import org.lucee.toolbox.output.OutputFormatter;

/**
 * Bitbucket Pipelines compatible output formatter
 */
public class BitbucketFormatter implements OutputFormatter {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String format(ToolboxResult result) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            
            // Bitbucket format for code insights
            ArrayNode annotations = objectMapper.createArrayNode();
            
            for (LintingViolation violation : result.getViolations()) {
                ObjectNode annotation = objectMapper.createObjectNode();
                
                // Required fields for Bitbucket
                annotation.put("path", violation.getFilePath());
                annotation.put("line", violation.getLine());
                annotation.put("message", violation.getMessage());
                annotation.put("severity", mapSeverity(violation.getSeverity().getName()));
                annotation.put("type", "BUG"); // or "CODE_SMELL", "VULNERABILITY"
                
                // Optional fields
                if (violation.getRuleId() != null) {
                    annotation.put("link", "https://docs.lucee.org/rules/" + violation.getRuleId());
                }
                
                annotations.add(annotation);
                
                // Limit to 1000 annotations as per Bitbucket limits
                if (annotations.size() >= 1000) {
                    break;
                }
            }
            
            root.set("annotations", annotations);
            
            // Summary
            ObjectNode summary = objectMapper.createObjectNode();
            summary.put("title", "Lucee Toolbox Analysis");
            summary.put("details", String.format(
                "Analysis completed. Found %d violations in %d files. " +
                "Errors: %d, Warnings: %d, Info: %d",
                result.getStats().getTotalViolations(),
                result.getStats().getFilesProcessed(),
                result.getStats().getErrorCount(),
                result.getStats().getWarningCount(),
                result.getStats().getInfoCount()
            ));
            summary.put("result", result.hasErrors() ? "FAILED" : "PASSED");
            
            root.set("summary", summary);
            
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            
        } catch (Exception e) {
            return "{\"error\": \"Failed to generate Bitbucket output: " + e.getMessage() + "\"}";
        }
    }
    
    private String mapSeverity(String severity) {
        switch (severity.toLowerCase()) {
            case "error":
                return "HIGH";
            case "warning":
                return "MEDIUM";
            case "info":
                return "LOW";
            default:
                return "MEDIUM";
        }
    }
    
    @Override
    public String getFileExtension() {
        return "json";
    }
    
    @Override
    public String getContentType() {
        return "application/json";
    }
}
