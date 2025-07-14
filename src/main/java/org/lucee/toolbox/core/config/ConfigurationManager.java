package org.lucee.toolbox.core.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Manages configuration for the Lucee Toolbox including rule sets,
 * formatting options, and parser settings.
 */
public class ConfigurationManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    private static final String DEFAULT_CONFIG_RESOURCE = "/lucee-toolbox.json";
    
    private JsonNode configuration;
    private final ObjectMapper objectMapper;
    private Path configBaseDirectory;
    
    public ConfigurationManager() {
        this.objectMapper = new ObjectMapper();
        loadDefaultConfiguration();
    }
    
    /**
     * Load configuration from file path
     */
    public void loadConfiguration(String configPath) throws IOException {
        Path path = Paths.get(configPath);
        
        if (Files.exists(path)) {
            logger.info("Loading configuration from: {}", configPath);
            this.configuration = objectMapper.readTree(Files.readString(path));
            this.configBaseDirectory = path.getParent().toAbsolutePath();
        } else {
            logger.warn("Configuration file not found: {}, using defaults", configPath);
            loadDefaultConfiguration();
        }
    }
    
    /**
     * Load default configuration from resources
     */
    private void loadDefaultConfiguration() {
        try (InputStream is = getClass().getResourceAsStream(DEFAULT_CONFIG_RESOURCE)) {
            if (is != null) {
                this.configuration = objectMapper.readTree(is);
                this.configBaseDirectory = Paths.get(".").toAbsolutePath();
                logger.info("Loaded default configuration from resources");
            } else {
                throw new IllegalStateException("Default configuration not found in resources");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load default configuration", e);
        }
    }
    
    // Parser configuration
    public String getPrimaryParser() {
        return getStringValue("parser.primary", "boxlang");
    }
    
    public String getFallbackParser() {
        return getStringValue("parser.fallback", "regex");
    }
    
    public int getParserTimeout() {
        return getIntValue("parser.timeout", 30000);
    }
    
    public long getMaxFileSize() {
        return getLongValue("parser.maxFileSize", 10485760L);
    }
    
    public String getEncoding() {
        return getStringValue("parser.encoding", "UTF-8");
    }
    
    // Performance configuration
    public boolean isParallelProcessingEnabled() {
        return getBooleanValue("performance.parallelProcessing", true);
    }
    
    public int getMaxThreads() {
        return getIntValue("performance.maxThreads", 4);
    }
    
    public boolean isCachingEnabled() {
        return getBooleanValue("performance.enableCaching", true);
    }
    
    public String getCacheDirectory() {
        return getStringValue("performance.cacheDirectory", ".lucee-toolbox-cache");
    }
    
    // Linting configuration
    public boolean isLintingEnabled() {
        return getBooleanValue("linting.enabled", true);
    }
    
    public List<String> getErrorSeverityRules() {
        return getStringListValue("linting.severity.error", Arrays.asList("SECURITY_VIOLATION", "SYNTAX_ERROR"));
    }
    
    public List<String> getWarningSeverityRules() {
        return getStringListValue("linting.severity.warning", Arrays.asList("NAMING_CONVENTION", "BEST_PRACTICE"));
    }
    
    public List<String> getInfoSeverityRules() {
        return getStringListValue("linting.severity.info", Arrays.asList("STYLE_GUIDE", "DOCUMENTATION"));
    }
    
    // Naming convention rules
    public String getComponentCase() {
        return getStringValue("linting.rules.naming.componentCase", "PascalCase");
    }
    
    public String getFunctionCase() {
        return getStringValue("linting.rules.naming.functionCase", "camelCase");
    }
    
    public String getVariableCase() {
        return getStringValue("linting.rules.naming.variableCase", "camelCase");
    }
    
    public String getConstantCase() {
        return getStringValue("linting.rules.naming.constantCase", "UPPER_CASE");
    }
    
    public String getFileCase() {
        return getStringValue("linting.rules.naming.fileCase", "camelCase");
    }
    
    public String getCfcFileCase() {
        return getStringValue("linting.rules.naming.cfcFileCase", "PascalCase");
    }
    
    public String getCfmFileCase() {
        return getStringValue("linting.rules.naming.cfmFileCase", "camelCase");
    }
    
    public String getInterfacePrefix() {
        return getStringValue("linting.rules.naming.interfacePrefix", "I");
    }
    
    public String getAbstractSuffix() {
        return getStringValue("linting.rules.naming.abstractSuffix", "Abstract");
    }
    
    public String getBaseSuffix() {
        return getStringValue("linting.rules.naming.baseSuffix", "Base");
    }
    
    // Whitespace rules
    public int getIndentSize() {
        return getIntValue("linting.rules.whitespace.indentSize", 4);
    }
    
    public String getIndentType() {
        return getStringValue("linting.rules.whitespace.indentType", "spaces");
    }
    
    public boolean shouldTrimTrailingWhitespace() {
        return getBooleanValue("linting.rules.whitespace.trimTrailingWhitespace", true);
    }
    
    public boolean shouldInsertFinalNewline() {
        return getBooleanValue("linting.rules.whitespace.insertFinalNewline", true);
    }
    
    public int getMaxEmptyLines() {
        return getIntValue("linting.rules.whitespace.maxEmptyLines", 1);
    }
    
    // Code structure rules
    public boolean shouldRequireCurlyBraces() {
        return getBooleanValue("linting.rules.codeStructure.requireCurlyBraces", true);
    }
    
    public String getCurlyBraceStyle() {
        return getStringValue("linting.rules.codeStructure.curlyBraceStyle", "same-line");
    }
    
    public int getMaxFunctionLength() {
        return getIntValue("linting.rules.codeStructure.maxFunctionLength", 50);
    }
    
    public int getMaxLineLength() {
        return getIntValue("linting.rules.codeStructure.maxLineLength", 120);
    }
    
    public boolean shouldRequireInit() {
        return getBooleanValue("linting.rules.codeStructure.requireInit", true);
    }
    
    public boolean shouldRequireReturnTypes() {
        return getBooleanValue("linting.rules.codeStructure.requireReturnTypes", true);
    }
    
    public boolean shouldRequireArgumentTypes() {
        return getBooleanValue("linting.rules.codeStructure.requireArgumentTypes", true);
    }
    
    public boolean shouldUseAccessors() {
        return getBooleanValue("linting.rules.codeStructure.useAccessors", true);
    }
    
    // Best practices
    public boolean shouldUseVarScoping() {
        return getBooleanValue("linting.rules.bestPractices.useVarScoping", true);
    }
    
    public boolean shouldAvoidEvaluate() {
        return getBooleanValue("linting.rules.bestPractices.avoidEvaluate", true);
    }
    
    public boolean shouldPreferDoubleQuotes() {
        return getBooleanValue("linting.rules.bestPractices.preferDoubleQuotes", true);
    }
    
    // Security rules
    public boolean shouldCheckSqlInjection() {
        return getBooleanValue("linting.rules.security.checkSqlInjection", true);
    }
    
    public boolean shouldCheckXss() {
        return getBooleanValue("linting.rules.security.checkXss", true);
    }
    
    // Formatting configuration
    public boolean isFormattingEnabled() {
        return getBooleanValue("formatting.enabled", true);
    }
    
    public String getFormattingIndentationType() {
        return getStringValue("formatting.indentation.type", "spaces");
    }
    
    public int getFormattingIndentationSize() {
        return getIntValue("formatting.indentation.size", 4);
    }
    
    public String getFormattingBraceStyle() {
        return getStringValue("formatting.braces.style", "same-line");
    }
    
    public int getFormattingMaxLineLength() {
        return getIntValue("formatting.wrapping.maxLineLength", 120);
    }
    
    // File patterns
    public List<String> getIncludePatterns() {
        return getStringListValue("includes", Arrays.asList("**/*.cfm", "**/*.cfc", "**/*.cfml"));
    }
    
    public List<String> getExcludePatterns() {
        return getStringListValue("excludes", Arrays.asList(
                "**/node_modules/**", "**/target/**", "**/build/**", "**/.git/**"
        ));
    }
    
    // Get config base directory for resolving relative patterns
    public Path getConfigBaseDirectory() {
        return configBaseDirectory;
    }
    
    // Custom rules
    public JsonNode getCustomRules(String ruleSet) {
        JsonNode customRules = configuration.path("customRules");
        return customRules.path(ruleSet);
    }
    
    // Utility methods for accessing nested configuration values
    public String getStringValue(String path, String defaultValue) {
        return getNodeValue(path, JsonNode::asText, defaultValue);
    }
    
    public int getIntValue(String path, int defaultValue) {
        return getNodeValue(path, JsonNode::asInt, defaultValue);
    }
    
    public long getLongValue(String path, long defaultValue) {
        return getNodeValue(path, JsonNode::asLong, defaultValue);
    }
    
    public boolean getBooleanValue(String path, boolean defaultValue) {
        return getNodeValue(path, JsonNode::asBoolean, defaultValue);
    }
    
    private List<String> getStringListValue(String path, List<String> defaultValue) {
        JsonNode node = getNode(path);
        if (node.isArray()) {
            List<String> result = new ArrayList<>();
            for (JsonNode item : node) {
                result.add(item.asText());
            }
            return result;
        }
        return defaultValue;
    }
    
    private <T> T getNodeValue(String path, NodeValueExtractor<T> extractor, T defaultValue) {
        JsonNode node = getNode(path);
        if (node.isMissingNode()) {
            return defaultValue;
        }
        return extractor.extract(node);
    }
    
    private JsonNode getNode(String path) {
        JsonNode current = configuration;
        String[] parts = path.split("\\.");
        
        for (String part : parts) {
            current = current.path(part);
            if (current.isMissingNode()) {
                break;
            }
        }
        
        return current;
    }
    
    @FunctionalInterface
    private interface NodeValueExtractor<T> {
        T extract(JsonNode node);
    }
    
    // Configuration export
    public String exportConfiguration() throws IOException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configuration);
    }
    
    // Get configuration summary for display
    public String getConfigurationSummary(String configFilePath) {
        StringBuilder summary = new StringBuilder();
        summary.append("Configuration Summary:\n");
        summary.append("=====================\n\n");
        
        if (configFilePath != null) {
            summary.append("Config file: ").append(configFilePath).append("\n");
        } else {
            summary.append("Config file: Default (built-in)\n");
        }
        
        summary.append("\nParser Settings:\n");
        summary.append("  Primary parser: ").append(getPrimaryParser()).append("\n");
        summary.append("  Fallback parser: ").append(getFallbackParser()).append("\n");
        summary.append("  Timeout: ").append(getParserTimeout()).append(" ms\n");
        summary.append("  Max file size: ").append(getMaxFileSize()).append(" bytes\n");
        summary.append("  Encoding: ").append(getEncoding()).append("\n");
        
        summary.append("\nLinting Settings:\n");
        summary.append("  Enabled: ").append(isLintingEnabled()).append("\n");
        summary.append("  Max line length: ").append(getMaxLineLength()).append("\n");
        summary.append("  Max function length: ").append(getMaxFunctionLength()).append("\n");
        summary.append("  Require curly braces: ").append(shouldRequireCurlyBraces()).append("\n");
        summary.append("  Require init(): ").append(shouldRequireInit()).append("\n");
        summary.append("  Require return types: ").append(shouldRequireReturnTypes()).append("\n");
        summary.append("  Require argument types: ").append(shouldRequireArgumentTypes()).append("\n");
        
        summary.append("\nNaming Conventions:\n");
        summary.append("  Component case: ").append(getComponentCase()).append("\n");
        summary.append("  Function case: ").append(getFunctionCase()).append("\n");
        summary.append("  Variable case: ").append(getVariableCase()).append("\n");
        summary.append("  Constant case: ").append(getConstantCase()).append("\n");
        summary.append("  File case: ").append(getFileCase()).append("\n");
        summary.append("  CFC file case: ").append(getCfcFileCase()).append("\n");
        summary.append("  CFM file case: ").append(getCfmFileCase()).append("\n");
        
        summary.append("\nFile Patterns:\n");
        summary.append("  Includes: ").append(String.join(", ", getIncludePatterns())).append("\n");
        summary.append("  Excludes: ").append(String.join(", ", getExcludePatterns())).append("\n");
        
        summary.append("\nPerformance Settings:\n");
        summary.append("  Parallel processing: ").append(isParallelProcessingEnabled()).append("\n");
        summary.append("  Max threads: ").append(getMaxThreads()).append("\n");
        summary.append("  Caching enabled: ").append(isCachingEnabled()).append("\n");
        
        return summary.toString();
    }
    
    // Configuration validation
    public void validateConfiguration() throws IllegalStateException {
        List<String> errors = new ArrayList<>();
        
        // Validate parser settings
        String primary = getPrimaryParser();
        if (!Arrays.asList("boxlang", "lucee", "regex").contains(primary)) {
            errors.add("Invalid primary parser: " + primary);
        }
        
        // Validate performance settings
        if (getMaxThreads() < 1) {
            errors.add("maxThreads must be at least 1");
        }
        
        // Validate indent settings
        if (getIndentSize() < 1) {
            errors.add("indentSize must be at least 1");
        }
        
        if (!Arrays.asList("spaces", "tabs").contains(getIndentType())) {
            errors.add("indentType must be 'spaces' or 'tabs'");
        }
        
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Configuration validation failed: " + String.join(", ", errors));
        }
    }
}
