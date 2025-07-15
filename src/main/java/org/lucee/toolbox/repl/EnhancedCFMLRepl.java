package org.lucee.toolbox.repl;

import org.lucee.toolbox.core.parser.impl.LuceeScriptEngineParser;
import org.lucee.toolbox.core.engine.LuceeFormattingEngine;
import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.parser.ParseResult;
import org.lucee.toolbox.core.parser.EnhancedParseResult;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.ScriptContext;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Enhanced CFML REPL with Lucee ScriptEngine integration
 * Features:
 * - Real Lucee ScriptEngine execution
 * - Syntax validation using Lucee parser
 * - Code formatting using Lucee formatter
 * - Multiple CFML engine support
 */
public class EnhancedCFMLRepl {
    
    private static final String VERSION = "1.0.0";
    private Scanner scanner;
    private List<String> history;
    private boolean running;
    private ScriptEngine luceeEngine;
    private LuceeScriptEngineParser luceeParser;
    private LuceeFormattingEngine luceeFormatter;
    private ConfigurationManager configManager;
    
    public EnhancedCFMLRepl() {
        this.scanner = new Scanner(System.in);
        this.history = new ArrayList<>();
        this.running = true;
        this.configManager = new ConfigurationManager();
        
        // Initialize Lucee components
        initializeLuceeComponents();
    }
    
    private void initializeLuceeComponents() {
        // Initialize ScriptEngine
        ScriptEngineManager manager = new ScriptEngineManager();
        String[] engineNames = {"lucee", "cfml", "CFML", "coldfusion", "cfscript", "BoxLang", "BL", "BX"};
        
        for (String name : engineNames) {
            luceeEngine = manager.getEngineByName(name);
            if (luceeEngine != null) {
                System.out.println("✅ Found CFML ScriptEngine: " + name);
                break;
            }
        }
        
        // Initialize parser and formatter
        luceeParser = new LuceeScriptEngineParser();
        luceeFormatter = new LuceeFormattingEngine(configManager);
        
        if (luceeEngine == null) {
            System.out.println("⚠️  No CFML ScriptEngine found, using fallback mode");
        }
    }
    
    public static void main(String[] args) {
        EnhancedCFMLRepl repl = new EnhancedCFMLRepl();
        repl.start();
    }
    
