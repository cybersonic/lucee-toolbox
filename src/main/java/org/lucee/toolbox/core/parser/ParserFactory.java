package org.lucee.toolbox.core.parser;

import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.parser.impl.RegexParser;
import org.lucee.toolbox.core.parser.impl.BoxLangParser;

/**
 * Factory for creating CFML parsers
 */
public class ParserFactory {
    
    private final ConfigurationManager configManager;
    
    public ParserFactory(ConfigurationManager configManager) {
        this.configManager = configManager;
    }
    
    /**
     * Create a parser of the specified type
     * @param parserType The type of parser (boxlang, lucee, regex)
     * @return CfmlParser instance or null if type not supported
     */
    public CfmlParser createParser(String parserType) {
        switch (parserType.toLowerCase()) {
            case "boxlang":
                return createBoxLangParser();
            case "lucee":
                return createLuceeParser();
            case "regex":
                return new RegexParser();
            default:
                return null;
        }
    }
    
    /**
     * Create BoxLang ANTLR parser
     */
    private CfmlParser createBoxLangParser() {
        return new BoxLangParser();
    }
    
    /**
     * Create Lucee native parser (placeholder)
     */
    private CfmlParser createLuceeParser() {
        // For now, fall back to regex parser
        // Log that we're falling back to regex parser
        System.out.println("Lucee native parser not yet implemented, falling back to RegexParser");
        // TODO: Implement Lucee native parser integration
        return new RegexParser();
    }
}
