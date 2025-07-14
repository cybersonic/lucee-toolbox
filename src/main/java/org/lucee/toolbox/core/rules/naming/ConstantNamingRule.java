package org.lucee.toolbox.core.rules.naming;

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
 * Rule that enforces constant naming conventions
 */
public class ConstantNamingRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    // Pattern to match constant declarations (variables that are all uppercase or assigned static values)
    private static final Pattern CONSTANT_PATTERN = 
        Pattern.compile("(?:^|\\s)(?:variables\\.|this\\.|local\\.)?([A-Z][A-Z0-9_]*)\\s*=\\s*(?:[\"'][^\"']*[\"']|\\d+|true|false|\\[[^\\]]*\\]|\\{[^}]*\\})", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    // Pattern to match explicit constant declarations
    private static final Pattern EXPLICIT_CONSTANT_PATTERN = 
        Pattern.compile("(?:^|\\s)(?:static\\s+)?(?:final\\s+)?(?:variables\\.|this\\.|local\\.)?([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(?:[\"'][^\"']*[\"']|\\d+|true|false)", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    public ConstantNamingRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "CONSTANT_NAMING";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        String expectedCase = config.getConstantCase();
        if (!"UPPER_CASE".equals(expectedCase)) {
            return violations; // Only enforce UPPER_CASE for now
        }
        
        String content = parseResult.getContent();
        
        // Check for constants based on naming pattern (all uppercase variables)
        checkConstantsByPattern(content, parseResult.getFilePath(), violations);
        
        return violations;
    }
    
    private void checkConstantsByPattern(String content, String filePath, List<LintingViolation> violations) {
        // Look for variables that appear to be constants but don't follow UPPER_CASE
        Pattern variablePattern = Pattern.compile("(?:^|\\s)(?:variables\\.|this\\.|local\\.)?([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(?:[\"'][^\"']*[\"']|\\d+|true|false)", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        Matcher matcher = variablePattern.matcher(content);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            
            // Skip if it's already in correct format
            if (isUpperCase(variableName)) {
                continue;
            }
            
            // Check if this looks like a constant (assigned a literal value and never reassigned)
            if (appearsToBeConstant(content, variableName)) {
                int line = getLineNumber(content, matcher.start());
                int column = getColumnNumber(content, matcher.start(1));
                
                violations.add(new LintingViolation(
                    getRuleId(),
                    String.format("Constant '%s' should be UPPER_CASE", variableName),
                    Severity.WARNING,
                    filePath,
                    line,
                    column
                ));
            }
        }
    }
    
    private boolean isUpperCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // UPPER_CASE: all uppercase letters, numbers, and underscores
        return name.matches("^[A-Z][A-Z0-9_]*$");
    }
    
    private boolean appearsToBeConstant(String content, String variableName) {
        // Heuristics to determine if a variable is likely a constant:
        // 1. Has multiple uppercase letters
        // 2. Contains underscores between words
        // 3. Is only assigned once in the file
        // 4. Assigned a literal value (string, number, boolean)
        
        if (variableName.contains("_") && variableName.toUpperCase().equals(variableName)) {
            return true;
        }
        
        // Check if variable name suggests it's a constant (multiple caps or underscores)
        if (variableName.matches(".*[A-Z].*[A-Z].*") || variableName.contains("_")) {
            // Count assignments to this variable
            Pattern assignmentPattern = Pattern.compile("\\b" + Pattern.quote(variableName) + "\\s*=", 
                Pattern.CASE_INSENSITIVE);
            Matcher assignmentMatcher = assignmentPattern.matcher(content);
            
            int assignmentCount = 0;
            while (assignmentMatcher.find()) {
                assignmentCount++;
            }
            
            // If only assigned once, likely a constant
            return assignmentCount == 1;
        }
        
        return false;
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
    
    private int getColumnNumber(String content, int position) {
        int column = 1;
        for (int i = position - 1; i >= 0 && content.charAt(i) != '\n'; i--) {
            column++;
        }
        return column;
    }
}
