package org.lucee.toolbox.core.model;

/**
 * Represents a linting violation found in CFML code
 */
public class LintingViolation {
    
    private final String ruleId;
    private final String message;
    private final Severity severity;
    private final String filePath;
    private final int line;
    private final int column;
    private final int endLine;
    private final int endColumn;
    private final String ruleCategory;
    private final String codeSnippet;
    private final String suggestedFix;
    
    public LintingViolation(String ruleId, String message, Severity severity, 
                           String filePath, int line, int column) {
        this(ruleId, message, severity, filePath, line, column, line, column + 1, null, null, null);
    }
    
    public LintingViolation(String ruleId, String message, Severity severity,
                           String filePath, int line, int column, 
                           String ruleCategory, String codeSnippet, String suggestedFix) {
        this(ruleId, message, severity, filePath, line, column, line, column + 1, ruleCategory, codeSnippet, suggestedFix);
    }
    
    public LintingViolation(String ruleId, String message, Severity severity,
                           String filePath, int line, int column, int endLine, int endColumn,
                           String ruleCategory, String codeSnippet, String suggestedFix) {
        this.ruleId = ruleId;
        this.message = message;
        this.severity = severity;
        this.filePath = filePath;
        this.line = line;
        this.column = column;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.ruleCategory = ruleCategory;
        this.codeSnippet = codeSnippet;
        this.suggestedFix = suggestedFix;
    }
    
    // Getters
    public String getRuleId() {
        return ruleId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
    
    public int getEndLine() {
        return endLine;
    }
    
    public int getEndColumn() {
        return endColumn;
    }
    
    public String getRuleCategory() {
        return ruleCategory;
    }
    
    public String getCodeSnippet() {
        return codeSnippet;
    }
    
    public String getSuggestedFix() {
        return suggestedFix;
    }
    
    @Override
    public String toString() {
        return String.format("%s:%d:%d: %s [%s] %s", 
                filePath, line, column, severity.getName(), ruleId, message);
    }
}
