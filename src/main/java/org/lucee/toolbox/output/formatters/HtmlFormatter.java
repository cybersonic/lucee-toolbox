package org.lucee.toolbox.output.formatters;

import org.lucee.toolbox.core.model.ToolboxResult;
import org.lucee.toolbox.output.OutputFormatter;

public class HtmlFormatter implements OutputFormatter {
    @Override
    public String format(ToolboxResult result) {
        return "<html><body><h1>Lucee Toolbox Results</h1><p>HTML output not yet implemented</p></body></html>";
    }
    
    @Override
    public String getFileExtension() {
        return "html";
    }
    
    @Override
    public String getContentType() {
        return "text/html";
    }
}
