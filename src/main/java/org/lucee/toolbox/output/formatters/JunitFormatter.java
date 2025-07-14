package org.lucee.toolbox.output.formatters;

import org.lucee.toolbox.core.model.ToolboxResult;
import org.lucee.toolbox.output.OutputFormatter;

public class JunitFormatter implements OutputFormatter {
    @Override
    public String format(ToolboxResult result) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<testsuite name=\"lucee-toolbox\">\n<!-- JUnit XML output not yet implemented -->\n</testsuite>";
    }
    
    @Override
    public String getFileExtension() {
        return "xml";
    }
    
    @Override
    public String getContentType() {
        return "application/xml";
    }
}
