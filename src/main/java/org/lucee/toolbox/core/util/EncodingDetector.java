package org.lucee.toolbox.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for detecting and handling different file encodings
 */
public class EncodingDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(EncodingDetector.class);
    
    // Common encodings to try, in order of preference
    private static final List<Charset> COMMON_ENCODINGS = Arrays.asList(
            StandardCharsets.UTF_8,
            StandardCharsets.UTF_16,
            StandardCharsets.UTF_16BE,
            StandardCharsets.UTF_16LE,
            StandardCharsets.ISO_8859_1,
            Charset.forName("windows-1252"),
            Charset.forName("US-ASCII")
    );
    
    /**
     * Read file content with automatic encoding detection
     */
    public static String readFileWithEncodingDetection(Path file) throws IOException {
        return readFileWithEncodingDetection(file, StandardCharsets.UTF_8);
    }
    
    /**
     * Read file content with automatic encoding detection, using fallback encoding if detection fails
     */
    public static String readFileWithEncodingDetection(Path file, Charset fallbackEncoding) throws IOException {
        // First, try to detect BOM (Byte Order Mark)
        byte[] fileBytes = Files.readAllBytes(file);
        if (fileBytes.length == 0) {
            return "";
        }
        
        // Check for BOM
        Charset bomDetectedEncoding = detectBOM(fileBytes);
        if (bomDetectedEncoding != null) {
            logger.debug("Detected BOM encoding for file {}: {}", file, bomDetectedEncoding.name());
            return new String(removeByteOrderMark(fileBytes), bomDetectedEncoding);
        }
        
        // Try different encodings
        for (Charset encoding : COMMON_ENCODINGS) {
            try {
                String content = new String(fileBytes, encoding);
                
                // Basic validation: check for replacement characters
                if (!content.contains("\uFFFD")) {
                    // Additional validation for text files
                    if (isValidTextContent(content)) {
                        if (!encoding.equals(StandardCharsets.UTF_8)) {
                            logger.debug("Detected encoding for file {}: {}", file, encoding.name());
                        }
                        return content;
                    }
                }
            } catch (Exception e) {
                // Continue to next encoding
                logger.trace("Failed to decode file {} with encoding {}: {}", file, encoding.name(), e.getMessage());
            }
        }
        
        // Fallback: use the specified fallback encoding
        logger.warn("Could not detect encoding for file {}, using fallback: {}", file, fallbackEncoding.name());
        return new String(fileBytes, fallbackEncoding);
    }
    
    /**
     * Detect encoding from Byte Order Mark (BOM)
     */
    private static Charset detectBOM(byte[] bytes) {
        if (bytes.length >= 3) {
            // UTF-8 BOM: EF BB BF
            if (bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                return StandardCharsets.UTF_8;
            }
        }
        
        if (bytes.length >= 2) {
            // UTF-16 BE BOM: FE FF
            if (bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
                return StandardCharsets.UTF_16BE;
            }
            
            // UTF-16 LE BOM: FF FE
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
                return StandardCharsets.UTF_16LE;
            }
        }
        
        if (bytes.length >= 4) {
            // UTF-32 BE BOM: 00 00 FE FF
            if (bytes[0] == 0x00 && bytes[1] == 0x00 && bytes[2] == (byte) 0xFE && bytes[3] == (byte) 0xFF) {
                return Charset.forName("UTF-32BE");
            }
            
            // UTF-32 LE BOM: FF FE 00 00
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE && bytes[2] == 0x00 && bytes[3] == 0x00) {
                return Charset.forName("UTF-32LE");
            }
        }
        
        return null;
    }
    
    /**
     * Remove BOM from byte array
     */
    private static byte[] removeByteOrderMark(byte[] bytes) {
        if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            // UTF-8 BOM
            return Arrays.copyOfRange(bytes, 3, bytes.length);
        }
        
        if (bytes.length >= 2) {
            if ((bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) ||
                (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE)) {
                // UTF-16 BOM
                return Arrays.copyOfRange(bytes, 2, bytes.length);
            }
        }
        
        if (bytes.length >= 4) {
            if ((bytes[0] == 0x00 && bytes[1] == 0x00 && bytes[2] == (byte) 0xFE && bytes[3] == (byte) 0xFF) ||
                (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE && bytes[2] == 0x00 && bytes[3] == 0x00)) {
                // UTF-32 BOM
                return Arrays.copyOfRange(bytes, 4, bytes.length);
            }
        }
        
        return bytes;
    }
    
    /**
     * Basic validation to check if content appears to be valid text
     */
    private static boolean isValidTextContent(String content) {
        if (content.isEmpty()) {
            return true;
        }
        
        // Check for reasonable ratio of control characters
        int controlChars = 0;
        int totalChars = content.length();
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            // Allow common control characters
            if (Character.isISOControl(c) && c != '\t' && c != '\n' && c != '\r') {
                controlChars++;
            }
        }
        
        // If more than 5% of characters are unexpected control characters, 
        // it's probably not text or wrong encoding
        double controlRatio = (double) controlChars / totalChars;
        return controlRatio < 0.05;
    }
    
    /**
     * Get the encoding name for a file, attempting to detect it
     */
    public static String detectEncodingName(Path file) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file);
        if (fileBytes.length == 0) {
            return StandardCharsets.UTF_8.name();
        }
        
        // Check for BOM
        Charset bomDetectedEncoding = detectBOM(fileBytes);
        if (bomDetectedEncoding != null) {
            return bomDetectedEncoding.name();
        }
        
        // Try different encodings
        for (Charset encoding : COMMON_ENCODINGS) {
            try {
                String content = new String(fileBytes, encoding);
                
                // Basic validation
                if (!content.contains("\uFFFD") && isValidTextContent(content)) {
                    return encoding.name();
                }
            } catch (Exception e) {
                // Continue to next encoding
            }
        }
        
        // Default fallback
        return StandardCharsets.UTF_8.name();
    }
}
