package org.lucee.toolbox.output.formatters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.lucee.toolbox.core.model.FormattingChange;
import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.ToolboxResult;
import org.lucee.toolbox.output.OutputFormatter;

import java.time.format.DateTimeFormatter;

/**
 * JSON output formatter
 */
public class JsonFormatter implements OutputFormatter {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String format(ToolboxResult result) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            
            // Metadata
            root.put("tool", "lucee-toolbox");
            root.put("version", "1.0.0");
            root.put("executionTime", result.getExecutionTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // Statistics
            ObjectNode stats = objectMapper.createObjectNode();
            stats.put("filesProcessed", result.getStats().getFilesProcessed());
            stats.put("totalViolations", result.getStats().getTotalViolations());
            stats.put("errorCount", result.getStats().getErrorCount());
            stats.put("warningCount", result.getStats().getWarningCount());
            stats.put("infoCount", result.getStats().getInfoCount());
            stats.put("formattingChanges", result.getStats().getFormattingChanges());
            stats.put("executionTimeMs", result.getStats().getExecutionTimeMs());
            
            // Add parser usage
            ObjectNode parserUsage = objectMapper.createObjectNode();
            for (var entry : result.getStats().getParserUsage().entrySet()) {
                parserUsage.put(entry.getKey(), entry.getValue());
            }
            stats.set("parserUsage", parserUsage);
            
            root.set("statistics", stats);
            
            // Violations
            ArrayNode violations = objectMapper.createArrayNode();
            for (LintingViolation violation : result.getViolations()) {
                ObjectNode violationNode = objectMapper.createObjectNode();
                violationNode.put("ruleId", violation.getRuleId());
                violationNode.put("message", violation.getMessage());
                violationNode.put("severity", violation.getSeverity().getName());
                violationNode.put("file", violation.getFilePath());
                violationNode.put("line", violation.getLine());
                violationNode.put("column", violation.getColumn());
                violationNode.put("endLine", violation.getEndLine());
                violationNode.put("endColumn", violation.getEndColumn());
                
                if (violation.getRuleCategory() != null) {
                    violationNode.put("category", violation.getRuleCategory());
                }
                if (violation.getCodeSnippet() != null) {
                    violationNode.put("codeSnippet", violation.getCodeSnippet());
                }
                if (violation.getSuggestedFix() != null) {
                    violationNode.put("suggestedFix", violation.getSuggestedFix());
                }
                
                violations.add(violationNode);
            }
            root.set("violations", violations);
            
            // Formatting changes
            ArrayNode changes = objectMapper.createArrayNode();
            for (FormattingChange change : result.getFormattingChanges()) {
                ObjectNode changeNode = objectMapper.createObjectNode();
                changeNode.put("file", change.getFilePath());
                changeNode.put("startLine", change.getStartLine());
                changeNode.put("endLine", change.getEndLine());
                changeNode.put("changeType", change.getChangeType());
                changeNode.put("description", change.getDescription());
                changes.add(changeNode);
            }
            root.set("formattingChanges", changes);
            
            // Errors and warnings
            ArrayNode errors = objectMapper.createArrayNode();
            for (String error : result.getErrors()) {
                errors.add(error);
            }
            root.set("errors", errors);
            
            ArrayNode warnings = objectMapper.createArrayNode();
            for (String warning : result.getWarnings()) {
                warnings.add(warning);
            }
            root.set("warnings", warnings);
            
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            
        } catch (Exception e) {
            return "{\"error\": \"Failed to generate JSON output: " + e.getMessage() + "\"}";
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
