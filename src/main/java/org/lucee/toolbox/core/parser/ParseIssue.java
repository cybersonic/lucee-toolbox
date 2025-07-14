package org.lucee.toolbox.core.parser;

/**
 * Represents a parsing issue (error, warning, or info)
 */
public class ParseIssue {
    
    public enum Severity {
        ERROR, WARNING, INFO
    }
    
    private final String message;
    private final Severity severity;
    private final int line;
    private final int column;
    private final int startOffset;
    private final int endOffset;
    private final String rule;
    
    public ParseIssue(String message, Severity severity, int line, int column) {
        this(message, severity, line, column, -1, -1, null);
    }
    
    public ParseIssue(String message, Severity severity, int line, int column, 
                     int startOffset, int endOffset, String rule) {
        this.message = message;
        this.severity = severity;
        this.line = line;
        this.column = column;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.rule = rule;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
    
    public int getStartOffset() {
        return startOffset;
    }
    
    public int getEndOffset() {
        return endOffset;
    }
    
    public String getRule() {
        return rule;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s at %d:%d", severity, message, line, column);
    }
    
    /**
     * Create an error issue
     */
    public static ParseIssue error(String message, int line, int column) {
        return new ParseIssue(message, Severity.ERROR, line, column);
    }
    
    /**
     * Create a warning issue
     */
    public static ParseIssue warning(String message, int line, int column) {
        return new ParseIssue(message, Severity.WARNING, line, column);
    }
    
    /**
     * Create an info issue
     */
    public static ParseIssue info(String message, int line, int column) {
        return new ParseIssue(message, Severity.INFO, line, column);
    }
}
