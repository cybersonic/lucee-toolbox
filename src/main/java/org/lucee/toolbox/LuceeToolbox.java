package org.lucee.toolbox;

import org.lucee.toolbox.cli.CommandLineInterface;
import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.engine.LintingEngine;
import org.lucee.toolbox.core.engine.FormattingEngine;
import org.lucee.toolbox.core.model.ToolboxResult;
import org.lucee.toolbox.output.OutputFormatFactory;
import org.lucee.toolbox.output.OutputFormatter;
import org.lucee.toolbox.repl.CFMLRepl;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Level;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the Lucee Toolbox - Advanced CFML Linter and Formatter
 * 
 * Features:
 * - Multiple parser support (BoxLang ANTLR, Lucee native, fallback regex)
 * - Comprehensive linting with standard and CFLint rules
 * - Advanced formatting based on cfformat
 * - Multiple output formats (Bitbucket JSON, HTML, CSV, console)
 * - Optimized for large codebases and long files
 * - VSCode integration ready
 * - Documentation integration with Lucee docs
 */
public class LuceeToolbox {
    
    private static final Logger logger = LoggerFactory.getLogger(LuceeToolbox.class);
    private static final String VERSION = "1.0.0";
    private static final String DEFAULT_CONFIG = "lucee-toolbox.json";
    
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        
        try {
            CommandLineInterface cli = new CommandLineInterface();
            Options options = cli.createOptions();
            
            CommandLineParser cliParser = new DefaultParser();
            CommandLine cmd = cliParser.parse(options, args);
            
            // Handle help and version
            if (cmd.hasOption("help")) {
                cli.printHelp(options);
                return;
            }
            
            if (cmd.hasOption("version")) {
                printVersion();
                return;
            }
            
            if (cmd.hasOption("list-formats")) {
                printAvailableFormats();
                return;
            }
            
            if (cmd.hasOption("show-config")) {
                showConfiguration(cmd);
                return;
            }
            
            // Check for REPL mode first
            String mode = cmd.getOptionValue("mode", "lint");
            if ("repl".equalsIgnoreCase(mode)) {
                CFMLRepl.main(new String[]{});
                return;
            }
            
            // Handle stdin input
            String inputPath = cmd.getOptionValue("input");
            if (inputPath == null) {
                // Try to read from stdin
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    
                    // Check if stdin has data by trying to read with a timeout
                    boolean hasData = reader.ready();
                    if (hasData) {
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append(System.lineSeparator());
                        }
                    }
                    
                    if (sb.length() > 0) {
                        Path tempInput = Files.createTempFile("lucee-stdin-", ".cfc");
                        Files.writeString(tempInput, sb.toString());
                        inputPath = tempInput.toString();
                        
                        // Schedule temp file for deletion on exit
                        tempInput.toFile().deleteOnExit();
                    }
                } catch (IOException e) {
                    // Stdin not available or error reading
                }
                
