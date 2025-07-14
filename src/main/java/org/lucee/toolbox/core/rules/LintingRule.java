package org.lucee.toolbox.core.rules;

import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.parser.ParseResult;

import java.util.List;

/**
 * Interface for linting rules
 */
public interface LintingRule {
    
    /**
     * Get the unique identifier for this rule
     * @return Rule ID string
     */
    String getRuleId();
    
    /**
     * Analyze the parse result and return any violations
     * @param parseResult The parsed content to analyze
     * @return List of violations found by this rule
     */
    List<LintingViolation> analyze(ParseResult parseResult);
}
