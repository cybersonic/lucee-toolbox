package org.lucee.toolbox.core.rules.structure;

import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.Severity;
import org.lucee.toolbox.core.parser.ParseResult;
import org.lucee.toolbox.core.rules.LintingRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule that enforces maximum file length limits
 */
public class MaxFileLengthRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    public MaxFileLengthRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "MAX_FILE_LENGTH";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        int maxLength = getMaxFileLength();
        if (maxLength <= 0) {
            return violations; // Disabled
        }
        
        String[] lines = parseResult.getLines();
        int actualLineCount = countNonEmptyLines(lines);
        
        if (actualLineCount > maxLength) {
            violations.add(new LintingViolation(
                getRuleId(),
                String.format("File is too long (%d lines, max %d)", actualLineCount, maxLength),
                Severity.WARNING,
                parseResult.getFilePath(),
                1,
                1
            ));
        }
        
        return violations;
    }
    
    private int getMaxFileLength() {
        // Try to get from file-specific config first, then fall back to general config
        try {
            return config.getIntValue("linting.rules.codeStructure.maxFileLength", 1000);
        } catch (Exception e) {
            return 1000; // Default fallback
        }
    }
    
    private int countNonEmptyLines(String[] lines) {
        int count = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            // Skip empty lines and comment-only lines
            if (!trimmed.isEmpty() && 
                !trimmed.startsWith("//") && 
                !trimmed.startsWith("/*") && 
                !trimmed.equals("*/") &&
                !trimmed.startsWith("*")) { // Skip * continuation lines in block comments
                count++;
            }
        }
        
        return count;
    }
}
