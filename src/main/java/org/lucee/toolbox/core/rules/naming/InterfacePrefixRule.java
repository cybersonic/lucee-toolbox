package org.lucee.toolbox.core.rules.naming;

import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.Severity;
import org.lucee.toolbox.core.parser.ParseResult;
import org.lucee.toolbox.core.rules.LintingRule;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule that enforces interface naming conventions with specified prefix
 */
public class InterfacePrefixRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    // Pattern to detect interface declarations
    private static final Pattern INTERFACE_PATTERN = 
        Pattern.compile("^\\s*interface\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?:extends\\s+[^{]*)?\\s*\\{", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    // Pattern to detect if a component has only abstract methods (likely an interface)
    private static final Pattern ABSTRACT_FUNCTION_PATTERN = 
        Pattern.compile("function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\([^)]*\\)\\s*;", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    public InterfacePrefixRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "INTERFACE_PREFIX";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        // Only check .cfc files
        if (!parseResult.getFilePath().toLowerCase().endsWith(".cfc")) {
            return violations;
        }
        
        String expectedPrefix = config.getStringValue("linting.rules.naming.interfacePrefix", "I");
        if (expectedPrefix == null || expectedPrefix.isEmpty()) {
            return violations; // No prefix required
        }
        
        String content = parseResult.getContent();
        
        // Check for explicit interface declarations
        checkExplicitInterfaces(content, parseResult, violations, expectedPrefix);
        
        // Check for components that appear to be interfaces (only abstract methods)
        checkImplicitInterfaces(content, parseResult, violations, expectedPrefix);
        
        return violations;
    }
    
    private void checkExplicitInterfaces(String content, ParseResult parseResult, 
                                       List<LintingViolation> violations, String expectedPrefix) {
        Matcher matcher = INTERFACE_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String interfaceName = matcher.group(1);
            
            if (!interfaceName.startsWith(expectedPrefix)) {
                int line = getLineNumber(content, matcher.start());
                int column = getColumnNumber(content, matcher.start(1));
                
                violations.add(new LintingViolation(
                    getRuleId(),
                    String.format("Interface name '%s' should start with prefix '%s'", interfaceName, expectedPrefix),
                    Severity.INFO,
                    parseResult.getFilePath(),
                    line,
                    column
                ));
            }
        }
    }
    
    private void checkImplicitInterfaces(String content, ParseResult parseResult, 
                                       List<LintingViolation> violations, String expectedPrefix) {
        // Check if this component looks like an interface (only has abstract method declarations)
        if (looksLikeInterface(content)) {
            Path path = Paths.get(parseResult.getFilePath());
            String fileName = path.getFileName().toString();
            String componentName = fileName.substring(0, fileName.lastIndexOf('.'));
            
            if (!componentName.startsWith(expectedPrefix)) {
                violations.add(new LintingViolation(
                    getRuleId(),
                    String.format("Component '%s' appears to be an interface and should start with prefix '%s'", 
                                componentName, expectedPrefix),
                    Severity.INFO,
                    parseResult.getFilePath(),
                    1,
                    1
                ));
            }
        }
    }
    
    private boolean looksLikeInterface(String content) {
        // Remove comments and strings to avoid false positives
        String cleanContent = removeCommentsAndStrings(content);
        
        // Count function declarations vs function implementations
        Pattern functionDeclPattern = Pattern.compile("function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\([^)]*\\)\\s*;", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Pattern functionImplPattern = Pattern.compile("function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\([^)]*\\)\\s*\\{", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        Matcher declMatcher = functionDeclPattern.matcher(cleanContent);
        Matcher implMatcher = functionImplPattern.matcher(cleanContent);
        
        int declarationCount = 0;
        int implementationCount = 0;
        
        while (declMatcher.find()) {
            declarationCount++;
        }
        
        while (implMatcher.find()) {
            implementationCount++;
        }
        
        // If it has function declarations but no implementations, likely an interface
        return declarationCount > 0 && implementationCount == 0;
    }
    
    private String removeCommentsAndStrings(String content) {
        // Simple removal of single-line comments, multi-line comments, and strings
        content = content.replaceAll("//.*$", ""); // Single-line comments
        content = content.replaceAll("/\\*[\\s\\S]*?\\*/", ""); // Multi-line comments
        content = content.replaceAll("\"[^\"]*\"", "\"\""); // Double-quoted strings
        content = content.replaceAll("'[^']*'", "''"); // Single-quoted strings
        return content;
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
