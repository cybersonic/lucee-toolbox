package org.lucee.toolbox.core.parser;

/**
 * Represents a comment found during parsing
 */
public class ParseComment {
    
    public enum CommentType {
        SINGLE_LINE,    // // comment
        MULTI_LINE,     // /* comment */
        JAVADOC,        // /** comment */
        HTML,           // <!-- comment -->
        CFML            // <!--- comment --->
    }
    
    private final String text;
    private final CommentType type;
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;
    private final int startOffset;
    private final int endOffset;
    private final String rawText;
    
    public ParseComment(String text, CommentType type, int startLine, int startColumn, 
                       int endLine, int endColumn, int startOffset, int endOffset, String rawText) {
        this.text = text;
        this.type = type;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.rawText = rawText;
    }
    
    public String getText() {
        return text;
    }
    
    public CommentType getType() {
        return type;
    }
    
    public int getStartLine() {
        return startLine;
    }
    
    public int getStartColumn() {
        return startColumn;
    }
    
    public int getEndLine() {
        return endLine;
    }
    
    public int getEndColumn() {
        return endColumn;
    }
    
    public int getStartOffset() {
        return startOffset;
    }
    
    public int getEndOffset() {
        return endOffset;
    }
    
    public String getRawText() {
        return rawText;
    }
    
    /**
     * Check if this is a documentation comment (JavaDoc or similar)
     */
    public boolean isDocumentation() {
        return type == CommentType.JAVADOC;
    }
    
    /**
     * Check if this comment spans multiple lines
     */
    public boolean isMultiLine() {
        return startLine != endLine;
    }
    
    @Override
    public String toString() {
        return String.format("%s comment at %d:%d-%d:%d: %s", 
                           type, startLine, startColumn, endLine, endColumn, text);
    }
}
