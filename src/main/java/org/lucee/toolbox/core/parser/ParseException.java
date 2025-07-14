package org.lucee.toolbox.core.parser;

/**
 * Exception thrown when parsing fails
 */
public class ParseException extends Exception {
    
    private final String filePath;
    private final int line;
    private final int column;
    
    public ParseException(String message) {
        super(message);
        this.filePath = null;
        this.line = -1;
        this.column = -1;
    }
    
    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.filePath = null;
        this.line = -1;
        this.column = -1;
    }
    
    public ParseException(String message, String filePath, int line, int column) {
        super(message);
        this.filePath = filePath;
        this.line = line;
        this.column = column;
    }
    
    public ParseException(String message, String filePath, int line, int column, Throwable cause) {
        super(message, cause);
        this.filePath = filePath;
        this.line = line;
        this.column = column;
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
}
