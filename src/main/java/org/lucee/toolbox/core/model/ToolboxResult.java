package org.lucee.toolbox.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Container for toolbox execution results including linting violations,
 * formatting changes, and execution metadata.
 */
public class ToolboxResult {
    
    private final LocalDateTime executionTime;
    private final List<LintingViolation> violations;
    private final List<FormattingChange> formattingChanges;
    private final Map<String, Object> metadata;
    private final List<String> errors;
    private final List<String> warnings;
    private final ExecutionStats stats;
    
    public ToolboxResult() {
        this.executionTime = LocalDateTime.now();
        this.violations = new ArrayList<>();
        this.formattingChanges = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.stats = new ExecutionStats();
    }
    
    // Violation management
    public void addViolation(LintingViolation violation) {
        this.violations.add(violation);
        this.stats.incrementViolationCount(violation.getSeverity());
    }
    
    public void addViolations(List<LintingViolation> violations) {
        for (LintingViolation violation : violations) {
            addViolation(violation);
        }
    }
    
    public List<LintingViolation> getViolations() {
        return new ArrayList<>(violations);
    }
    
    public List<LintingViolation> getViolationsBySeverity(Severity severity) {
        return violations.stream()
                .filter(v -> v.getSeverity() == severity)
                .toList();
    }
    
    // Formatting changes management
    public void addFormattingChange(FormattingChange change) {
        this.formattingChanges.add(change);
        this.stats.incrementFormattingChanges();
    }
    
    public void addFormattingChanges(List<FormattingChange> changes) {
        for (FormattingChange change : changes) {
            addFormattingChange(change);
        }
    }
    
    public List<FormattingChange> getFormattingChanges() {
        return new ArrayList<>(formattingChanges);
    }
    
    // Error and warning management
    public void addError(String error) {
        this.errors.add(error);
        this.stats.incrementErrorCount();
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
        this.stats.incrementWarningCount();
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    // Status checks
    public boolean hasErrors() {
        return !errors.isEmpty() || !violations.stream()
                .filter(v -> v.getSeverity() == Severity.ERROR)
                .toList().isEmpty();
    }
    
    public boolean hasViolations() {
        return !violations.isEmpty();
    }
    
    public boolean hasFormattingChanges() {
        return !formattingChanges.isEmpty();
    }
    
    // Metadata management
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }
    
    public Map<String, Object> getAllMetadata() {
        return new HashMap<>(metadata);
    }
    
    // Merge results (for combining lint and format results)
    public void mergeWith(ToolboxResult other) {
        this.violations.addAll(other.violations);
        this.formattingChanges.addAll(other.formattingChanges);
        this.errors.addAll(other.errors);
        this.warnings.addAll(other.warnings);
        this.metadata.putAll(other.metadata);
        this.stats.mergeWith(other.stats);
    }
    
    // Getters
    public LocalDateTime getExecutionTime() {
        return executionTime;
    }
    
    public ExecutionStats getStats() {
        return stats;
    }
    
    // Execution statistics
    public static class ExecutionStats {
        private int filesProcessed = 0;
        private int totalViolations = 0;
        private int errorCount = 0;
        private int warningCount = 0;
        private int infoCount = 0;
        private int formattingChanges = 0;
        private long executionTimeMs = 0;
        private final Map<String, Integer> parserUsage = new HashMap<>();
        
        public void incrementFilesProcessed() {
            this.filesProcessed++;
        }
        
        public void incrementViolationCount(Severity severity) {
            this.totalViolations++;
            switch (severity) {
                case ERROR:
                    this.errorCount++;
                    break;
                case WARNING:
                    this.warningCount++;
                    break;
                case INFO:
                    this.infoCount++;
                    break;
            }
        }
        
        public void incrementErrorCount() {
            this.errorCount++;
        }
        
        public void incrementWarningCount() {
            this.warningCount++;
        }
        
        public void incrementFormattingChanges() {
            this.formattingChanges++;
        }
        
        public void setExecutionTime(long timeMs) {
            this.executionTimeMs = timeMs;
        }
        
        public void incrementParserUsage(String parserType) {
            this.parserUsage.put(parserType, this.parserUsage.getOrDefault(parserType, 0) + 1);
        }
        
        public void mergeWith(ExecutionStats other) {
            this.filesProcessed += other.filesProcessed;
            this.totalViolations += other.totalViolations;
            this.errorCount += other.errorCount;
            this.warningCount += other.warningCount;
            this.infoCount += other.infoCount;
            this.formattingChanges += other.formattingChanges;
            this.executionTimeMs += other.executionTimeMs;
            
            // Merge parser usage statistics
            for (Map.Entry<String, Integer> entry : other.parserUsage.entrySet()) {
                this.parserUsage.put(entry.getKey(), 
                    this.parserUsage.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
        
        // Getters
        public int getFilesProcessed() { return filesProcessed; }
        public int getTotalViolations() { return totalViolations; }
        public int getErrorCount() { return errorCount; }
        public int getWarningCount() { return warningCount; }
        public int getInfoCount() { return infoCount; }
        public int getFormattingChanges() { return formattingChanges; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public Map<String, Integer> getParserUsage() { return new HashMap<>(parserUsage); }
    }
}
