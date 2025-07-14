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
 * Rule that enforces curly braces for all control structures
 */
public class RequireCurlyBracesRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    // Pattern to detect control structures without curly braces
    private static final Pattern CONTROL_STRUCTURE_PATTERN = 
        Pattern.compile("(?i)\\b(if|else|for|while|switch|try|catch|finally)\\s*\\([^)]*\\)\\s*(?!\\{)([^;{]+;)", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    // Pattern to detect single-statement control structures
    private static final Pattern SINGLE_STATEMENT_PATTERN = 
        Pattern.compile("(?i)\\b(if|else|for|while)\\s*(?:\\([^)]*\\))?\\s*(?!\\{)([^;\\r\\n]+;)", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    public RequireCurlyBracesRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "REQUIRE_CURLY_BRACES";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        if (!config.shouldRequireCurlyBraces()) {
            return violations;
        }
        
        String content = parseResult.getContent();
        
        // Check for if/else without braces
        checkIfElseStatements(content, parseResult.getFilePath(), violations);
        
        // Check for loops without braces
        checkLoopStatements(content, parseResult.getFilePath(), violations);
        
        // Check for try/catch without braces
        checkTryCatchStatements(content, parseResult.getFilePath(), violations);
        
        return violations;
    }
    
    private void checkIfElseStatements(String content, String filePath, List<LintingViolation> violations) {
        // Pattern for if statements without braces
        Pattern ifPattern = Pattern.compile("(?i)\\bif\\s*\\([^)]+\\)\\s*(?!\\{)([^;\\r\\n]+;)", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        Matcher matcher = ifPattern.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            violations.add(new LintingViolation(
                getRuleId(),
                "If statement should use curly braces",
                Severity.WARNING,
                filePath,
                line,
                1
            ));
        }
        
        // Pattern for else statements without braces
        Pattern elsePattern = Pattern.compile("(?i)\\belse\\s*(?!\\{|if)([^;\\r\\n]+;)", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        matcher = elsePattern.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            violations.add(new LintingViolation(
                getRuleId(),
                "Else statement should use curly braces",
                Severity.WARNING,
                filePath,
                line,
                1
            ));
        }
    }
    
    private void checkLoopStatements(String content, String filePath, List<LintingViolation> violations) {
        // Pattern for for loops without braces
        Pattern forPattern = Pattern.compile("(?i)\\bfor\\s*\\([^)]+\\)\\s*(?!\\{)([^;\\r\\n]+;)", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        Matcher matcher = forPattern.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            violations.add(new LintingViolation(
                getRuleId(),
                "For loop should use curly braces",
                Severity.WARNING,
                filePath,
                line,
                1
            ));
        }
        
        // Pattern for while loops without braces
        Pattern whilePattern = Pattern.compile("(?i)\\bwhile\\s*\\([^)]+\\)\\s*(?!\\{)([^;\\r\\n]+;)", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        matcher = whilePattern.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            violations.add(new LintingViolation(
                getRuleId(),
                "While loop should use curly braces",
                Severity.WARNING,
                filePath,
                line,
                1
            ));
        }
    }
    
    private void checkTryCatchStatements(String content, String filePath, List<LintingViolation> violations) {
        // Pattern for try statements without braces
        Pattern tryPattern = Pattern.compile("(?i)\\btry\\s*(?!\\{)([^;\\r\\n]+;)", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        Matcher matcher = tryPattern.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            violations.add(new LintingViolation(
                getRuleId(),
                "Try statement should use curly braces",
                Severity.WARNING,
                filePath,
                line,
                1
            ));
        }
        
        // Pattern for catch statements without braces
        Pattern catchPattern = Pattern.compile("(?i)\\bcatch\\s*\\([^)]*\\)\\s*(?!\\{)([^;\\r\\n]+;)", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        matcher = catchPattern.matcher(content);
        while (matcher.find()) {
            int line = getLineNumber(content, matcher.start());
            violations.add(new LintingViolation(
                getRuleId(),
                "Catch statement should use curly braces",
                Severity.WARNING,
                filePath,
                line,
                1
            ));
        }
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
