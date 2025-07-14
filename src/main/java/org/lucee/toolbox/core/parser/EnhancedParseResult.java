package org.lucee.toolbox.core.parser;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Enhanced ParseResult with better AST support and error reporting
 */
public class EnhancedParseResult extends ParseResult {
    
    private final List<ParseIssue> issues;
    private final List<ParseComment> comments;
    private final String parserType;
    
    public EnhancedParseResult(String filePath, String content, boolean parseSuccessful, 
                              Object syntaxTree, String parserType) {
        super(filePath, content, parseSuccessful, syntaxTree);
        this.issues = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.parserType = parserType;
    }
    
    public void addIssue(ParseIssue issue) {
        this.issues.add(issue);
    }
    
    public void addComment(ParseComment comment) {
        this.comments.add(comment);
    }
    
    public List<ParseIssue> getIssues() {
        return Collections.unmodifiableList(issues);
    }
    
    public List<ParseComment> getComments() {
        return Collections.unmodifiableList(comments);
    }
    
    public String getParserType() {
        return parserType;
    }
    
    /**
     * Get all issues of a specific severity
     */
    public List<ParseIssue> getIssuesBySeverity(ParseIssue.Severity severity) {
        return issues.stream()
                .filter(issue -> issue.getSeverity() == severity)
                .toList();
    }
    
    /**
     * Check if there are any errors (not just warnings)
     */
    public boolean hasErrors() {
        return issues.stream().anyMatch(issue -> issue.getSeverity() == ParseIssue.Severity.ERROR);
    }
    
    /**
     * Get line and column information for an offset
     */
    public LineColumn getLineColumn(int offset) {
        String[] lines = getContent().split("\r?\n");
        int currentOffset = 0;
        
        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            int lineLength = lines[lineNum].length() + 1; // +1 for newline
            if (currentOffset + lineLength > offset) {
                return new LineColumn(lineNum + 1, offset - currentOffset + 1);
            }
            currentOffset += lineLength;
        }
        
        return new LineColumn(lines.length, lines[lines.length - 1].length());
    }
    
    public static class LineColumn {
        private final int line;
        private final int column;
        
        public LineColumn(int line, int column) {
            this.line = line;
            this.column = column;
        }
        
        public int getLine() { return line; }
        public int getColumn() { return column; }
        
        @Override
        public String toString() {
            return String.format("%d:%d", line, column);
        }
    }
}
