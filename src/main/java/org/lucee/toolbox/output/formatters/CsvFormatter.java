package org.lucee.toolbox.output.formatters;

import org.lucee.toolbox.core.model.ToolboxResult;
import org.lucee.toolbox.output.OutputFormatter;

public class CsvFormatter implements OutputFormatter {
    @Override
    public String format(ToolboxResult result) {
        return "file,line,column,severity,rule,message\n# CSV output not yet implemented\n";
    }
    
    @Override
    public String getFileExtension() {
        return "csv";
    }
    
    @Override
    public String getContentType() {
        return "text/csv";
    }
}
