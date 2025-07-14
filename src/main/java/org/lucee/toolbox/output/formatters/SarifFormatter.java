package org.lucee.toolbox.output.formatters;

import org.lucee.toolbox.core.model.ToolboxResult;
import org.lucee.toolbox.output.OutputFormatter;

public class SarifFormatter implements OutputFormatter {
    @Override
    public String format(ToolboxResult result) {
        return "{\n  \"$schema\": \"https://schemastore.azurewebsites.net/schemas/json/sarif-2.1.0.json\",\n  \"version\": \"2.1.0\",\n  \"runs\": []\n}";
    }
    
    @Override
    public String getFileExtension() {
        return "sarif";
    }
    
    @Override
    public String getContentType() {
        return "application/json";
    }
}
