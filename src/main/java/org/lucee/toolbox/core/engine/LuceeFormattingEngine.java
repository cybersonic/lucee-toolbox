package org.lucee.toolbox.core.engine;

import org.lucee.toolbox.core.model.FormattingChange;
import org.lucee.toolbox.core.model.ToolboxResult;
import org.lucee.toolbox.core.config.ConfigurationManager;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.ScriptContext;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Formatting engine using Lucee's JSR-223 ScriptEngine
 * This engine uses actual Lucee for CFML code formatting
 */
public class LuceeFormattingEngine {
    
    private final ScriptEngine luceeEngine;
    private final boolean isEngineAvailable;
    private final ConfigurationManager configManager;
    
    public LuceeFormattingEngine(ConfigurationManager configManager) {
        this.configManager = configManager;
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
            System.err.println("Lucee ScriptEngine not available for formatting");
        }
    }
    
    /**
     * Format CFML code using Lucee ScriptEngine
     */
    public String formatCode(String cfmlCode) {
        if (!isEngineAvailable) {
            // If Lucee engine is not available, return basic formatting
            return basicFormat(cfmlCode);
        }
        
        try {
            // Create isolated context for formatting
            ScriptContext context = luceeEngine.getContext();
            StringWriter output = new StringWriter();
            StringWriter errorOutput = new StringWriter();
            
            context.setWriter(output);
            context.setErrorWriter(errorOutput);
            
            // Set the code to be formatted
            luceeEngine.put("sourceCode", cfmlCode);
            
            // Get formatting configuration
            boolean useSpaces = configManager.getFormattingIndentationType().equals("spaces");
            int indentSize = configManager.getFormattingIndentationSize();
            int maxLineLength = configManager.getFormattingMaxLineLength();
            
            // Lucee-based formatting script
            String formattingScript = """
                // Formatting configuration
                useSpaces = %s;
                indentSize = %d;
                maxLineLength = %d;
                
                // Basic CFML formatting using Lucee string functions
                formattedCode = sourceCode;
                
                // Normalize line endings
                formattedCode = replace(formattedCode, chr(13) & chr(10), chr(10), "all");
                formattedCode = replace(formattedCode, chr(13), chr(10), "all");
                
                // Split into lines for processing
                lines = listToArray(formattedCode, chr(10));
                processedLines = [];
                indentLevel = 0;
                
                for (i = 1; i <= arrayLen(lines); i++) {
                    line = lines[i];
                    trimmedLine = trim(line);
                    
                    if (len(trimmedLine) == 0) {
                        // Empty line
                        arrayAppend(processedLines, "");
                        continue;
                    }
                    
                    // Adjust indent level before processing
                    if (reFind("^\\s*</", trimmedLine) || reFind("^\\s*}", trimmedLine)) {
                        indentLevel = max(0, indentLevel - 1);
                    }
                    
                    // Create indentation
                    indent = "";
                    if (useSpaces) {
                        indent = repeatString(" ", indentLevel * indentSize);
                    } else {
                        indent = repeatString(chr(9), indentLevel);
                    }
                    
                    // Apply indentation
                    processedLine = indent & trimmedLine;
                    
                    // Adjust indent level after processing
                    if (reFind("<[^/].*>\\s*$", trimmedLine) || reFind("\\{\\s*$", trimmedLine)) {
                        indentLevel++;
                    }
                    
                    arrayAppend(processedLines, processedLine);
                }
                
                // Join lines back together
                formattedResult = arrayToList(processedLines, chr(10));
                
                // Clean up extra blank lines
                formattedResult = reReplace(formattedResult, "\\n\\s*\\n\\s*\\n", chr(10) & chr(10), "all");
                """.formatted(
                useSpaces ? "true" : "false", 
                indentSize, 
                maxLineLength
            );
            
            luceeEngine.eval(formattingScript);
            
            // Get the formatted result
            String formattedCode = (String) luceeEngine.get("formattedResult");
            
            return formattedCode != null ? formattedCode : cfmlCode;
            
        } catch (ScriptException e) {
            System.err.println("Error formatting CFML with Lucee: " + e.getMessage());
            return basicFormat(cfmlCode);
        } catch (Exception e) {
            System.err.println("Unexpected error in Lucee formatting: " + e.getMessage());
            return basicFormat(cfmlCode);
        }
    }
    
    /**
     * Generate list of formatting changes
     */
    public List<FormattingChange> getFormattingChanges(String originalCode, String formattedCode) {
        List<FormattingChange> changes = new ArrayList<>();
        
        if (!isEngineAvailable) {
            return changes;
        }
        
        try {
            // Use Lucee to analyze differences
            luceeEngine.put("originalCode", originalCode);
            luceeEngine.put("formattedCode", formattedCode);
            
            String diffScript = """
                // Split both codes into lines
                originalLines = listToArray(originalCode, chr(10));
                formattedLines = listToArray(formattedCode, chr(10));
                
                // Simple diff analysis
                changes = [];
                maxLines = max(arrayLen(originalLines), arrayLen(formattedLines));
                
                for (i = 1; i <= maxLines; i++) {
                    origLine = (i <= arrayLen(originalLines)) ? originalLines[i] : "";
                    formLine = (i <= arrayLen(formattedLines)) ? formattedLines[i] : "";
                    
                    if (origLine != formLine) {
                        change = {
                            "lineNumber": i,
                            "type": "modification",
                            "original": origLine,
                            "formatted": formLine,
                            "description": "Line formatting changed"
                        };
                        arrayAppend(changes, change);
                    }
                }
                """;
            
            luceeEngine.eval(diffScript);
            
            // Note: In a real implementation, you would convert the Lucee array
            // to Java objects and create FormattingChange instances
            
        } catch (Exception e) {
            System.err.println("Error analyzing formatting changes: " + e.getMessage());
        }
        
        return changes;
    }
    
    /**
     * Basic formatting fallback when Lucee engine is not available
     */
    private String basicFormat(String cfmlCode) {
        // Basic indentation and cleanup
        String[] lines = cfmlCode.split("\\r?\\n");
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            if (trimmed.isEmpty()) {
                formatted.append("\n");
                continue;
            }
            
            // Adjust indent before processing
            if (trimmed.startsWith("</") || trimmed.startsWith("}")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }
            
            // Add indentation
            for (int i = 0; i < indentLevel; i++) {
                formatted.append("    ");
            }
            formatted.append(trimmed).append("\n");
            
            // Adjust indent after processing
            if (trimmed.matches("<[^/].*>\\s*$") || trimmed.endsWith("{")) {
                indentLevel++;
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * Check if Lucee engine is available
     */
    public boolean isAvailable() {
        return isEngineAvailable;
    }
    
    /**
     * Get the engine name
     */
    public String getEngineName() {
        return isEngineAvailable ? "Lucee ScriptEngine Formatter" : "Basic Formatter (fallback)";
    }
}
