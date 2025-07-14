package org.lucee.toolbox.core.rules.structure;

import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.Severity;
import org.lucee.toolbox.core.parser.ParseResult;
import org.lucee.toolbox.core.rules.LintingRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule that enforces maximum line length limits
 */
public class MaxLineLengthRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    public MaxLineLengthRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "MAX_LINE_LENGTH";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        int maxLength = config.getMaxLineLength();
        if (maxLength <= 0) {
            return violations; // Disabled
        }
        
        String[] lines = parseResult.getLines();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Calculate actual display width (tabs count as configured tab width)
            int displayWidth = calculateDisplayWidth(line);
            
            if (displayWidth > maxLength) {
                violations.add(new LintingViolation(
                    getRuleId(),
                    String.format("Line is too long (%d characters, max %d)", displayWidth, maxLength),
                    Severity.WARNING,
                    parseResult.getFilePath(),
                    i + 1,
                    displayWidth
                ));
            }
        }
        
        return violations;
    }
    
    private int calculateDisplayWidth(String line) {
        int width = 0;
        int tabSize = config.getIndentSize();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\t') {
                // Calculate tab width based on current position
                width += tabSize - (width % tabSize);
            } else {
                width++;
            }
        }
        
        return width;
    }
}
