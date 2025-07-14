package org.lucee.toolbox.output;

import org.lucee.toolbox.output.formatters.*;

/**
 * Factory for creating output formatters based on format type
 */
public class OutputFormatFactory {
    
    /**
     * Get an output formatter for the specified format
     * @param format The format type (console, json, bitbucket, html, csv, junit, sarif)
     * @return OutputFormatter instance
     * @throws IllegalArgumentException if format is not supported
     */
    public static OutputFormatter getFormatter(String format) {
        if (format == null) {
            throw new IllegalArgumentException("Format cannot be null");
        }
        
        switch (format.toLowerCase()) {
            case "console":
                return new ConsoleFormatter();
            case "json":
                return new JsonFormatter();
            case "bitbucket":
                return new BitbucketFormatter();
            case "html":
                return new HtmlFormatter();
            case "csv":
                return new CsvFormatter();
            case "junit":
                return new JunitFormatter();
            case "sarif":
                return new SarifFormatter();
            default:
                throw new IllegalArgumentException("Unsupported output format: " + format);
        }
    }
    
    /**
     * Check if a format is supported
     * @param format The format to check
     * @return true if supported, false otherwise
     */
    public static boolean isFormatSupported(String format) {
        try {
            getFormatter(format);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Get all supported formats
     * @return Array of supported format names
     */
    public static String[] getSupportedFormats() {
        return new String[]{"console", "json", "bitbucket", "html", "csv", "junit", "sarif"};
    }
}
