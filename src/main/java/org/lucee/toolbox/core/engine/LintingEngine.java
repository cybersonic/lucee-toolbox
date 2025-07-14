package org.lucee.toolbox.core.engine;

import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.Severity;
import org.lucee.toolbox.core.model.ToolboxResult;
import org.lucee.toolbox.core.parser.CfmlParser;
import org.lucee.toolbox.core.parser.ParserFactory;
import org.lucee.toolbox.core.rules.LintingRuleEngine;
import org.lucee.toolbox.core.util.EncodingDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * Main linting engine for analyzing CFML code and finding violations
 */
public class LintingEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(LintingEngine.class);
    
    private final ConfigurationManager configManager;
    private final String parserType;
    private final boolean performanceMode;
    private final LintingRuleEngine ruleEngine;
    private final ParserFactory parserFactory;
    private ExecutorService executorService;
    
    public LintingEngine(ConfigurationManager configManager, String parserType, boolean performanceMode) {
        this.configManager = configManager;
        this.parserType = parserType;
        this.performanceMode = performanceMode;
        this.ruleEngine = new LintingRuleEngine(configManager);
        this.parserFactory = new ParserFactory(configManager);
        
        if (performanceMode && configManager.isParallelProcessingEnabled()) {
            this.executorService = Executors.newFixedThreadPool(configManager.getMaxThreads());
        }
    }
    
    /**
     * Lint the given path (file or directory)
     */
    public ToolboxResult lint(Path inputPath, boolean verbose, boolean quiet) throws IOException {
        return lint(inputPath, verbose, quiet, false);
    }
    
    /**
     * Lint the given path (file or directory) with single file mode support
     */
    public ToolboxResult lint(Path inputPath, boolean verbose, boolean quiet, boolean singleFileMode) throws IOException {
        long startTime = System.currentTimeMillis();
        ToolboxResult result = new ToolboxResult();
        
        if (!quiet && verbose) {
            logger.info("Starting linting analysis on: {}", inputPath);
        }
        
        try {
            if (Files.isDirectory(inputPath)) {
                lintDirectory(inputPath, result, verbose, quiet, singleFileMode);
            } else {
                lintFile(inputPath, result, verbose, quiet, singleFileMode);
            }
        } catch (Exception e) {
            logger.error("Error during linting: {}", e.getMessage(), e);
            result.addError("Linting failed: " + e.getMessage());
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        result.getStats().setExecutionTime(duration);
        
        
        if (!quiet && verbose) {
            logger.info("Linting completed in {} ms. Found {} violations", 
                    duration, result.getViolations().size());
        }
        
        return result;
    }
    
    /**
     * Lint all CFML files in a directory
     */
    private void lintDirectory(Path directory, ToolboxResult result, boolean verbose, boolean quiet, boolean singleFileMode) throws IOException {
        List<String> includePatterns = configManager.getIncludePatterns();
        List<String> excludePatterns = configManager.getExcludePatterns();
        Path configBaseDir = configManager.getConfigBaseDirectory();
        
        try (Stream<Path> files = Files.walk(directory)) {
            List<Path> cfmlFiles = files
                    .filter(Files::isRegularFile)
                    .filter(path -> matchesIncludePatterns(path, includePatterns, configBaseDir))
                    .filter(path -> !matchesExcludePatterns(path, excludePatterns, configBaseDir))
                    .collect(Collectors.toList());
            
            if (!quiet && verbose) {
                logger.info("Found {} CFML files to analyze", cfmlFiles.size());
            }
            
            // Note: singleFileMode is always false for directory processing
            if (performanceMode && executorService != null && cfmlFiles.size() > 1) {
                lintFilesInParallel(cfmlFiles, result, verbose, quiet, false);
            } else {
                lintFilesSequentially(cfmlFiles, result, verbose, quiet, false);
            }
        }
    }
    
    /**
     * Lint files in parallel for better performance
     */
    private void lintFilesInParallel(List<Path> files, ToolboxResult result, boolean verbose, boolean quiet, boolean singleFileMode) {
        List<CompletableFuture<ToolboxResult>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    try {
                        ToolboxResult fileResult = new ToolboxResult();
                        lintSingleFile(file, fileResult, verbose, quiet, singleFileMode);
                        return fileResult;
                    } catch (Exception e) {
                        logger.error("Error linting file {}: {}", file, e.getMessage());
                        ToolboxResult errorResult = new ToolboxResult();
                        errorResult.addError("Failed to lint " + file + ": " + e.getMessage());
                        return errorResult;
                    }
                }, executorService))
                .collect(Collectors.toList());
        
        // Collect results
        for (CompletableFuture<ToolboxResult> future : futures) {
            try {
                ToolboxResult fileResult = future.get();
                result.mergeWith(fileResult);
            } catch (Exception e) {
                logger.error("Error getting parallel linting result: {}", e.getMessage());
                result.addError("Parallel processing error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Lint files sequentially
     */
    private void lintFilesSequentially(List<Path> files, ToolboxResult result, boolean verbose, boolean quiet, boolean singleFileMode) {
        for (Path file : files) {
            try {
                lintSingleFile(file, result, verbose, quiet, singleFileMode);
            } catch (Exception e) {
                logger.error("Error linting file {}: {}", file, e.getMessage());
                result.addError("Failed to lint " + file + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Lint a single file
     */
    private void lintFile(Path file, ToolboxResult result, boolean verbose, boolean quiet, boolean singleFileMode) throws IOException {
        lintSingleFile(file, result, verbose, quiet, singleFileMode);
    }
    
    /**
     * Core method to lint a single file
     */
    private void lintSingleFile(Path file, ToolboxResult result, boolean verbose, boolean quiet, boolean singleFileMode) throws IOException {
        if (!quiet && verbose) {
            logger.debug("Analyzing file: {}", file);
        }
        
        // Check file size
        long fileSize = Files.size(file);
        if (fileSize > configManager.getMaxFileSize()) {
            logger.warn("Skipping large file: {} ({} bytes)", file, fileSize);
            result.addWarning("Skipped large file: " + file);
            return;
        }
        
        // Read file content with automatic encoding detection
        Charset fallbackEncoding = Charset.forName(configManager.getEncoding());
        String content = EncodingDetector.readFileWithEncodingDetection(file, fallbackEncoding);
        
        // Determine and create parser
        String selectedParserType = determineParserType(content);
        CfmlParser parser = parserFactory.createParser(selectedParserType);
        if (parser == null) {
            logger.error("No suitable parser found for file: {}", file);
            result.addError("No parser available for: " + file);
            return;
        }
        
        try {
            // Parse and analyze
            var parseResult = parser.parse(content, file.toString());
            List<LintingViolation> violations = ruleEngine.analyzeFile(parseResult, file.toString());
            
            // If in single file mode, enhance violations with context
            if (singleFileMode) {
                violations = enhanceViolationsWithContext(violations, content);
            }
            
            // Add violations to result
            result.addViolations(violations);
            result.getStats().incrementFilesProcessed();
            
            // Track parser usage
            result.getStats().incrementParserUsage(parser.getParserType());
            
            if (!quiet && verbose && !violations.isEmpty()) {
                logger.debug("Found {} violations in {}", violations.size(), file);
            }
            
        } catch (Exception e) {
            logger.error("Error parsing file {}: {}", file, e.getMessage());
            result.addError("Parse error in " + file + ": " + e.getMessage());
        }
    }
    
    /**
     * Determine which parser to use based on content analysis
     */
    private String determineParserType(String content) {
        if ("auto".equals(parserType)) {
            // Simple heuristics to determine parser type
            if (content.contains("component") || content.contains("interface")) {
                return configManager.getPrimaryParser();
            } else if (content.contains("<cf") || content.contains("</cf")) {
                return configManager.getPrimaryParser();
            } else {
                return configManager.getFallbackParser();
            }
        }
        return parserType;
    }
    
    /**
     * Check if path matches include patterns
     */
    private boolean matchesIncludePatterns(Path path, List<String> patterns, Path configBaseDir) {
        String relativePath = getRelativePathString(path, configBaseDir);
        return patterns.stream().anyMatch(pattern -> matchesPattern(relativePath, pattern));
    }
    
    /**
     * Check if path matches exclude patterns
     */
    private boolean matchesExcludePatterns(Path path, List<String> patterns, Path configBaseDir) {
        String relativePath = getRelativePathString(path, configBaseDir);
        return patterns.stream().anyMatch(pattern -> matchesPattern(relativePath, pattern));
    }
    
    /**
     * Get path relative to config base directory
     */
    private String getRelativePathString(Path path, Path configBaseDir) {
        try {
            Path relativePath = configBaseDir.relativize(path.toAbsolutePath());
            return relativePath.toString().replace('\\', '/');
        } catch (IllegalArgumentException e) {
            // If paths are not on same file system, fall back to absolute path
            return path.toString().replace('\\', '/');
        }
    }
    
    /**
     * Simple glob pattern matching
     */
    private boolean matchesPattern(String path, String pattern) {
        // Check for direct equality first
        if (path.equals(pattern)) {
            return true;
        }
        
        // Handle directory patterns (ending with /)
        if (pattern.endsWith("/")) {
            String dirPattern = pattern.substring(0, pattern.length() - 1);
            // Check if path starts with the directory pattern
            if (path.startsWith(dirPattern + "/") || path.equals(dirPattern)) {
                return true;
            }
        }
        
        // Handle patterns that should match directory contents
        if (!pattern.contains("*") && !pattern.contains("?")) {
            // Simple directory name - check if path starts with it
            if (path.startsWith(pattern + "/") || path.equals(pattern)) {
                return true;
            }
        }

        // Convert glob pattern to regex for complex patterns
        String regex = pattern
                .replace("**", "DOUBLE_STAR")
                .replace("*", "[^/]*")
                .replace("DOUBLE_STAR", ".*")
                .replace("?", "[^/]");
        
        return path.matches(regex);
    }
    
    /**
     * Enhance violations with context lines for single file mode
     */
    private List<LintingViolation> enhanceViolationsWithContext(List<LintingViolation> violations, String content) {
        if (violations.isEmpty()) {
            return violations;
        }
        
        // Split content into lines for context extraction
        String[] lines = content.split("\r?\n");
        
        return violations.stream()
                .map(violation -> {
                    String context = extractContext(lines, violation.getLine());
                    
                    // Create new violation with context
                    return new LintingViolation(
                        violation.getRuleId(),
                        violation.getMessage(),
                        violation.getSeverity(),
                        violation.getFilePath(),
                        violation.getLine(),
                        violation.getColumn(),
                        violation.getRuleCategory(),
                        context,  // Add context as code snippet
                        violation.getSuggestedFix()
                    );
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Extract context lines around a specific line (3 before, target line, 3 after)
     */
    private String extractContext(String[] lines, int targetLine) {
        int contextLines = 3;
        int startLine = Math.max(0, targetLine - contextLines - 1); // -1 because line numbers are 1-based
        int endLine = Math.min(lines.length - 1, targetLine + contextLines - 1);
        
        StringBuilder context = new StringBuilder();
        
        for (int i = startLine; i <= endLine; i++) {
            String lineContent = lines[i];
            int lineNumber = i + 1; // Convert back to 1-based line numbers
            
            // Mark the target line with an arrow
            String prefix = (lineNumber == targetLine) ? "➤ " : "  ";
            
            context.append(String.format("%s%4d: %s%n", prefix, lineNumber, lineContent));
        }
        
        return context.toString();
    }
}
