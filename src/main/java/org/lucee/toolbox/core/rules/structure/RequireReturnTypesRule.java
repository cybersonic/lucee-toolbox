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
 * Rule that enforces function return type annotations
 */
public class RequireReturnTypesRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    // Pattern to match functions without return types
    private static final Pattern FUNCTION_WITHOUT_RETURN_TYPE_PATTERN = 
        Pattern.compile("(?i)(?:^|\\s)(?:(public|private|package|remote)\\s+)?(?:(static)\\s+)?function\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    // Pattern to match functions WITH return types
    private static final Pattern FUNCTION_WITH_RETURN_TYPE_PATTERN = 
        Pattern.compile("(?i)(?:^|\\s)(?:(public|private|package|remote)\\s+)?(?:(static)\\s+)?([a-zA-Z_][a-zA-Z0-9_]*)\\s+function\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    public RequireReturnTypesRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "REQUIRE_RETURN_TYPES";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        if (!config.shouldRequireReturnTypes()) {
            return violations;
        }
        
        String content = parseResult.getContent();
        
        // Find all functions
        Matcher functionMatcher = FUNCTION_WITHOUT_RETURN_TYPE_PATTERN.matcher(content);
        
        while (functionMatcher.find()) {
            String functionName = functionMatcher.group(3);
            
            // Skip constructor functions and event handlers
            if (isSpecialFunction(functionName)) {
                continue;
            }
            
            // Check if this function actually has a return type by checking if it matches the "with return type" pattern
            String functionDeclaration = extractFunctionDeclaration(content, functionMatcher.start());
            
            if (!hasReturnType(functionDeclaration)) {
                int line = getLineNumber(content, functionMatcher.start());
                violations.add(new LintingViolation(
                    getRuleId(),
                    String.format("Function '%s' should specify a return type", functionName),
                    Severity.WARNING,
                    parseResult.getFilePath(),
                    line,
                    1
                ));
            }
        }
        
        return violations;
    }
    
    private boolean isSpecialFunction(String functionName) {
        // Constructor and event handler functions that might not need return types
        return functionName.equals("init") ||
               functionName.startsWith("on") && Character.isUpperCase(functionName.charAt(2)) ||
               functionName.equals("onApplicationStart") ||
               functionName.equals("onApplicationEnd") ||
               functionName.equals("onSessionStart") ||
               functionName.equals("onSessionEnd") ||
               functionName.equals("onRequestStart") ||
               functionName.equals("onRequestEnd") ||
               functionName.equals("onError") ||
               functionName.equals("onMissingMethod") ||
               functionName.equals("onMissingTemplate");
    }
    
    private String extractFunctionDeclaration(String content, int startPos) {
        // Extract the function declaration line
        int lineStart = content.lastIndexOf('\n', startPos) + 1;
        int lineEnd = content.indexOf('\n', startPos);
        if (lineEnd == -1) lineEnd = content.length();
        
        return content.substring(lineStart, lineEnd);
    }
    
    private boolean hasReturnType(String functionDeclaration) {
        // Check if the function declaration has a return type
        // Pattern: [access] [static] returnType function functionName(
        Pattern withTypePattern = Pattern.compile("(?i)(?:^|\\s)(?:(?:public|private|package|remote)\\s+)?(?:static\\s+)?([a-zA-Z_][a-zA-Z0-9_]*)\\s+function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(");
        
        return withTypePattern.matcher(functionDeclaration).find();
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
