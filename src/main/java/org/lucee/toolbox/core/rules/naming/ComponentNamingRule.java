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
 * Rule that enforces component naming conventions
 */
public class ComponentNamingRule implements LintingRule {
    
    private final ConfigurationManager config;
    private static final Pattern COMPONENT_DECLARATION_PATTERN = 
        Pattern.compile("^\\s*component\\s+(?:extends\\s*=\\s*[\"']?[^\\s\"']+[\"']?\\s+)?(?:accessors\\s*=\\s*[\"']?(?:true|false)[\"']?\\s+)?(?:displayname\\s*=\\s*[\"'][^\"']*[\"']\\s+)?(?:hint\\s*=\\s*[\"'][^\"']*[\"']\\s+)?(?:output\\s*=\\s*[\"']?(?:true|false)[\"']?\\s+)?(?:persistent\\s*=\\s*[\"']?(?:true|false)[\"']?\\s+)?(?:name\\s*=\\s*[\"']?([^\\s\"']+)[\"']?\\s*)?\\{?", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    public ComponentNamingRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "COMPONENT_NAMING";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        // Only check .cfc files
        if (!parseResult.getFilePath().toLowerCase().endsWith(".cfc")) {
            return violations;
        }
        
        String expectedCase = config.getComponentCase();
        if (!"PascalCase".equals(expectedCase)) {
            return violations; // Only enforce PascalCase for now
        }
        
        // Check filename
        Path path = Paths.get(parseResult.getFilePath());
        String fileName = path.getFileName().toString();
        String componentName = fileName.substring(0, fileName.lastIndexOf('.'));
        
        if (!isPascalCase(componentName)) {
            violations.add(new LintingViolation(
                getRuleId(),
                String.format("Component filename '%s' should be PascalCase", componentName),
                Severity.WARNING,
                parseResult.getFilePath(),
                1,
                1
            ));
        }
        
        // Check component declaration name attribute if present
        String content = parseResult.getContent();
        String[] lines = parseResult.getLines();
        
        Matcher matcher = COMPONENT_DECLARATION_PATTERN.matcher(content);
        while (matcher.find()) {
            String declaredName = matcher.group(1);
            if (declaredName != null && !isPascalCase(declaredName)) {
                int line = getLineNumber(content, matcher.start());
                violations.add(new LintingViolation(
                    getRuleId(),
                    String.format("Component name attribute '%s' should be PascalCase", declaredName),
                    Severity.WARNING,
                    parseResult.getFilePath(),
                    line,
                    1
                ));
            }
        }
        
        return violations;
    }
    
    private boolean isPascalCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // PascalCase: starts with uppercase, no underscores, no spaces
        return name.matches("^[A-Z][a-zA-Z0-9]*$");
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
