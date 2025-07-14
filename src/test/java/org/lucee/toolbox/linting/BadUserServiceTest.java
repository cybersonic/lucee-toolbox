package org.lucee.toolbox.linting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lucee.toolbox.LuceeToolbox;
import org.lucee.toolbox.core.config.ConfigurationManager;
import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.Severity;
import org.lucee.toolbox.core.model.ToolboxResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for validating linting of the bad userService.cfc file
 */
public class BadUserServiceTest {
    
    private LuceeToolbox toolbox;
    private ConfigurationManager configManager;
    private Path testFile;
    private static final String TEST_FILE_PATH = "test-files/bad/userService.cfc";
    
    @BeforeEach
    public void setUp() throws IOException {
        toolbox = new LuceeToolbox();
        configManager = new ConfigurationManager();
        configManager.loadConfiguration("lucee-toolbox.json"); // Will use defaults if not found
        testFile = Paths.get(TEST_FILE_PATH);
    }
    
    /**
     * Helper method to run linting on a file and return the result
     */
    private ToolboxResult lintFile(String filePath) throws IOException {
        return toolbox.execute(
            filePath,
            "lint",
            "auto",
            "json",
            null,
            configManager,
            false, // verbose
            true,  // quiet
            false  // performance mode
        );
    }
    
    @Test
    public void testLintingDetectsExpectedViolations() throws IOException {
        // Ensure the test file exists
        assertTrue(Files.exists(testFile), "Test file should exist: " + TEST_FILE_PATH);
        
        // Run linting on the bad file
        ToolboxResult result = lintFile(TEST_FILE_PATH);
        
        // Verify that violations were found
        assertNotNull(result, "Result should not be null");
        List<LintingViolation> violations = result.getViolations();
        assertFalse(violations.isEmpty(), "Should have found violations");
        
        // Print violations for debugging
        System.out.println("Found " + violations.size() + " violations:");
        for (LintingViolation violation : violations) {
            System.out.println("  - " + violation.getRuleId() + " at line " + violation.getLine() + 
                             ", column " + violation.getColumn() + ": " + violation.getMessage());
        }
        
        // Verify expected violations
        verifyTrailingWhitespaceViolations(violations);
        verifyExcessiveEmptyLineViolations(violations);
    }
    
    private void verifyTrailingWhitespaceViolations(List<LintingViolation> violations) {
        List<LintingViolation> trailingWhitespaceViolations = violations.stream()
            .filter(v -> "TRAILING_WHITESPACE".equals(v.getRuleId()))
            .collect(Collectors.toList());
        
        // Should have multiple trailing whitespace violations
        assertTrue(trailingWhitespaceViolations.size() >= 5, 
            "Should have at least 5 trailing whitespace violations, found: " + trailingWhitespaceViolations.size());
        
        // Verify specific lines with trailing whitespace
        assertTrue(hasViolationAtLine(trailingWhitespaceViolations, 3), 
            "Should have trailing whitespace violation at line 3 (property NAME='username';   )");
        assertTrue(hasViolationAtLine(trailingWhitespaceViolations, 8), 
            "Should have trailing whitespace violation at line 8 (function init(   )  )");
        assertTrue(hasViolationAtLine(trailingWhitespaceViolations, 19), 
            "Should have trailing whitespace violation at line 19 (qGetUsers.setSQL line)");
        assertTrue(hasViolationAtLine(trailingWhitespaceViolations, 24), 
            "Should have trailing whitespace violation at line 24 (results = result;   )");
        
        // Verify all are WARNING severity
        for (LintingViolation violation : trailingWhitespaceViolations) {
            assertEquals(Severity.WARNING, violation.getSeverity(), 
                "Trailing whitespace violations should be WARNING severity");
        }
    }
    
    private void verifyExcessiveEmptyLineViolations(List<LintingViolation> violations) {
        List<LintingViolation> emptyLineViolations = violations.stream()
            .filter(v -> "EXCESSIVE_EMPTY_LINES".equals(v.getRuleId()))
            .collect(Collectors.toList());
        
        // Should have multiple excessive empty line violations
        assertTrue(emptyLineViolations.size() >= 2, 
            "Should have at least 2 excessive empty line violations, found: " + emptyLineViolations.size());
        
        // Verify all are INFO severity
        for (LintingViolation violation : emptyLineViolations) {
            assertEquals(Severity.INFO, violation.getSeverity(), 
                "Excessive empty line violations should be INFO severity");
        }
    }
    
    private boolean hasViolationAtLine(List<LintingViolation> violations, int line) {
        return violations.stream()
            .anyMatch(v -> v.getLine() == line);
    }
    
    @Test
    public void testLintingJsonOutput() throws IOException {
        // Run linting and get JSON output
        ToolboxResult result = lintFile(TEST_FILE_PATH);
        
        // Verify statistics
        assertNotNull(result.getStats(), "Statistics should not be null");
        assertEquals(1, result.getStats().getFilesProcessed(), "Should process 1 file");
        assertTrue(result.getStats().getTotalViolations() > 0, "Should have violations");
        assertTrue(result.getStats().getWarningCount() > 0, "Should have warnings");
        assertTrue(result.getStats().getInfoCount() > 0, "Should have info violations");
        assertEquals(0, result.getStats().getErrorCount(), "Should not have errors");
    }
    
    @Test
    public void testSpecificViolationDetails() throws IOException {
        ToolboxResult result = lintFile(TEST_FILE_PATH);
        List<LintingViolation> violations = result.getViolations();
        
        // Verify file path is correct in all violations
        for (LintingViolation violation : violations) {
            assertEquals(TEST_FILE_PATH, violation.getFilePath(), 
                "All violations should reference the correct file");
        }
        
        // Verify violation messages are descriptive
        for (LintingViolation violation : violations) {
            assertNotNull(violation.getMessage(), "Message should not be null");
            assertFalse(violation.getMessage().trim().isEmpty(), "Message should not be empty");
        }
        
        // Verify line and column numbers are positive
        for (LintingViolation violation : violations) {
            assertTrue(violation.getLine() > 0, "Line number should be positive");
            assertTrue(violation.getColumn() > 0, "Column number should be positive");
        }
    }
    
    @Test
    public void testNoFormattingChanges() throws IOException {
        // Since we're only linting (not formatting), there should be no formatting changes
        ToolboxResult result = lintFile(TEST_FILE_PATH);
        
        assertTrue(result.getFormattingChanges().isEmpty(), 
            "Should have no formatting changes when only linting");
        assertEquals(0, result.getStats().getFormattingChanges(), 
            "Statistics should show 0 formatting changes");
    }
}
