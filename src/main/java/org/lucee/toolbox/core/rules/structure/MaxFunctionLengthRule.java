package org.lucee.toolbox.core.rules.structure;

import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.Severity;
import org.lucee.toolbox.core.parser.ParseResult;
import org.lucee.toolbox.core.rules.LintingRule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule that enforces maximum function length limits
 */
public class MaxFunctionLengthRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    // Pattern to match function declarations and their bodies
    private static final Pattern FUNCTION_PATTERN = 
        Pattern.compile("(?i)(?:^|\\s)(?:(public|private|package|remote)\\s+)?(?:(static)\\s+)?(?:([a-zA-Z_][a-zA-Z0-9_]*)\\s+)?function\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\([^)]*\\)\\s*\\{([^}]*)\\}", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    
    public MaxFunctionLengthRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "MAX_FUNCTION_LENGTH";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        int maxLength = config.getMaxFunctionLength();
        if (maxLength <= 0) {
            return violations; // Disabled
        }
        
        String content = parseResult.getContent();
        Matcher matcher = FUNCTION_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String functionName = matcher.group(4);
            String functionBody = matcher.group(5);
            
            // Count non-empty lines in function body
            String[] bodyLines = functionBody.split("\\r?\\n");
            int lineCount = 0;
            
            for (String line : bodyLines) {
                String trimmedLine = line.trim();
                // Skip empty lines and comment-only lines
                if (!trimmedLine.isEmpty() && 
                    !trimmedLine.startsWith("//") && 
                    !trimmedLine.startsWith("/*") && 
                    !trimmedLine.equals("*/")) {
                    lineCount++;
                }
            }
            
            if (lineCount > maxLength) {
                int functionStartLine = getLineNumber(content, matcher.start());
                violations.add(new LintingViolation(
                    getRuleId(),
                    String.format("Function '%s' is too long (%d lines, max %d)", functionName, lineCount, maxLength),
                    Severity.WARNING,
                    parseResult.getFilePath(),
                    functionStartLine,
                    1
                ));
            }
        }
        
        return violations;
    }
    
    private int getLineNumber(String content, int position) {
        int line = 1;
        for (int i = 0; i < position && i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }
}
