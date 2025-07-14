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
 * Rule that enforces components to have an init() method
 */
public class RequireInitRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    // Pattern to detect component declarations
    private static final Pattern COMPONENT_PATTERN = 
        Pattern.compile("^\\s*component", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    // Pattern to detect init() method
    private static final Pattern INIT_METHOD_PATTERN = 
        Pattern.compile("\\bfunction\\s+init\\s*\\(", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    public RequireInitRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "REQUIRE_INIT";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        if (!config.shouldRequireInit()) {
            return violations;
        }
        
        // Only check .cfc files
        if (!parseResult.getFilePath().toLowerCase().endsWith(".cfc")) {
            return violations;
        }
        
        String content = parseResult.getContent();
        
        // Check if this is a component
        Matcher componentMatcher = COMPONENT_PATTERN.matcher(content);
        if (!componentMatcher.find()) {
            return violations; // Not a component
        }
        
        // Check if it has an init() method
        Matcher initMatcher = INIT_METHOD_PATTERN.matcher(content);
        if (!initMatcher.find()) {
            // Check if it might be an interface or abstract class (which might not need init)
            if (isInterfaceOrAbstract(content)) {
                return violations;
            }
            
            violations.add(new LintingViolation(
                getRuleId(),
                "Component should have an init() method for proper initialization",
                Severity.WARNING,
                parseResult.getFilePath(),
                1,
                1
            ));
        }
        
        return violations;
    }
    
    private boolean isInterfaceOrAbstract(String content) {
        // Check if it's an interface (only has function declarations, no implementations)
        Pattern functionImplPattern = Pattern.compile("function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\([^)]*\\)\\s*\\{", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        Pattern functionDeclPattern = Pattern.compile("function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\([^)]*\\)\\s*;", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        Matcher implMatcher = functionImplPattern.matcher(content);
        Matcher declMatcher = functionDeclPattern.matcher(content);
        
        int implementations = 0;
        int declarations = 0;
        
        while (implMatcher.find()) {
            implementations++;
        }
        
        while (declMatcher.find()) {
            declarations++;
        }
        
        // If it has declarations but no implementations, likely an interface
        return declarations > 0 && implementations == 0;
    }
    
    private boolean shouldRequireInit() {
        try {
            return config.getBooleanValue("linting.rules.codeStructure.requireInit", true);
        } catch (Exception e) {
            return true; // Default to requiring init
        }
    }
}
