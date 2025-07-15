package org.lucee.toolbox.core.parser;

import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.parser.impl.RegexParser;
import org.lucee.toolbox.core.parser.impl.BoxLangParser;
import org.lucee.toolbox.core.parser.impl.LuceeScriptEngineParser;

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
     * @param parserType The type of parser (boxlang, lucee, lucee-script, regex)
     * @return CfmlParser instance or null if type not supported
     */
    public CfmlParser createParser(String parserType) {
        switch (parserType.toLowerCase()) {
            case "boxlang":
                return createBoxLangParser();
            case "lucee":
            case "lucee-script":
            case "lucee-scriptengine":
                return createLuceeScriptEngineParser();
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
     * Create Lucee ScriptEngine parser
     */
    private CfmlParser createLuceeScriptEngineParser() {
        return new LuceeScriptEngineParser();
    }
}
