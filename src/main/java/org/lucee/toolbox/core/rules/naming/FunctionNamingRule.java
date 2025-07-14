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
 * Rule that enforces function naming conventions
 */
public class FunctionNamingRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    // Pattern to match function declarations
    private static final Pattern FUNCTION_PATTERN = 
        Pattern.compile("(?:^|\\s)(?:(public|private|package|remote)\\s+)?(?:(static)\\s+)?(?:([a-zA-Z_][a-zA-Z0-9_]*)\\s+)?function\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    public FunctionNamingRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "FUNCTION_NAMING";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        String expectedCase = config.getFunctionCase();
        if (!"camelCase".equals(expectedCase)) {
            return violations; // Only enforce camelCase for now
        }
        
        String content = parseResult.getContent();
        Matcher matcher = FUNCTION_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String functionName = matcher.group(4);
            
            // Skip constructor functions (init) and special methods
            if ("init".equals(functionName) || isSpecialMethod(functionName)) {
                continue;
            }
            
            if (!isCamelCase(functionName)) {
                int line = getLineNumber(content, matcher.start());
                int column = getColumnNumber(content, matcher.start(4));
                
                violations.add(new LintingViolation(
                    getRuleId(),
                    String.format("Function name '%s' should be camelCase", functionName),
                    Severity.WARNING,
                    parseResult.getFilePath(),
                    line,
                    column
                ));
            }
        }
        
        return violations;
    }
    
    private boolean isCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // camelCase: starts with lowercase, no underscores, no spaces
        return name.matches("^[a-z][a-zA-Z0-9]*$");
    }
    
    private boolean isSpecialMethod(String name) {
        // Common special methods that might have different naming conventions
        return name.equals("onApplicationStart") ||
               name.equals("onApplicationEnd") ||
               name.equals("onSessionStart") ||
               name.equals("onSessionEnd") ||
               name.equals("onRequestStart") ||
               name.equals("onRequestEnd") ||
               name.equals("onError") ||
               name.equals("onMissingMethod") ||
               name.equals("onMissingTemplate") ||
               name.startsWith("on") && Character.isUpperCase(name.charAt(2));
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