    public void start() {
        printWelcome();
        
        // Add shutdown hook for clean exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            if (scanner != null) {
                scanner.close();
            }
        }));
        
        while (running) {
            System.out.print("cfml> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            history.add(input);
            
            // Handle commands
            if (handleCommand(input)) {
                continue;
            }
            
            // Process CFML code
            processCFMLCode(input);
        }
        
        scanner.close();
        System.exit(0);
    }
    
    private boolean handleCommand(String input) {
        switch (input.toLowerCase()) {
            case "exit":
            case "quit":
                running = false;
                System.out.println("Goodbye!");
                return true;
                
            case "help":
                printHelp();
                return true;
                
            case "history":
                printHistory();
                return true;
                
            case "clear":
                clearScreen();
                return true;
                
            case "engines":
                listAvailableEngines();
                return true;
                
            case "status":
                printStatus();
                return true;
                
            default:
                // Check if it's a format command
                if (input.toLowerCase().startsWith("format ")) {
                    String code = input.substring(7).trim();
                    formatCode(code);
                    return true;
                }
                
                // Check if it's a validate command
                if (input.toLowerCase().startsWith("validate ")) {
                    String code = input.substring(9).trim();
                    validateCode(code);
                    return true;
                }
                
                return false;
        }
    }
    
    private void processCFMLCode(String cfmlCode) {
        try {
            // First, validate the syntax
            if (luceeParser.canParse(cfmlCode)) {
                try {
                    ParseResult parseResult = luceeParser.parse(cfmlCode, "<repl>");
                    if (parseResult instanceof EnhancedParseResult) {
                        EnhancedParseResult epr = (EnhancedParseResult) parseResult;
                        if (epr.hasErrors()) {
                            System.out.println("⚠️  Syntax validation warnings:");
                            epr.getIssues().forEach(issue -> 
                                System.out.printf("   %s: %s (line %d, col %d)%n", 
                                    issue.getSeverity(), issue.getMessage(), 
                                    issue.getLine(), issue.getColumn())
                            );
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️  Syntax validation error: " + e.getMessage());
                }
            }
            
            // Execute with Lucee if available
            if (luceeEngine != null) {
                executeLuceeCode(cfmlCode);
            } else {
                System.out.println("❌ No CFML engine available for execution");
            }
            
        } catch (Exception e) {
            System.err.println("Error processing CFML code: " + e.getMessage());
        }
    }
    
    private void executeLuceeCode(String cfmlCode) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);
            
            // Redirect Lucee output
            ScriptContext context = luceeEngine.getContext();
            context.setWriter(pw);
            context.setErrorWriter(pw);
            
            // Execute the code
            Object result = luceeEngine.eval(cfmlCode, context);
            
            // Get output
            pw.flush();
            String output = baos.toString().trim();
            
            // Display output
            if (!output.isEmpty()) {
                System.out.println(output);
            }
            
            // Display return value if any and no output was printed
            if (output.isEmpty() && result != null) {
                System.out.println("→ " + result.toString());
            }
            
        } catch (ScriptException e) {
            System.err.println("CFML Error: " + e.getMessage());
            if (e.getLineNumber() > 0) {
                System.err.println("  at line " + e.getLineNumber());
            }
        } catch (Exception e) {
            System.err.println("Execution Error: " + e.getMessage());
        }
    }
    
    private void formatCode(String cfmlCode) {
        if (luceeFormatter.isAvailable()) {
            try {
                String formatted = luceeFormatter.formatCode(cfmlCode);
                System.out.println("Formatted code:");
                System.out.println("================");
                System.out.println(formatted);
                System.out.println("================");
            } catch (Exception e) {
                System.err.println("Formatting error: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Lucee formatter not available");
        }
    }
    
    private void validateCode(String cfmlCode) {
        if (luceeParser.canParse(cfmlCode)) {
            try {
                ParseResult parseResult = luceeParser.parse(cfmlCode, "<validation>");
                if (parseResult instanceof EnhancedParseResult) {
                    EnhancedParseResult epr = (EnhancedParseResult) parseResult;
                    if (!epr.hasErrors()) {
                        System.out.println("✅ Code validation passed");
                    } else {
                        System.out.println("❌ Code validation failed:");
                        epr.getIssues().forEach(issue -> 
                            System.out.printf("   %s: %s (line %d, col %d)%n", 
                                issue.getSeverity(), issue.getMessage(), 
                                issue.getLine(), issue.getColumn())
                        );
                    }
                } else {
                    System.out.println("✅ Code validation passed");
                }
            } catch (Exception e) {
                System.err.println("Validation error: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Lucee parser not available");
        }
    }
    
    private void listAvailableEngines() {
        System.out.println("Available Script Engines:");
        System.out.println("========================");
        
        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        
        for (ScriptEngineFactory factory : factories) {
            System.out.printf("Engine: %s v%s%n", factory.getEngineName(), factory.getEngineVersion());
            System.out.printf("  Language: %s v%s%n", factory.getLanguageName(), factory.getLanguageVersion());
            System.out.printf("  Names: %s%n", String.join(", ", factory.getNames()));
            System.out.printf("  Extensions: %s%n", String.join(", ", factory.getExtensions()));
            System.out.printf("  MIME Types: %s%n", String.join(", ", factory.getMimeTypes()));
            System.out.println();
        }
    }
    
    private void printStatus() {
        System.out.println("Enhanced CFML REPL Status:");
        System.out.println("=========================");
        System.out.printf("ScriptEngine: %s%n", luceeEngine != null ? "✅ Available" : "❌ Not available");
        System.out.printf("Parser: %s (%s)%n", "Lucee ScriptEngine Parser", luceeParser.getParserType());
        System.out.printf("Formatter: %s%n", luceeFormatter.getEngineName());
        System.out.printf("Commands executed: %d%n", history.size());
        System.out.println();
    }
    
    private void printWelcome() {
        System.out.println("===============================================");
        System.out.println("  Enhanced Lucee Toolbox CFML REPL v" + VERSION);
        System.out.println("  Real Lucee ScriptEngine Integration");
        System.out.println("===============================================");
        System.out.println("Type 'help' for available commands");
        System.out.println("Type 'exit' or 'quit' to leave");
        System.out.println();
    }
    
    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("==================");
        System.out.println("  help           - Show this help message");
        System.out.println("  history        - Show command history");
        System.out.println("  clear          - Clear the screen");
        System.out.println("  engines        - List available script engines");
        System.out.println("  status         - Show REPL status");
        System.out.println("  format <code>  - Format CFML code");
        System.out.println("  validate <code> - Validate CFML code");
        System.out.println("  exit/quit      - Exit the REPL");
        System.out.println();
        System.out.println("CFML Examples:");
        System.out.println("  writeOutput('Hello, World!')");
        System.out.println("  name = 'Lucee'; writeOutput('Hello, ' & name & '!')");
        System.out.println("  now()");
        System.out.println("  arrayLen([1,2,3])");
        System.out.println("  structKeyExists({a:1}, 'a')");
        System.out.println("  createObject('java', 'java.lang.System').currentTimeMillis()");
        System.out.println();
        System.out.println("Advanced Examples:");
        System.out.println("  format writeOutput('Hello World')");
        System.out.println("  validate if(true) writeOutput('valid')");
        System.out.println();
    }
    
    private void printHistory() {
        System.out.println("Command History:");
        System.out.println("===============");
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("%3d: %s%n", i + 1, history.get(i));
        }
        System.out.println();
    }
    
    private void clearScreen() {
        try {
            new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (Exception e) {
            // For Windows or if clear command fails
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
}
