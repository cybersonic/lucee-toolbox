package org.lucee.toolbox.output.formatters;

import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.ToolboxResult;
import org.lucee.toolbox.output.OutputFormatter;

/**
 * Console output formatter with color support
 */
public class ConsoleFormatter implements OutputFormatter {
    
    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String BOLD = "\u001B[1m";
    
    @Override
    public String format(ToolboxResult result) {
        StringBuilder output = new StringBuilder();
        
        // Header
        output.append(BOLD).append("Lucee Toolbox Analysis Results").append(RESET).append("\n");
        output.append("=".repeat(40)).append("\n\n");
        
        // Violations
        if (!result.getViolations().isEmpty()) {
            output.append(BOLD).append("Linting Violations:").append(RESET).append("\n");
            for (LintingViolation violation : result.getViolations()) {
                String color;
                switch (violation.getSeverity()) {
                    case ERROR:
                        color = RED;
                        break;
                    case WARNING:
                        color = YELLOW;
                        break;
                    case INFO:
                        color = BLUE;
                        break;
                    default:
                        color = "";
                        break;
                }
                
                output.append(color)
                      .append(violation.getSeverity().getName().toUpperCase())
                      .append(RESET)
                      .append(" ")
                      .append(violation.getFilePath())
                      .append(":")
                      .append(violation.getLine())
                      .append(":")
                      .append(violation.getColumn())
                      .append(" ")
                      .append(violation.getMessage())
                      .append(" [")
                      .append(violation.getRuleId())
                      .append("]\n");
                      
                // Add context if available (single file mode)
                if (violation.getCodeSnippet() != null && !violation.getCodeSnippet().trim().isEmpty()) {
                    output.append("\n")
                          .append("Context:\n")
                          .append(violation.getCodeSnippet())
                          .append("\n");
                }
            }
            output.append("\n");
        }
        
        // Formatting changes
        if (!result.getFormattingChanges().isEmpty()) {
            output.append(BOLD).append("Formatting Changes:").append(RESET).append("\n");
            output.append(GREEN).append("Applied ").append(result.getFormattingChanges().size())
                  .append(" formatting changes").append(RESET).append("\n\n");
        }
        
        // Errors
        if (!result.getErrors().isEmpty()) {
            output.append(BOLD).append(RED).append("Errors:").append(RESET).append("\n");
            for (String error : result.getErrors()) {
                output.append(RED).append("ERROR: ").append(error).append(RESET).append("\n");
            }
            output.append("\n");
        }
        
        // Summary
        output.append(BOLD).append("Summary:").append(RESET).append("\n");
        output.append("Files processed: ").append(result.getStats().getFilesProcessed()).append("\n");
        output.append("Total violations: ").append(result.getStats().getTotalViolations()).append("\n");
        output.append("  - Errors: ").append(RED).append(result.getStats().getErrorCount()).append(RESET).append("\n");
        output.append("  - Warnings: ").append(YELLOW).append(result.getStats().getWarningCount()).append(RESET).append("\n");
        output.append("  - Info: ").append(BLUE).append(result.getStats().getInfoCount()).append(RESET).append("\n");
        output.append("Formatting changes: ").append(GREEN).append(result.getStats().getFormattingChanges()).append(RESET).append("\n");
        
        // Parser usage information
        if (!result.getStats().getParserUsage().isEmpty()) {
            output.append("Parser usage:\n");
            for (var entry : result.getStats().getParserUsage().entrySet()) {
                output.append("  - ").append(entry.getKey()).append(": ")
                      .append(entry.getValue()).append(" files\n");
            }
        }
        
        output.append("Execution time: ").append(result.getStats().getExecutionTimeMs()).append(" ms\n");
        
        return output.toString();
    }
    
    @Override
    public String getFileExtension() {
        return "txt";
    }
    
    @Override
    public String getContentType() {
        return "text/plain";
    }
}
