package org.lucee.toolbox.core.parser.impl;

import org.lucee.toolbox.core.parser.CfmlParser;
import org.lucee.toolbox.core.parser.ParseException;
import org.lucee.toolbox.core.parser.ParseResult;

/**
 * Simple regex-based fallback parser for CFML
 */
public class RegexParser implements CfmlParser {
    
    @Override
    public ParseResult parse(String content, String filePath) throws ParseException {
        try {
            // Basic validation - just check if content is not null/empty
            if (content == null) {
                throw new ParseException("Content cannot be null", filePath, 1, 1);
            }
            
            // For now, just return a successful parse result
            // TODO: Implement actual regex-based parsing
            ParseResult result = new ParseResult(filePath, content, true);
            
            return result;
            
        } catch (Exception e) {
            throw new ParseException("Regex parsing failed: " + e.getMessage(), filePath, 1, 1, e);
        }
    }
    
    @Override
    public String getParserType() {
        return "regex";
    }
    
    @Override
    public boolean canParse(String content) {
        // This fallback parser can handle any content
        return content != null;
    }
}
