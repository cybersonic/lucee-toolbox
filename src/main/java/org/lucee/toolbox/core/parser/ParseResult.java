package org.lucee.toolbox.core.parser;

import java.util.List;
import java.util.ArrayList;

/**
 * Result from parsing CFML content
 */
public class ParseResult {
    
    private final String filePath;
    private final String content;
    private final boolean parseSuccessful;
    private final List<String> parseErrors;
    private final Object syntaxTree; // Will be implementation-specific
    
    public ParseResult(String filePath, String content, boolean parseSuccessful) {
        this.filePath = filePath;
        this.content = content;
        this.parseSuccessful = parseSuccessful;
        this.parseErrors = new ArrayList<>();
        this.syntaxTree = null;
    }
    
    public ParseResult(String filePath, String content, boolean parseSuccessful, Object syntaxTree) {
        this.filePath = filePath;
        this.content = content;
        this.parseSuccessful = parseSuccessful;
        this.parseErrors = new ArrayList<>();
        this.syntaxTree = syntaxTree;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public String getContent() {
        return content;
    }
    
    public boolean isParseSuccessful() {
        return parseSuccessful;
    }
    
    public List<String> getParseErrors() {
        return new ArrayList<>(parseErrors);
    }
    
    public void addParseError(String error) {
        this.parseErrors.add(error);
    }
    
    public Object getSyntaxTree() {
        return syntaxTree;
    }
    
    public String[] getLines() {
        return content.split("\r?\n");
    }
    
    public int getLineCount() {
        return getLines().length;
    }
}
