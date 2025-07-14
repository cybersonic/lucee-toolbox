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

/**
 * Rule that enforces file naming conventions
 */
public class FileNamingRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    public FileNamingRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "FILE_NAMING";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        Path path = Paths.get(parseResult.getFilePath());
        String fileName = path.getFileName().toString();
        
        if (fileName.toLowerCase().endsWith(".cfc")) {
            checkCfcFileNaming(fileName, parseResult, violations);
        } else if (fileName.toLowerCase().endsWith(".cfm") || fileName.toLowerCase().endsWith(".cfml")) {
            checkCfmFileNaming(fileName, parseResult, violations);
        }
        
        return violations;
    }
    
    private void checkCfcFileNaming(String fileName, ParseResult parseResult, List<LintingViolation> violations) {
        String expectedCase = "PascalCase"; // CFC files should be PascalCase
        String componentName = fileName.substring(0, fileName.lastIndexOf('.'));
        
        if (!isPascalCase(componentName)) {
            violations.add(new LintingViolation(
                getRuleId(),
                String.format("CFC filename '%s' should be PascalCase", componentName),
                Severity.WARNING,
                parseResult.getFilePath(),
                1,
                1
            ));
        }
        
        // Check for common anti-patterns
        if (componentName.contains("_")) {
            violations.add(new LintingViolation(
                getRuleId(),
                String.format("CFC filename '%s' should not contain underscores, use PascalCase instead", componentName),
                Severity.WARNING,
                parseResult.getFilePath(),
                1,
                1
            ));
        }
        
        if (componentName.contains("-")) {
            violations.add(new LintingViolation(
                getRuleId(),
                String.format("CFC filename '%s' should not contain hyphens, use PascalCase instead", componentName),
                Severity.WARNING,
                parseResult.getFilePath(),
                1,
                1
            ));
        }
    }
    
    private void checkCfmFileNaming(String fileName, ParseResult parseResult, List<LintingViolation> violations) {
        String expectedCase = config.getStringValue("linting.rules.naming.cfmFileCase", "camelCase");
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        
        if ("camelCase".equals(expectedCase) && !isCamelCase(baseName)) {
            violations.add(new LintingViolation(
                getRuleId(),
                String.format("CFM filename '%s' should be camelCase", baseName),
                Severity.WARNING,
                parseResult.getFilePath(),
                1,
                1
            ));
        }
        
        // Check for common anti-patterns in CFM files
        if (baseName.contains("_") && !baseName.equals("Application") && !baseName.equals("OnRequestEnd")) {
            violations.add(new LintingViolation(
                getRuleId(),
                String.format("CFM filename '%s' should avoid underscores, use camelCase instead", baseName),
                Severity.INFO,
                parseResult.getFilePath(),
                1,
                1
            ));
        }
    }
    
    private boolean isPascalCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // PascalCase: starts with uppercase, no underscores, no hyphens
        return name.matches("^[A-Z][a-zA-Z0-9]*$");
    }
    
    private boolean isCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // camelCase: starts with lowercase, no underscores, no hyphens
        // Exception: Allow some special CFM files like Application.cfm, index.cfm
        if (name.equals("Application") || name.equals("index") || name.equals("error") || 
            name.equals("404") || name.equals("500")) {
            return true;
        }
        
        return name.matches("^[a-z][a-zA-Z0-9]*$");
    }
}
