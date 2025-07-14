package org.lucee.toolbox.repl;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.List;

public class ScriptEngineTest {
    
    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        
        System.out.println("Available ScriptEngines:");
        System.out.println("========================");
        
        for (ScriptEngineFactory factory : factories) {
            System.out.println("Engine Name: " + factory.getEngineName());
            System.out.println("Engine Version: " + factory.getEngineVersion());
            System.out.println("Language Name: " + factory.getLanguageName());
            System.out.println("Language Version: " + factory.getLanguageVersion());
            System.out.println("Extensions: " + factory.getExtensions());
            System.out.println("MIME Types: " + factory.getMimeTypes());
            System.out.println("Names: " + factory.getNames());
            System.out.println("---");
        }
        
        // Test specific engine names
        String[] testNames = {"lucee", "cfml", "CFML", "coldfusion", "cfscript"};
        for (String name : testNames) {
            ScriptEngine engine = manager.getEngineByName(name);
            System.out.println("Engine '" + name + "': " + (engine != null ? "Available" : "Not available"));
        }
    }
}