                // If still no input, show error
                if (inputPath == null) {
                    System.err.println("Error: Input file/directory required or pipe CFML code to stdin");
                    cli.printHelp(options);
                    System.exit(1);
                }
            }
            
            // Parse command line options first to get verbose/quiet flags
            String outputFormat = cmd.getOptionValue("format", "console");
            String outputFile = cmd.getOptionValue("output");
            boolean verbose = cmd.hasOption("verbose");
            boolean quiet = cmd.hasOption("quiet");
            String parser = cmd.getOptionValue("parser", "auto");
            boolean performanceMode = cmd.hasOption("performance");
            boolean ignoreViolations = cmd.hasOption("ignore-violations") || cmd.hasOption("no-exit-error");
            
            // Configure logging levels based on flags BEFORE loading config
            configureLogging(verbose, quiet);
            
            // Initialize configuration
            String configFile = cmd.getOptionValue("config", DEFAULT_CONFIG);
            ConfigurationManager configManager = new ConfigurationManager();
            
            // Try to load config from input directory first, then fall back to current directory
            String resolvedConfigFile = resolveConfigFile(configFile, inputPath);
            configManager.loadConfiguration(resolvedConfigFile);
            
            if (verbose && !quiet) {
                logger.info("Lucee Toolbox v{} starting...", VERSION);
                logger.info("Mode: {}, Parser: {}, Format: {}", mode, parser, outputFormat);
                logger.info("Input: {}", inputPath);
            }

            LuceeToolbox toolbox = new LuceeToolbox();
            ToolboxResult result = toolbox.execute(
                inputPath, mode, parser, outputFormat, outputFile, 
                configManager, verbose, quiet, performanceMode
            );
            
            // Output results
            OutputFormatter formatter = OutputFormatFactory.getFormatter(outputFormat);
            String formattedOutput = formatter.format(result);
            
            if (outputFile != null) {
                toolbox.writeToFile(formattedOutput, outputFile, formatter.getFileExtension());
                if (verbose && !quiet) {
                    logger.info("Output written to: {}", outputFile);
                }
            } else {
                System.out.println(formattedOutput);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            if (verbose && !quiet) {
                logger.info("Completed in {} ms", duration);
            }
            
            // Exit with appropriate code
            // Always exit with error for actual errors (parsing failures, etc.)
            // Only exit with error for violations if ignoreViolations is false
            if (result.hasErrors() || (!ignoreViolations && result.hasViolations())) {
                System.exit(1);
            }
            
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Execute the main toolbox functionality
     */
    public ToolboxResult execute(String inputPath, String mode, String parser, 
                               String outputFormat, String outputFile,
                               ConfigurationManager configManager, 
                               boolean verbose, boolean quiet, boolean performanceMode) 
                               throws IOException {
        
        Path input = Paths.get(inputPath);
        if (!input.toFile().exists()) {
            throw new IllegalArgumentException("Input path does not exist: " + inputPath);
        }
        
        ToolboxResult result = new ToolboxResult();
        
        // Determine if we're processing a single file
        boolean singleFileMode = Files.isRegularFile(input);
        
        switch (mode.toLowerCase()) {
            case "lint":
                return executeLinting(input, parser, configManager, verbose, quiet, performanceMode, singleFileMode);
            
            case "format":
                return executeFormatting(input, parser, configManager, verbose, quiet, performanceMode, singleFileMode);
            
            case "both":
                // Execute both linting and formatting
                ToolboxResult lintResult = executeLinting(input, parser, configManager, verbose, quiet, performanceMode, singleFileMode);
                ToolboxResult formatResult = executeFormatting(input, parser, configManager, verbose, quiet, performanceMode, singleFileMode);
                
                // Merge results
                result = lintResult;
                result.mergeWith(formatResult);
                return result;
            
            case "repl":
                // REPL mode is handled in main method, this shouldn't be reached
                throw new IllegalStateException("REPL mode should be handled in main method");
            
            default:
                throw new IllegalArgumentException("Invalid mode: " + mode + ". Use 'lint', 'format', 'both', or 'repl'");
        }
    }
    
    private ToolboxResult executeLinting(Path input, String parser, 
                                       ConfigurationManager configManager,
                                       boolean verbose, boolean quiet, boolean performanceMode, 
                                       boolean singleFileMode) 
                                       throws IOException {
        
        LintingEngine engine = new LintingEngine(configManager, parser, performanceMode);
        return engine.lint(input, verbose, quiet, singleFileMode);
    }
    
    private ToolboxResult executeFormatting(Path input, String parser,
                                          ConfigurationManager configManager,
                                          boolean verbose, boolean quiet, boolean performanceMode,
                                          boolean singleFileMode)
                                          throws IOException {
        
        FormattingEngine engine = new FormattingEngine(configManager, parser, performanceMode);
        return engine.format(input, verbose, quiet, singleFileMode);
    }
    
    private void writeToFile(String content, String outputFile, String defaultExtension) throws IOException {
        Path outputPath = Paths.get(outputFile);
        String fileName = outputPath.toString();
        
        // Add extension if not present
        if (!fileName.contains(".") && defaultExtension != null) {
            fileName += "." + defaultExtension;
            outputPath = Paths.get(fileName);
        }
        
        java.nio.file.Files.writeString(outputPath, content);
    }
    
    private static void printVersion() {
        System.out.println("Lucee Toolbox v" + VERSION);
        System.out.println("Advanced CFML Linter and Formatter");
        System.out.println("https://github.com/lucee/lucee-toolbox");
    }
    
    private static void printAvailableFormats() {
        System.out.println("Available Output Formats:");
        System.out.println("  console     - Console output with colors");
        System.out.println("  json        - JSON format");
        System.out.println("  bitbucket   - Bitbucket Pipelines JSON format");
        System.out.println("  html        - HTML report");
        System.out.println("  csv         - CSV format");
        System.out.println("  junit       - JUnit XML format");
        System.out.println("  sarif       - SARIF format for security analysis");
    }
    
    private static String resolveConfigFile(String configFile, String inputPath) {
        Path inputDir = Paths.get(inputPath).toAbsolutePath();
        
        // If inputPath is a file, get its parent directory
        if (Files.isRegularFile(inputDir)) {
            inputDir = inputDir.getParent();
        }
        
        Path configInInputDir = inputDir.resolve(configFile);
        if (Files.exists(configInInputDir)) {
            return configInInputDir.toString();
        }
        return configFile; // Fall back to current directory
    }
    
    private static void showConfiguration(CommandLine cmd) {
        try {
            String configFile = cmd.getOptionValue("config", DEFAULT_CONFIG);
            String inputPath = cmd.getOptionValue("input", ".");
            
            // Resolve config file path
            String resolvedConfigFile = resolveConfigFile(configFile, inputPath);
            
            // Initialize configuration manager
            ConfigurationManager configManager = new ConfigurationManager();
            
            String actualConfigPath = null;
            try {
                configManager.loadConfiguration(resolvedConfigFile);
                
                // Check if the config file actually exists
                Path configPath = Paths.get(resolvedConfigFile);
                if (Files.exists(configPath)) {
                    actualConfigPath = configPath.toAbsolutePath().toString();
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not load config file: " + e.getMessage());
                System.err.println("Using default configuration.");
            }
            
            // Show configuration summary
            System.out.println(configManager.getConfigurationSummary(actualConfigPath));
            
            // Show full configuration as JSON
            System.out.println("\nFull Configuration (JSON):");
            System.out.println("==========================\n");
            System.out.println(configManager.exportConfiguration());
            
        } catch (Exception e) {
            System.err.println("Error showing configuration: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Configure logging levels based on verbose and quiet flags
     */
    private static void configureLogging(boolean verbose, boolean quiet) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        ch.qos.logback.classic.Logger toolboxLogger = loggerContext.getLogger("org.lucee.toolbox");
        
        if (quiet) {
            // Quiet mode: suppress all logging to console except errors
            rootLogger.setLevel(Level.ERROR);
            toolboxLogger.setLevel(Level.ERROR);
        } else if (verbose) {
            // Verbose mode: show all logging including INFO and DEBUG
            rootLogger.setLevel(Level.INFO);
            toolboxLogger.setLevel(Level.DEBUG);
        } else {
            // Default mode: only show warnings and errors
            rootLogger.setLevel(Level.WARN);
            toolboxLogger.setLevel(Level.WARN);
        }
    }
}
