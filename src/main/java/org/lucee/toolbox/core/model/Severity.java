package org.lucee.toolbox.core.model;

/**
 * Severity levels for linting violations
 */
public enum Severity {
    ERROR("error", 3),
    WARNING("warning", 2),
    INFO("info", 1);
    
    private final String name;
    private final int level;
    
    Severity(String name, int level) {
        this.name = name;
        this.level = level;
    }
    
    public String getName() {
        return name;
    }
    
    public int getLevel() {
        return level;
    }
    
    public static Severity fromString(String name) {
        for (Severity severity : values()) {
            if (severity.name.equalsIgnoreCase(name)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Unknown severity: " + name);
    }
}
