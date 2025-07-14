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
 * Combined rules for requiring argument types, accessors, and curly brace style
 */
public class AdditionalStructureRules {

    /**
     * Rule that enforces function argument type annotations
     */
    public static class RequireArgumentTypesRule implements LintingRule {
        
        private final ConfigurationManager config;
        
        private static final Pattern FUNCTION_PATTERN = 
            Pattern.compile("function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(([^)]+)\\)", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        private static final Pattern TYPED_ARG_PATTERN = 
            Pattern.compile("(?:required\\s+)?([a-zA-Z_][a-zA-Z0-9_]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)", 
            Pattern.CASE_INSENSITIVE);
        
        public RequireArgumentTypesRule(ConfigurationManager config) {
            this.config = config;
        }
        
        @Override
        public String getRuleId() {
            return "REQUIRE_ARGUMENT_TYPES";
        }
        
        @Override
        public List<LintingViolation> analyze(ParseResult parseResult) {
            List<LintingViolation> violations = new ArrayList<>();
            
            if (!config.shouldRequireArgumentTypes()) {
                return violations;
            }
            
            String content = parseResult.getContent();
            Matcher functionMatcher = FUNCTION_PATTERN.matcher(content);
            
            while (functionMatcher.find()) {
                String argumentsStr = functionMatcher.group(1);
                
                // Split arguments by comma
                String[] arguments = argumentsStr.split(",");
                for (String arg : arguments) {
                    arg = arg.trim();
                    if (arg.isEmpty()) continue;
                    
                    // Check if argument has type annotation
                    if (!hasTypeAnnotation(arg)) {
                        int line = getLineNumber(content, functionMatcher.start());
                        violations.add(new LintingViolation(
                            getRuleId(),
                            String.format("Function argument '%s' should specify a type", getArgumentName(arg)),
                            Severity.WARNING,
                            parseResult.getFilePath(),
                            line,
                            1
                        ));
                    }
                }
            }
            
            return violations;
        }
        
        private boolean hasTypeAnnotation(String argument) {
            return TYPED_ARG_PATTERN.matcher(argument).find();
        }
        
        private String getArgumentName(String argument) {
            // Extract argument name (last word in the argument declaration)
            String[] parts = argument.trim().split("\\s+");
            return parts[parts.length - 1].replaceAll("[=].*", "");
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

    /**
     * Rule that enforces components with properties to use accessors="true"
     */
    public static class UseAccessorsRule implements LintingRule {
        
        private final ConfigurationManager config;
        
        private static final Pattern COMPONENT_PATTERN = 
            Pattern.compile("^\\s*component\\s*([^{]*)?\\{", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        private static final Pattern PROPERTY_PATTERN = 
            Pattern.compile("^\\s*property\\s+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        private static final Pattern ACCESSORS_PATTERN = 
            Pattern.compile("accessors\\s*=\\s*[\"']?true[\"']?", Pattern.CASE_INSENSITIVE);
        
        public UseAccessorsRule(ConfigurationManager config) {
            this.config = config;
        }
        
        @Override
        public String getRuleId() {
            return "USE_ACCESSORS";
        }
        
        @Override
        public List<LintingViolation> analyze(ParseResult parseResult) {
            List<LintingViolation> violations = new ArrayList<>();
            
            if (!config.shouldUseAccessors()) {
                return violations;
            }
            
            // Only check .cfc files
            if (!parseResult.getFilePath().toLowerCase().endsWith(".cfc")) {
                return violations;
            }
            
            String content = parseResult.getContent();
            
            // Check if component has properties
            Matcher propertyMatcher = PROPERTY_PATTERN.matcher(content);
            if (propertyMatcher.find()) {
                // Component has properties, check if it uses accessors
                Matcher componentMatcher = COMPONENT_PATTERN.matcher(content);
                if (componentMatcher.find()) {
                    String componentDeclaration = componentMatcher.group(1);
                    if (componentDeclaration == null || !ACCESSORS_PATTERN.matcher(componentDeclaration).find()) {
                        int line = getLineNumber(content, componentMatcher.start());
                        violations.add(new LintingViolation(
                            getRuleId(),
                            "Component with properties should use accessors=\"true\"",
                            Severity.INFO,
                            parseResult.getFilePath(),
                            line,
                            1
                        ));
                    }
                }
            }
            
            return violations;
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

    /**
     * Rule that enforces curly brace style (same-line vs new-line)
     */
    public static class CurlyBraceStyleRule implements LintingRule {
        
        private final ConfigurationManager config;
        
        private static final Pattern BRACE_PATTERN = 
            Pattern.compile("(?i)(if|else|for|while|function|component|try|catch)\\s*(?:\\([^)]*\\))?\\s*\\n\\s*\\{", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        
        public CurlyBraceStyleRule(ConfigurationManager config) {
            this.config = config;
        }
        
        @Override
        public String getRuleId() {
            return "CURLY_BRACE_STYLE";
        }
        
        @Override
        public List<LintingViolation> analyze(ParseResult parseResult) {
            List<LintingViolation> violations = new ArrayList<>();
            
            String expectedStyle = config.getCurlyBraceStyle();
            if (!"same-line".equals(expectedStyle)) {
                return violations; // Only enforce same-line style for now
            }
            
            String content = parseResult.getContent();
            Matcher matcher = BRACE_PATTERN.matcher(content);
            
            while (matcher.find()) {
                String keyword = matcher.group(1);
                int line = getLineNumber(content, matcher.start());
                violations.add(new LintingViolation(
                    getRuleId(),
                    String.format("%s statement should have opening brace on same line", keyword),
                    Severity.INFO,
                    parseResult.getFilePath(),
                    line,
                    1
                ));
            }
            
            return violations;
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
}
