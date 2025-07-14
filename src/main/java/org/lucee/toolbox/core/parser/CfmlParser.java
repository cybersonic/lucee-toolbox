package org.lucee.toolbox.core.parser;

/**
 * Interface for CFML parsers
 */
public interface CfmlParser {
    
    /**
     * Parse CFML content and return parse result
     * @param content The CFML content to parse
     * @param filePath The file path for context
     * @return ParseResult containing the parsed information
     * @throws ParseException if parsing fails
     */
    ParseResult parse(String content, String filePath) throws ParseException;
    
    /**
     * Get the parser type identifier
     * @return Parser type string
     */
    String getParserType();
    
    /**
     * Check if this parser can handle the given content
     * @param content Content to check
     * @return true if parser can handle content
     */
    boolean canParse(String content);
}
