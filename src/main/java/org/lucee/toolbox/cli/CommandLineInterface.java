package org.lucee.toolbox.cli;

import org.apache.commons.cli.*;

/**
 * Command Line Interface for Lucee Toolbox
 * Provides comprehensive CLI options for linting and formatting CFML code
 */
public class CommandLineInterface {
    
    public Options createOptions() {
        Options options = new Options();
        
        // Main operation options
        options.addOption(Option.builder("i")
                .longOpt("input")
                .hasArg()
                .argName("PATH")
                .desc("Input file or directory to process (required)")
                .required(false)
                .build());
        
        options.addOption(Option.builder("m")
                .longOpt("mode")
                .hasArg()
                .argName("MODE")
                .desc("Processing mode: lint, format, both, or repl (default: lint)")
                .build());
        
        options.addOption(Option.builder("p")
                .longOpt("parser")
                .hasArg()
                .argName("PARSER")
                .desc("Parser to use: auto, boxlang, lucee, regex (default: auto)")
                .build());
        
        // Configuration options
        options.addOption(Option.builder("c")
                .longOpt("config")
                .hasArg()
                .argName("FILE")
                .desc("Configuration file path (default: lucee-toolbox.json)")
                .build());
        
        // Output options
        options.addOption(Option.builder("f")
                .longOpt("format")
                .hasArg()
                .argName("FORMAT")
                .desc("Output format: console, json, bitbucket, html, csv, junit, sarif (default: console)")
                .build());
        
        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .argName("FILE")
                .desc("Output file path (prints to stdout if not specified)")
                .build());
        
        // Control options
        options.addOption(Option.builder("v")
                .longOpt("verbose")
                .desc("Enable verbose output")
                .build());
        
        options.addOption(Option.builder("q")
                .longOpt("quiet")
                .desc("Suppress all output except errors")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("performance")
                .desc("Enable performance optimizations for large codebases")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("ignore-violations")
                .desc("Do not exit with error code when violations are found (useful for IDE integration)")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("no-exit-error")
                .desc("Alias for --ignore-violations")
                .build());
        
        // Filtering options
        options.addOption(Option.builder()
                .longOpt("include")
                .hasArg()
                .argName("PATTERN")
                .desc("Include files matching pattern (can be specified multiple times)")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("exclude")
                .hasArg()
                .argName("PATTERN")
                .desc("Exclude files matching pattern (can be specified multiple times)")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("severity")
                .hasArg()
                .argName("LEVEL")
                .desc("Minimum severity level: error, warning, info (default: warning)")
                .build());
        
        // Rule management options
        options.addOption(Option.builder()
                .longOpt("rules")
                .hasArg()
                .argName("RULES")
                .desc("Comma-separated list of rules to enable/disable (e.g., +rule1,-rule2)")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("rule-set")
                .hasArg()
                .argName("SET")
                .desc("Predefined rule set: standard, cflint, minimal, strict (default: standard)")
                .build());
        
        // Formatting specific options
        options.addOption(Option.builder()
                .longOpt("dry-run")
                .desc("Show what would be formatted without making changes")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("diff")
                .desc("Show differences that would be made")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("backup")
                .desc("Create backup files before formatting")
                .build());
        
        // Performance and caching options
        options.addOption(Option.builder()
                .longOpt("no-cache")
                .desc("Disable caching")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("clear-cache")
                .desc("Clear cache and exit")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("max-threads")
                .hasArg()
                .argName("NUMBER")
                .desc("Maximum number of threads for parallel processing (default: auto)")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("timeout")
                .hasArg()
                .argName("SECONDS")
                .desc("Timeout for processing individual files (default: 30)")
                .build());
        
        // Documentation integration
        options.addOption(Option.builder()
                .longOpt("docs")
                .desc("Enable documentation integration")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("offline")
                .desc("Run in offline mode (no documentation lookup)")
                .build());
        
        // Information options
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Show this help message")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("version")
                .desc("Show version information")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("list-formats")
                .desc("List available output formats")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("list-rules")
                .desc("List available linting rules")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("list-parsers")
                .desc("List available parsers")
                .build());
        
        options.addOption(Option.builder()
                .longOpt("show-config")
                .desc("Show resolved configuration and exit")
                .build());
        
        return options;
    }
    
    public void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        
        String header = "\nLucee Toolbox v1.0.0 - Advanced CFML Linter and Formatter\n" +
                       "Comprehensive linting and formatting for Lucee CFML projects\n\n";
        
        String footer = "\nExamples:\n" +
                       "  lucee-toolbox -i src/                    # Lint all CFML files in src/\n" +
                       "  lucee-toolbox -i file.cfc -m format     # Format a single file\n" +
                       "  lucee-toolbox -i src/ -f bitbucket      # Generate Bitbucket report\n" +
                       "  lucee-toolbox -i src/ -m both --dry-run # Show what would be changed\n" +
                       "  lucee-toolbox -i large-app/ --performance # Optimize for large codebase\n" +
                       "  lucee-toolbox -i src/ --ignore-violations # Don't exit with error for violations (IDE friendly)\n" +
                       "  lucee-toolbox -i src/ --no-exit-error   # Same as --ignore-violations\n" +
                       "  lucee-toolbox -i src/ --show-config     # Show resolved configuration\n\n" +
                       "For more information: https://github.com/lucee/lucee-toolbox\n";
        
        formatter.printHelp("lucee-toolbox", header, options, footer, true);
    }
    
    public void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(new java.io.PrintWriter(System.out, true), 80, "lucee-toolbox", options);
    }
}
