package org.lucee.toolbox.core.engine;

import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.model.FormattingChange;
import org.lucee.toolbox.core.model.ToolboxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Main formatting engine for cleaning and formatting CFML code
 */
public class FormattingEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(FormattingEngine.class);
    
    private final ConfigurationManager configManager;
    private final String parserType;
    private final boolean performanceMode;
    private ExecutorService executorService;
    
    public FormattingEngine(ConfigurationManager configManager, String parserType, boolean performanceMode) {
        this.configManager = configManager;
        this.parserType = parserType;
        this.performanceMode = performanceMode;
        
        if (performanceMode && configManager.isParallelProcessingEnabled()) {
            this.executorService = Executors.newFixedThreadPool(configManager.getMaxThreads());
        }
    }
    
    /**
     * Format the given file or directory
     */
    public ToolboxResult format(Path inputPath, boolean verbose, boolean quiet) throws IOException {
        return format(inputPath, verbose, quiet, false);
    }
    
    /**
     * Format the given file or directory with single file mode support
     */
    public ToolboxResult format(Path inputPath, boolean verbose, boolean quiet, boolean singleFileMode) throws IOException {
        long startTime = System.currentTimeMillis();
        ToolboxResult result = new ToolboxResult();
        
        if (!quiet && verbose) {
            logger.info("Starting formatting on: {}", inputPath);
        }
        
        try {
            if (Files.isDirectory(inputPath)) {
                formatDirectory(inputPath, result, verbose, quiet, singleFileMode);
            } else {
                formatFile(inputPath, result, verbose, quiet, singleFileMode);
            }
        } catch (Exception e) {
            logger.error("Error during formatting: {}", e.getMessage(), e);
            result.addError("Formatting failed: " + e.getMessage());
        } finally {
            if (executorService != null) {
                executorService.shutdown();
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        result.getStats().setExecutionTime(duration);
        
        if (!quiet && verbose) {
            logger.info("Formatting completed in {} ms. Applied {} changes", 
                    duration, result.getFormattingChanges().size());
        }
        
        return result;
    }
    
    /**
     * Format all CFML files in a directory
     */
    private void formatDirectory(Path directory, ToolboxResult result, boolean verbose, boolean quiet) throws IOException {
        formatDirectory(directory, result, verbose, quiet, false);
    }
    
    private void formatDirectory(Path directory, ToolboxResult result, boolean verbose, boolean quiet, boolean singleFileMode) throws IOException {
        List<String> includePatterns = configManager.getIncludePatterns();
        List<String> excludePatterns = configManager.getExcludePatterns();
        Path configBaseDir = configManager.getConfigBaseDirectory();
        
        try (Stream<Path> files = Files.walk(directory)) {
            List<Path> cfmlFiles = files
                    .filter(Files::isRegularFile)
                    .filter(path -> matchesIncludePatterns(path, includePatterns, configBaseDir))
                    .filter(path -> !matchesExcludePatterns(path, excludePatterns, configBaseDir))
                    .toList();
            
            if (!quiet && verbose) {
                logger.info("Found {} CFML files to format", cfmlFiles.size());
            }
            
            // Note: singleFileMode is always false for directory processing
            if (performanceMode && executorService != null && cfmlFiles.size() > 1) {
                formatFilesInParallel(cfmlFiles, result, verbose, quiet, false);
            } else {
                formatFilesSequentially(cfmlFiles, result, verbose, quiet, false);
            }
        }
    }
    
    /**
     * Format files in parallel for better performance
     */
    private void formatFilesInParallel(List<Path> files, ToolboxResult result, boolean verbose, boolean quiet) {
        formatFilesInParallel(files, result, verbose, quiet, false);
    }
    
    private void formatFilesInParallel(List<Path> files, ToolboxResult result, boolean verbose, boolean quiet, boolean singleFileMode) {
        List<CompletableFuture<ToolboxResult>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    try {
                        ToolboxResult fileResult = new ToolboxResult();
                        formatSingleFile(file, fileResult, verbose, quiet, singleFileMode);
                        return fileResult;
                    } catch (Exception e) {
                        logger.error("Error formatting file {}: {}", file, e.getMessage());
                        ToolboxResult errorResult = new ToolboxResult();
                        errorResult.addError("Failed to format " + file + ": " + e.getMessage());
                        return errorResult;
                    }
                }, executorService))
                .toList();
        
        // Collect results
        for (CompletableFuture<ToolboxResult> future : futures) {
            try {
                ToolboxResult fileResult = future.get();
                result.mergeWith(fileResult);
            } catch (Exception e) {
                logger.error("Error getting parallel formatting result: {}", e.getMessage());
                result.addError("Parallel processing error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Format files sequentially
     */
    private void formatFilesSequentially(List<Path> files, ToolboxResult result, boolean verbose, boolean quiet) {
        formatFilesSequentially(files, result, verbose, quiet, false);
    }
    
    private void formatFilesSequentially(List<Path> files, ToolboxResult result, boolean verbose, boolean quiet, boolean singleFileMode) {
        for (Path file : files) {
            try {
                formatSingleFile(file, result, verbose, quiet, singleFileMode);
            } catch (Exception e) {
                logger.error("Error formatting file {}: {}", file, e.getMessage());
                result.addError("Failed to format " + file + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Format a single file
     */
    private void formatFile(Path file, ToolboxResult result, boolean verbose, boolean quiet) throws IOException {
        formatFile(file, result, verbose, quiet, false);
    }
    
    private void formatFile(Path file, ToolboxResult result, boolean verbose, boolean quiet, boolean singleFileMode) throws IOException {
        formatSingleFile(file, result, verbose, quiet, singleFileMode);
    }
    
    /**
     * Core method to format a single file
     */
    private void formatSingleFile(Path file, ToolboxResult result, boolean verbose, boolean quiet) throws IOException {
        formatSingleFile(file, result, verbose, quiet, false);
    }
    
    private void formatSingleFile(Path file, ToolboxResult result, boolean verbose, boolean quiet, boolean singleFileMode) throws IOException {
        if (!quiet && verbose) {
            logger.debug("Formatting file: {}", file);
        }
        
        // Read file content
        String originalContent = Files.readString(file, java.nio.charset.Charset.forName(configManager.getEncoding()));
        
        // Apply basic formatting
        String formattedContent = applyBasicFormatting(originalContent);
        
        // Check if formatting actually changed anything
        if (!originalContent.equals(formattedContent)) {
            // Create a meaningful formatting change
            FormattingChange change = new FormattingChange(
                file.toString(), 
                1, 
                originalContent.split("\n").length, 
                originalContent, 
                formattedContent, 
                "formatting", 
                "Applied code formatting (indentation, line breaks, etc.)"
            );
            result.addFormattingChange(change);
            
            if (!quiet && verbose) {
                logger.debug("Applied formatting change in {}", file);
            }
        } else {
            if (!quiet && verbose) {
                logger.debug("No formatting changes needed in {}", file);
            }
        }
        
        result.getStats().incrementFilesProcessed();
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
     * Apply basic formatting to CFML code
     */
    private String applyBasicFormatting(String content) {
        // Basic formatting improvements
        String formatted = content;
        
        // Normalize line endings
        formatted = formatted.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        
        // Remove trailing whitespace from lines
        formatted = formatted.replaceAll("[ \\t]+$", "");
        
        // Ensure consistent indentation (convert tabs to spaces)
        int indentSize = configManager.getIndentSize();
        String spaces = " ".repeat(indentSize);
        formatted = formatted.replaceAll("\\t", spaces);
        
        // Add consistent spacing around operators
        formatted = formatted.replaceAll("\\s*=\\s*", " = ");
        formatted = formatted.replaceAll("\\s*\\+\\s*", " + ");
        formatted = formatted.replaceAll("\\s*-\\s*", " - ");
        formatted = formatted.replaceAll("\\s*\\*\\s*", " * ");
        formatted = formatted.replaceAll("\\s*/\\s*", " / ");
        
        // Clean up multiple blank lines
        formatted = formatted.replaceAll("\n{3,}", "\n\n");
        
        // Ensure file ends with single newline
        formatted = formatted.replaceAll("\n*$", "\n");
        
        return formatted;
    }
}
