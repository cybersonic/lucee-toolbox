package org.lucee.toolbox.repl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.ScriptContext;

/**
 * CFML REPL (Read-Eval-Print Loop) for the Lucee Toolbox
 * Uses the Lucee ScriptEngine for real CFML evaluation
 */
public class CFMLRepl {
    
    private static final String VERSION = "1.0.0";
    private Scanner scanner;
    private List<String> history;
    private boolean running;
    
    public CFMLRepl() {
        this.scanner = new Scanner(System.in);
        this.history = new ArrayList<>();
        this.running = true;
    }
    
    public static void main(String[] args) {
        CFMLRepl repl = new CFMLRepl();
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
        
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = null;
        
        // Try different engine names
        String[] engineNames = {"lucee", "cfml", "CFML", "coldfusion", "cfscript", "BoxLang", "BL", "BX"};
        
        for (String name : engineNames) {
            engine = manager.getEngineByName(name);
            if (engine != null) {
                System.out.println("Using ScriptEngine: " + name);
                break;
            }
        }
        
        if (engine == null) {
            System.err.println("Could not load any CFML ScriptEngine");
            System.err.println("Available engines:");
            for (ScriptEngineFactory factory : manager.getEngineFactories()) {
                System.err.println("  - " + factory.getEngineName() + " (" + String.join(", ", factory.getNames()) + ")");
            }
            return;
        }

        while (running) {
            System.out.print("cfml> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            history.add(input);
            
            if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                running = false;
                System.out.println("Goodbye!");
                break;
            }
            
            if ("help".equalsIgnoreCase(input)) {
                printHelp();
                continue;
            }
            
            if ("history".equalsIgnoreCase(input)) {
                printHistory();
                continue;
            }
            
            if ("clear".equalsIgnoreCase(input)) {
                clearScreen();
                continue;
            }
            
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter pw = new PrintWriter(baos);
                
                // Redirect Lucee output to ByteArrayOutputStream
                ScriptContext context = engine.getContext();
                context.setWriter(pw);
                context.setErrorWriter(pw);
                
                Object result = engine.eval(input, context);
                
                // Get output from the stream
                pw.flush();
                String output = baos.toString().trim();
                
                // Display output if any
                if (!output.isEmpty()) {
                    System.out.println(output);
                }
                
                // Display return value if any and no output was printed
                if (output.isEmpty() && result != null) {
                    System.out.println(result.toString());
                }
                
            } catch (ScriptException e) {
                System.err.println("CFML Error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("General Error: " + e.getMessage());
            }
        }
        
        scanner.close();
        
        // Ensure clean exit
        System.exit(0);
    }
    
    private void printWelcome() {
        System.out.println("===============================================");
        System.out.println("  Lucee Toolbox CFML REPL v" + VERSION);
        System.out.println("  Interactive CFML Evaluation Environment");
        System.out.println("===============================================");
        System.out.println("Type 'help' for available commands");
        System.out.println("Type 'exit' or 'quit' to leave");
        System.out.println("---");
    }
    
    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help       - Show this help message");
        System.out.println("  history    - Show command history");
        System.out.println("  clear      - Clear the screen");
        System.out.println("  exit/quit  - Exit the REPL");
        System.out.println("");
        System.out.println("CFML Examples:");
        System.out.println("  writeOutput('Hello, World!')");
        System.out.println("  name = 'Lucee'");
        System.out.println("  now()");
        System.out.println("  arrayLen([1,2,3])");
        System.out.println("  structKeyExists({a:1}, 'a')");
        System.out.println("  createObject('java', 'java.lang.System').currentTimeMillis()");
        System.out.println("");
        System.out.println("Note: Using real Lucee ScriptEngine for CFML evaluation");
        System.out.println("Variables persist between evaluations in this session");
    }
    
    private void printHistory() {
        System.out.println("Command History:");
        for (int i = 0; i < history.size(); i++) {
            System.out.println(String.format("%3d: %s", i + 1, history.get(i)));
        }
    }
    
    private void clearScreen() {
        // Clear screen for Unix/Linux/Mac
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

