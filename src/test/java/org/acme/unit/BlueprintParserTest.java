

package org.acme.unit;

import org.acme.util.BlueprintParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


/*
testExtractSimplePlaceholders()

Creates a temporary XML file with simple placeholders ({{key1}}, {{key2}})

Copies it to test resources directory

Verifies the parser correctly extracts these placeholders

Checks the resulting map contains both keys with null values

Tests basic placeholder extraction from XML routes

testExtractNestedPlaceholder()

Directly tests the pattern extraction method

Verifies nested placeholders ({{base_{{env}}}}) are properly parsed

Checks the base key ("base_") maps to the inner key ("env")

Tests complex placeholder patterns
 */


class BlueprintParserTest {
    @Test
    void testExtractSimplePlaceholders() throws Exception {
        String xml = """
        <blueprint xmlns="http://camel.apache.org/schema/blueprint">
            <route>
                <from uri="http://host/{{key1}}" />
                <to uri="http://ankit/{{key2}}" />
            </route>
        </blueprint>
        """;

        Path tempFile = Files.createTempFile("test-blueprint", ".xml");
        Files.writeString(tempFile, xml);

        try {
            // Write the file to a location in the classpath (like target/test-classes)
            Path testResources = Path.of("target", "test-classes");
            Files.createDirectories(testResources);
            Path testFile = testResources.resolve("test-blueprint.xml");
            Files.copy(tempFile, testFile, StandardCopyOption.REPLACE_EXISTING);

            // Now load it from classpath
            Map<String, String> patterns = BlueprintParser.extractKeyPatternsFromBlueprint("test-blueprint.xml");

            assertEquals(2, patterns.size());
            assertTrue(patterns.containsKey("key1"));
            assertTrue(patterns.containsKey("key2"));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testExtractNestedPlaceholder() {
        Map<String, String> patterns = new HashMap<>();
        Set<String> simpleKeys = new HashSet<>();

        BlueprintParser.extractPlaceholderPatterns("http://host/{{base_{{env}}}}", patterns, simpleKeys);

        assertTrue(patterns.containsKey("base_"));
        assertEquals("env", patterns.get("base_"));
    }
}
