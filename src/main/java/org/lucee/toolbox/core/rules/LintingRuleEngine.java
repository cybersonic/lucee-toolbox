package org.lucee.toolbox.core.rules;

import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.Severity;
import org.lucee.toolbox.core.parser.ParseResult;
import org.lucee.toolbox.core.rules.naming.*;
import org.lucee.toolbox.core.rules.structure.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Engine for applying linting rules to parsed CFML code
 */
public class LintingRuleEngine {
    
    private final ConfigurationManager configManager;
    private final List<LintingRule> rules;
    
    public LintingRuleEngine(ConfigurationManager configManager) {
        this.configManager = configManager;
        this.rules = new ArrayList<>();
        initializeRules();
    }
    
    /**
     * Analyze a parsed file and return violations
     */
    public List<LintingViolation> analyzeFile(ParseResult parseResult, String filePath) {
        List<LintingViolation> violations = new ArrayList<>();
        
        // Apply each rule to the parse result
        for (LintingRule rule : rules) {
            try {
                List<LintingViolation> ruleViolations = rule.analyze(parseResult);
                violations.addAll(ruleViolations);
            } catch (Exception e) {
                // Log rule execution error but continue with other rules
                violations.add(new LintingViolation(
                    "RULE_EXECUTION_ERROR",
                    "Rule " + rule.getRuleId() + " failed: " + e.getMessage(),
                    Severity.WARNING,
                    filePath,
                    1,
                    1
                ));
            }
        }
        
        return violations;
    }
    
    /**
     * Initialize the default set of rules
     */
    private void initializeRules() {
        // Add basic whitespace rules
        rules.add(new BasicTrailingWhitespaceRule(configManager));
        rules.add(new BasicEmptyLineRule(configManager));
        
        // Add naming convention rules
        rules.add(new ComponentNamingRule(configManager));
        rules.add(new FunctionNamingRule(configManager));
        rules.add(new VariableNamingRule(configManager));
        rules.add(new ConstantNamingRule(configManager));
        rules.add(new FileNamingRule(configManager));
        rules.add(new InterfacePrefixRule(configManager));
        rules.add(new AbstractSuffixRule(configManager));
        
        // Add code structure rules
        rules.add(new RequireCurlyBracesRule(configManager));
        rules.add(new MaxFunctionLengthRule(configManager));
        rules.add(new MaxLineLengthRule(configManager));
        rules.add(new MaxFileLengthRule(configManager));
        rules.add(new RequireInitRule(configManager));
        rules.add(new RequireReturnTypesRule(configManager));
        rules.add(new AdditionalStructureRules.RequireArgumentTypesRule(configManager));
        rules.add(new AdditionalStructureRules.UseAccessorsRule(configManager));
        rules.add(new AdditionalStructureRules.CurlyBraceStyleRule(configManager));
        
        // TODO: Add more comprehensive rule implementations:
        // - CFLint rule adapters
        // - Security rules
        // - Best practice rules
        // - Additional whitespace & formatting rules
    }
    
    /**
     * Basic rule to check for trailing whitespace
     */
    private static class BasicTrailingWhitespaceRule implements LintingRule {
        private final ConfigurationManager config;
        
        public BasicTrailingWhitespaceRule(ConfigurationManager config) {
            this.config = config;
        }
        
        @Override
        public String getRuleId() {
            return "TRAILING_WHITESPACE";
        }
        
        @Override
        public List<LintingViolation> analyze(ParseResult parseResult) {
            List<LintingViolation> violations = new ArrayList<>();
            
            if (!config.shouldTrimTrailingWhitespace()) {
                return violations;
            }
            
            String[] lines = parseResult.getLines();
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.length() > 0 && Character.isWhitespace(line.charAt(line.length() - 1))) {
                    violations.add(new LintingViolation(
                        getRuleId(),
                        "Line has trailing whitespace",
                        Severity.WARNING,
                        parseResult.getFilePath(),
                        i + 1,
                        line.length()
                    ));
                }
            }
            
            return violations;
        }
    }
    
    /**
     * Basic rule to check for too many empty lines
     */
    private static class BasicEmptyLineRule implements LintingRule {
        private final ConfigurationManager config;
        
        public BasicEmptyLineRule(ConfigurationManager config) {
            this.config = config;
        }
        
        @Override
        public String getRuleId() {
            return "EXCESSIVE_EMPTY_LINES";
        }
        
        @Override
        public List<LintingViolation> analyze(ParseResult parseResult) {
            List<LintingViolation> violations = new ArrayList<>();
            
            int maxEmptyLines = config.getMaxEmptyLines();
            String[] lines = parseResult.getLines();
            int consecutiveEmptyLines = 0;
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                
                if (line.isEmpty()) {
                    consecutiveEmptyLines++;
                } else {
                    if (consecutiveEmptyLines > maxEmptyLines) {
                        violations.add(new LintingViolation(
                            getRuleId(),
                            String.format("Too many consecutive empty lines (%d, max %d)", 
                                    consecutiveEmptyLines, maxEmptyLines),
                            Severity.INFO,
                            parseResult.getFilePath(),
                            i,
                            1
                        ));
                    }
                    consecutiveEmptyLines = 0;
                }
            }
            
            return violations;
        }
    }
}
