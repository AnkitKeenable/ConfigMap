package org.acme.unit;

import org.acme.util.BlueprintParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/*
In the Below I explain each testcase working

testExtractSimplePlaceholders()
texts extraction of simple placeholder pattern from a XML file

testExtractNestedPlaceholder()
Tests extraction of nested placeholder patterns (like {{base_{{env}}}}).



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
        File tempFile = Files.writeString(Files.createTempFile("blueprint", ".xml"), xml).toFile();

        Map<String, String> patterns = BlueprintParser.extractKeyPatternsFromBlueprint(tempFile.getAbsolutePath());

        assertEquals(2, patterns.size());
        assertTrue(patterns.containsKey("key1"));
        assertTrue(patterns.containsKey("key2"));
        assertNull(patterns.get("simpleKey"));
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





