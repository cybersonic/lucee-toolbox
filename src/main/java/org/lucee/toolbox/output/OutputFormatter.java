package org.lucee.toolbox.output;

import org.lucee.toolbox.core.model.ToolboxResult;

/**
 * Interface for formatting toolbox results into different output formats
 */
public interface OutputFormatter {
    
    /**
     * Format the result into a specific output format
     * @param result The result to format
     * @return Formatted string output
     */
    String format(ToolboxResult result);
    
    /**
     * Get the file extension for this format
     * @return File extension (without the dot) or null if not applicable
     */
    String getFileExtension();
    
    /**
     * Get the content type for this format
     * @return Content type string
     */
    String getContentType();
}
