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
 * Rule that enforces abstract class naming conventions with specified suffix
 */
public class AbstractSuffixRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    // Pattern to detect abstract components
    private static final Pattern ABSTRACT_COMPONENT_PATTERN = 
        Pattern.compile("^\\s*component\\s+[^{]*\\babstract\\s*=\\s*[\"']?true[\"']?[^{]*\\{", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    // Pattern to detect components with abstract methods
    private static final Pattern ABSTRACT_METHOD_PATTERN = 
        Pattern.compile("(?:^|\\s)(?:public|private|package|remote)?\\s*abstract\\s+function\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    // Pattern to detect base/parent components that are meant to be extended
    private static final Pattern BASE_COMPONENT_INDICATORS = 
        Pattern.compile("(?i)(?:base|parent|abstract|template)", Pattern.CASE_INSENSITIVE);
    
    public AbstractSuffixRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "ABSTRACT_SUFFIX";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        // Only check .cfc files
        if (!parseResult.getFilePath().toLowerCase().endsWith(".cfc")) {
            return violations;
        }
        
        String expectedSuffix = config.getStringValue("linting.rules.naming.abstractSuffix", "Abstract");
        if (expectedSuffix == null || expectedSuffix.isEmpty()) {
            return violations; // No suffix required
        }
        
        String content = parseResult.getContent();
        
        // Check for explicit abstract components
        if (isExplicitlyAbstract(content)) {
            checkAbstractNaming(parseResult, violations, expectedSuffix, "explicitly abstract");
        }
        
        // Check for components with abstract methods
        if (hasAbstractMethods(content)) {
            checkAbstractNaming(parseResult, violations, expectedSuffix, "contains abstract methods");
        }
        
        // Check for components that appear to be base/parent classes
        if (appearsToBeBaseClass(content, parseResult.getFilePath())) {
            checkAbstractNaming(parseResult, violations, expectedSuffix, "appears to be a base class");
        }
        
        return violations;
    }
    
    private boolean isExplicitlyAbstract(String content) {
        return ABSTRACT_COMPONENT_PATTERN.matcher(content).find();
    }
    
    private boolean hasAbstractMethods(String content) {
        return ABSTRACT_METHOD_PATTERN.matcher(content).find();
    }
    
    private boolean appearsToBeBaseClass(String content, String filePath) {
        // Check filename for base class indicators
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString().toLowerCase();
        
        if (fileName.contains("base") || fileName.contains("abstract") || 
            fileName.contains("parent") || fileName.contains("template")) {
            return true;
        }
        
        // Check for common base class patterns in content
        String lowerContent = content.toLowerCase();
        
        // Look for comments or documentation indicating this is a base class
        if (lowerContent.contains("base class") || lowerContent.contains("abstract class") ||
            lowerContent.contains("extend this") || lowerContent.contains("parent class")) {
            return true;
        }
        
        // Check for minimal implementation with mostly empty or throw methods
        Pattern emptyMethodPattern = Pattern.compile("function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\([^)]*\\)\\s*\\{\\s*(?://[^}]*|/\\*[^}]*\\*/\\s*)?\\s*\\}", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        Pattern throwMethodPattern = Pattern.compile("function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\([^)]*\\)\\s*\\{[^}]*throw[^}]*\\}", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        Matcher emptyMatcher = emptyMethodPattern.matcher(content);
        Matcher throwMatcher = throwMethodPattern.matcher(content);
        
        int emptyMethods = 0;
        int throwMethods = 0;
        
        while (emptyMatcher.find()) {
            emptyMethods++;
        }
        
        while (throwMatcher.find()) {
            throwMethods++;
        }
        
        // If more than half the methods are empty or throw, likely a base class
        Pattern allMethodsPattern = Pattern.compile("function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\([^)]*\\)\\s*\\{", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        Matcher allMethodsMatcher = allMethodsPattern.matcher(content);
        int totalMethods = 0;
        while (allMethodsMatcher.find()) {
            totalMethods++;
        }
        
        return totalMethods > 0 && (emptyMethods + throwMethods) > totalMethods / 2;
    }
    
    private void checkAbstractNaming(ParseResult parseResult, List<LintingViolation> violations, 
                                   String expectedSuffix, String reason) {
        Path path = Paths.get(parseResult.getFilePath());
        String fileName = path.getFileName().toString();
        String componentName = fileName.substring(0, fileName.lastIndexOf('.'));
        
        if (!componentName.endsWith(expectedSuffix)) {
            violations.add(new LintingViolation(
                getRuleId(),
                String.format("Component '%s' %s and should end with suffix '%s'", 
                            componentName, reason, expectedSuffix),
                Severity.INFO,
                parseResult.getFilePath(),
                1,
                1
            ));
        }
    }
}
