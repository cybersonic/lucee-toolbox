package org.lucee.toolbox.core.rules.naming;

import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.Severity;
import org.lucee.toolbox.core.parser.ParseResult;
import org.lucee.toolbox.core.rules.LintingRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule that enforces variable naming conventions
 */
public class VariableNamingRule implements LintingRule {
    
    private final ConfigurationManager config;
    
    // Pattern to match variable declarations
    private static final Pattern VAR_DECLARATION_PATTERN = 
        Pattern.compile("(?:^|\\s)(?:var\\s+|local\\.|variables\\.)([a-zA-Z_][a-zA-Z0-9_]*)\\s*=", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    // Pattern to match function arguments
    private static final Pattern FUNCTION_ARG_PATTERN = 
        Pattern.compile("function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(([^)]+)\\)", 
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    
    // Pattern to match individual arguments
    private static final Pattern ARG_PATTERN = 
        Pattern.compile("(?:required\\s+)?(?:[a-zA-Z_][a-zA-Z0-9_]*\\s+)?([a-zA-Z_][a-zA-Z0-9_]*)(?:\\s*=\\s*[^,)]+)?", 
        Pattern.CASE_INSENSITIVE);
    
    // Built-in variables that don't need to follow naming conventions
    private static final Set<String> BUILTIN_VARIABLES = new HashSet<>();
    static {
        BUILTIN_VARIABLES.add("arguments");
        BUILTIN_VARIABLES.add("variables");
        BUILTIN_VARIABLES.add("local");
        BUILTIN_VARIABLES.add("this");
        BUILTIN_VARIABLES.add("super");
        BUILTIN_VARIABLES.add("session");
        BUILTIN_VARIABLES.add("application");
        BUILTIN_VARIABLES.add("request");
        BUILTIN_VARIABLES.add("form");
        BUILTIN_VARIABLES.add("url");
        BUILTIN_VARIABLES.add("cookie");
        BUILTIN_VARIABLES.add("server");
        BUILTIN_VARIABLES.add("client");
        BUILTIN_VARIABLES.add("cgi");
    }
    
    public VariableNamingRule(ConfigurationManager config) {
        this.config = config;
    }
    
    @Override
    public String getRuleId() {
        return "VARIABLE_NAMING";
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        
        String expectedCase = config.getVariableCase();
        if (!"camelCase".equals(expectedCase)) {
            return violations; // Only enforce camelCase for now
        }
        
        String content = parseResult.getContent();
        
        // Check variable declarations
        checkVariableDeclarations(content, parseResult.getFilePath(), violations);
        
        // Check function arguments
        checkFunctionArguments(content, parseResult.getFilePath(), violations);
        
        return violations;
    }
    
    private void checkVariableDeclarations(String content, String filePath, List<LintingViolation> violations) {
        Matcher matcher = VAR_DECLARATION_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            
            if (BUILTIN_VARIABLES.contains(variableName.toLowerCase())) {
                continue;
            }
            
            if (!isCamelCase(variableName)) {
                int line = getLineNumber(content, matcher.start());
                int column = getColumnNumber(content, matcher.start(1));
                
                violations.add(new LintingViolation(
                    getRuleId(),
                    String.format("Variable name '%s' should be camelCase", variableName),
                    Severity.WARNING,
                    filePath,
                    line,
                    column
                ));
            }
        }
    }
    
    private void checkFunctionArguments(String content, String filePath, List<LintingViolation> violations) {
        Matcher functionMatcher = FUNCTION_ARG_PATTERN.matcher(content);
        
        while (functionMatcher.find()) {
            String argumentsStr = functionMatcher.group(1);
            
            // Split arguments by comma and check each one
            String[] arguments = argumentsStr.split(",");
            for (String arg : arguments) {
                arg = arg.trim();
                if (arg.isEmpty()) continue;
                
                Matcher argMatcher = ARG_PATTERN.matcher(arg);
                if (argMatcher.find()) {
                    String argName = argMatcher.group(1);
                    
                    if (BUILTIN_VARIABLES.contains(argName.toLowerCase())) {
                        continue;
                    }
                    
                    if (!isCamelCase(argName)) {
                        int line = getLineNumber(content, functionMatcher.start());
                        
                        violations.add(new LintingViolation(
                            getRuleId(),
                            String.format("Function argument '%s' should be camelCase", argName),
                            Severity.WARNING,
                            filePath,
                            line,
                            1
                        ));
                    }
                }
            }
        }
    }
    
    private boolean isCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // camelCase: starts with lowercase, no underscores, no spaces
        return name.matches("^[a-z][a-zA-Z0-9]*$");
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
