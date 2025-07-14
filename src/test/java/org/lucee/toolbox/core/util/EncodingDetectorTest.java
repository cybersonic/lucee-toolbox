package org.lucee.toolbox.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EncodingDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void testReadUTF8File() throws IOException {
        // Create a UTF-8 file
        String content = "This is a UTF-8 file with some Unicode: 你好世界";
        Path file = tempDir.resolve("utf8.cfc");
        Files.writeString(file, content, StandardCharsets.UTF_8);

        // Read with encoding detection
        String readContent = EncodingDetector.readFileWithEncodingDetection(file);
        assertEquals(content, readContent);
    }

    @Test
    void testReadUTF8FileWithBOM() throws IOException {
        // Create a UTF-8 file with BOM
        String content = "This is a UTF-8 file with BOM";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] bomBytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        
        // Combine BOM + content
        byte[] fileBytes = new byte[bomBytes.length + contentBytes.length];
        System.arraycopy(bomBytes, 0, fileBytes, 0, bomBytes.length);
        System.arraycopy(contentBytes, 0, fileBytes, bomBytes.length, contentBytes.length);
        
        Path file = tempDir.resolve("utf8_bom.cfc");
        Files.write(file, fileBytes);

        // Read with encoding detection
        String readContent = EncodingDetector.readFileWithEncodingDetection(file);
        assertEquals(content, readContent);
    }

    @Test
    void testReadISO88591File() throws IOException {
        // Create an ISO-8859-1 file with special characters
        String content = "This file contains special chars: café, naïve, résumé";
        Path file = tempDir.resolve("iso.cfc");
        Files.writeString(file, content, StandardCharsets.ISO_8859_1);

        // Read with encoding detection
        String readContent = EncodingDetector.readFileWithEncodingDetection(file);
        assertEquals(content, readContent);
    }

    @Test
    void testReadUTF16LEFileWithBOM() throws IOException {
        // Create a UTF-16LE file with BOM
        String content = "UTF-16LE file content";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_16LE);
        byte[] bomBytes = {(byte) 0xFF, (byte) 0xFE};
        
        // Combine BOM + content
        byte[] fileBytes = new byte[bomBytes.length + contentBytes.length];
        System.arraycopy(bomBytes, 0, fileBytes, 0, bomBytes.length);
        System.arraycopy(contentBytes, 0, fileBytes, bomBytes.length, contentBytes.length);
        
        Path file = tempDir.resolve("utf16le.cfc");
        Files.write(file, fileBytes);

        // Read with encoding detection
        String readContent = EncodingDetector.readFileWithEncodingDetection(file);
        assertEquals(content, readContent);
    }

    @Test
    void testReadUTF16BEFileWithBOM() throws IOException {
        // Create a UTF-16BE file with BOM
        String content = "UTF-16BE file content";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_16BE);
        byte[] bomBytes = {(byte) 0xFE, (byte) 0xFF};
        
        // Combine BOM + content
        byte[] fileBytes = new byte[bomBytes.length + contentBytes.length];
        System.arraycopy(bomBytes, 0, fileBytes, 0, bomBytes.length);
        System.arraycopy(contentBytes, 0, fileBytes, bomBytes.length, contentBytes.length);
        
        Path file = tempDir.resolve("utf16be.cfc");
        Files.write(file, fileBytes);

        // Read with encoding detection
        String readContent = EncodingDetector.readFileWithEncodingDetection(file);
        assertEquals(content, readContent);
    }

    @Test
    void testReadEmptyFile() throws IOException {
        // Create an empty file
        Path file = tempDir.resolve("empty.cfc");
        Files.writeString(file, "", StandardCharsets.UTF_8);

        // Read with encoding detection
        String readContent = EncodingDetector.readFileWithEncodingDetection(file);
        assertEquals("", readContent);
    }

    @Test
    void testDetectEncodingNameUTF8() throws IOException {
        // Create a UTF-8 file
        String content = "UTF-8 content with Unicode: 🚀";
        Path file = tempDir.resolve("utf8.cfc");
        Files.writeString(file, content, StandardCharsets.UTF_8);

        // Detect encoding
        String encodingName = EncodingDetector.detectEncodingName(file);
        assertEquals("UTF-8", encodingName);
    }

    @Test
    void testDetectEncodingNameISO88591() throws IOException {
        // Create an ISO-8859-1 file
        String content = "ISO content: café";
        Path file = tempDir.resolve("iso.cfc");
        Files.writeString(file, content, StandardCharsets.ISO_8859_1);

        // Detect encoding
        String encodingName = EncodingDetector.detectEncodingName(file);
        assertEquals("ISO-8859-1", encodingName);
    }

    @Test
    void testReadWithFallbackEncoding() throws IOException {
        // Create a file with problematic encoding
        byte[] weirdBytes = {(byte) 0x80, (byte) 0x90, (byte) 0xA0}; // Invalid UTF-8
        Path file = tempDir.resolve("weird.cfc");
        Files.write(file, weirdBytes);

        // Read with fallback encoding
        Charset fallback = StandardCharsets.ISO_8859_1;
        String readContent = EncodingDetector.readFileWithEncodingDetection(file, fallback);
        
        // Should not throw exception and return some content
        assertNotNull(readContent);
    }

    @Test
    void testSimpleCFMLFile() throws IOException {
        // Create a simple CFML file
        String content = "<cfcomponent>\n" +
                "    <cffunction name=\"test\">\n" +
                "        <cfreturn \"Hello World\">\n" +
                "    </cffunction>\n" +
                "</cfcomponent>";
        
        Path file = tempDir.resolve("test.cfc");
        Files.writeString(file, content, StandardCharsets.UTF_8);

        // Read with encoding detection
        String readContent = EncodingDetector.readFileWithEncodingDetection(file);
        assertEquals(content, readContent);
        
        // Verify encoding detection
        String encodingName = EncodingDetector.detectEncodingName(file);
        assertEquals("UTF-8", encodingName);
    }
}
