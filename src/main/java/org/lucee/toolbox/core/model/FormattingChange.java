package org.lucee.toolbox.core.model;

/**
 * Represents a formatting change made to CFML code
 */
public class FormattingChange {
    
    private final String filePath;
    private final int startLine;
    private final int endLine;
    private final String originalText;
    private final String formattedText;
    private final String changeType;
    private final String description;
    
    public FormattingChange(String filePath, int startLine, int endLine, 
                           String originalText, String formattedText, 
                           String changeType, String description) {
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.originalText = originalText;
        this.formattedText = formattedText;
        this.changeType = changeType;
        this.description = description;
    }
    
    // Getters
    public String getFilePath() {
        return filePath;
    }
    
    public int getStartLine() {
        return startLine;
    }
    
    public int getEndLine() {
        return endLine;
    }
    
    public String getOriginalText() {
        return originalText;
    }
    
    public String getFormattedText() {
        return formattedText;
    }
    
    public String getChangeType() {
        return changeType;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return String.format("%s:%d-%d: %s - %s", 
                filePath, startLine, endLine, changeType, description);
    }
}
