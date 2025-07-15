package org.lucee.toolbox.core.parser.impl;

import org.lucee.toolbox.core.parser.CfmlParser;
import org.lucee.toolbox.core.parser.ParseResult;
import org.lucee.toolbox.core.parser.ParseException;
import org.lucee.toolbox.core.parser.EnhancedParseResult;
import org.lucee.toolbox.core.parser.ParseIssue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.ScriptContext;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * CFML parser using Lucee's JSR-223 ScriptEngine
 * This parser uses the actual Lucee engine for parsing and validation
 */
public class LuceeScriptEngineParser implements CfmlParser {
    
    private final ScriptEngine luceeEngine;
    private final boolean isEngineAvailable;
    
    public LuceeScriptEngineParser() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = null;
        
        // Try different engine names that Lucee might register under
        String[] engineNames = {"lucee", "cfml", "CFML", "coldfusion", "cfscript"};
        
        for (String name : engineNames) {
            engine = manager.getEngineByName(name);
            if (engine != null) {
                break;
            }
        }
        
        this.luceeEngine = engine;
        this.isEngineAvailable = engine != null;
        
        if (!isEngineAvailable) {
            System.err.println("Lucee ScriptEngine not available, falling back to syntax-only validation");
        }
    }
    
    @Override
    public ParseResult parse(String content, String filePath) throws ParseException {
        if (!isEngineAvailable) {
            // If Lucee engine is not available, do basic syntax checking
            return createBasicParseResult(content, filePath);
        }
        
        try {
            // Create enhanced parse result
            EnhancedParseResult result = new EnhancedParseResult(filePath, content, true, null, getParserType());
            
            // Use Lucee to validate the syntax
            List<ParseIssue> issues = validateSyntax(content);
            for (ParseIssue issue : issues) {
                result.addIssue(issue);
            }
            
            return result;
            
        } catch (Exception e) {
            throw new ParseException("Error parsing CFML with Lucee ScriptEngine: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate CFML syntax using Lucee ScriptEngine
     */
    private List<ParseIssue> validateSyntax(String cfmlCode) {
        List<ParseIssue> issues = new ArrayList<>();
        
        try {
            // Create isolated context for validation
            ScriptContext context = luceeEngine.getContext();
            StringWriter output = new StringWriter();
            StringWriter errorOutput = new StringWriter();
            
            context.setWriter(output);
            context.setErrorWriter(errorOutput);
            
            // Try to compile/validate the CFML code
            // We wrap in a try-catch to capture any compilation errors
            String validationCode = """
                try {
                    // Wrap user code in a function to isolate it
                    evalResult = evaluate("%s");
                    validationSuccess = true;
                } catch (any e) {
                    validationSuccess = false;
                    validationError = e.message;
                    validationDetail = e.detail ?: '';
                    validationLine = e.line ?: 1;
                    validationColumn = e.column ?: 1;
                }
                """.formatted(cfmlCode.replace("\"", "\\\""));
            
            luceeEngine.eval(validationCode);
            
            // Check validation results
            Boolean success = (Boolean) luceeEngine.get("validationSuccess");
            if (success == null || !success) {
                String errorMessage = (String) luceeEngine.get("validationError");
                String errorDetail = (String) luceeEngine.get("validationDetail");
                Integer line = (Integer) luceeEngine.get("validationLine");
                Integer column = (Integer) luceeEngine.get("validationColumn");
                
                ParseIssue issue = new ParseIssue(
                    errorMessage != null ? errorMessage : "Syntax error",
                    ParseIssue.Severity.ERROR,
                    line != null ? line : 1,
                    column != null ? column : 1,
                    -1, -1, errorDetail != null ? errorDetail : null
                );
                
                issues.add(issue);
            }
            
            // Check for any output in error stream
            String errors = errorOutput.toString();
            if (!errors.isEmpty()) {
                // Parse error output for additional issues
                String[] errorLines = errors.split("\n");
                for (String errorLine : errorLines) {
                    if (!errorLine.trim().isEmpty()) {
                        ParseIssue issue = new ParseIssue(
                            "Lucee warning: " + errorLine.trim(),
                            ParseIssue.Severity.WARNING,
                            1, 1, -1, -1, null
                        );
                        issues.add(issue);
                    }
                }
            }
            
        } catch (ScriptException e) {
            // Script execution error indicates syntax problem
            ParseIssue issue = new ParseIssue(
                "Syntax error: " + e.getMessage(),
                ParseIssue.Severity.ERROR,
                e.getLineNumber() > 0 ? e.getLineNumber() : 1,
                e.getColumnNumber() > 0 ? e.getColumnNumber() : 1,
                -1, -1, e.toString()
            );
            issues.add(issue);
        } catch (Exception e) {
            // Other errors
            ParseIssue issue = new ParseIssue(
                "Parser error: " + e.getMessage(),
                ParseIssue.Severity.ERROR,
                1, 1, -1, -1, e.toString()
            );
            issues.add(issue);
        }
        
        return issues;
    }
    
    /**
     * Create basic parse result when Lucee engine is not available
     */
    private ParseResult createBasicParseResult(String cfmlCode, String filePath) {
        EnhancedParseResult result = new EnhancedParseResult(filePath, cfmlCode, true, null, getParserType());
        
        List<ParseIssue> issues = new ArrayList<>();
        
        // Basic syntax checks without Lucee
        if (cfmlCode.trim().isEmpty()) {
            issues.add(new ParseIssue("Empty CFML content", ParseIssue.Severity.WARNING, 1, 1, -1, -1, null));
        }
        
        // Check for basic syntax patterns
        if (cfmlCode.contains("<%") && !cfmlCode.contains("%>")) {
            issues.add(new ParseIssue("Unclosed tag syntax", ParseIssue.Severity.ERROR, 1, 1, -1, -1, null));
        }
        
        if (cfmlCode.contains("<cf") && !cfmlCode.contains(">")) {
            issues.add(new ParseIssue("Unclosed CF tag", ParseIssue.Severity.ERROR, 1, 1, -1, -1, null));
        }
        
        // Check for unmatched braces
        int openBraces = 0;
        int closeBraces = 0;
        for (char c : cfmlCode.toCharArray()) {
            if (c == '{') openBraces++;
            if (c == '}') closeBraces++;
        }
        
        if (openBraces != closeBraces) {
            issues.add(new ParseIssue("Unmatched braces", ParseIssue.Severity.ERROR, 1, 1, -1, -1, null));
        }
        
        for (ParseIssue issue : issues) {
            result.addIssue(issue);
        }
        
        return result;
    }
    
    @Override
    public boolean canParse(String content) {
        return content != null && !content.trim().isEmpty();
    }
    
    @Override
    public String getParserType() {
        return "lucee-scriptengine";
    }
}
