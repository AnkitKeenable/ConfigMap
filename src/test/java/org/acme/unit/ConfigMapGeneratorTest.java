package org.acme.unit;

import org.acme.util.ConfigMapGenerator;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
/*
In the Below I explain each testcase working

testGenerateConfigMapAsString()
Text generate from Configmap YAML from properties

patterns: {"key1": ""}

props: {key1=value1}


testEscapeYamlValueEmpty()

Tests handling of empty values in YAML generation.

 Tests handling of empty values in YAML generation.
 */
class ConfigMapGeneratorTest {

    @Test
    void testGenerateConfigMapAsString() {
        Map<String, String> patterns = Map.of("key1", "");
        Properties props = new Properties();
        props.setProperty("key1", "value1");

        String yaml = ConfigMapGenerator.generateConfigMapAsString(props, patterns);

        assertTrue(yaml.contains("key1: value1"));
        assertTrue(yaml.contains("apiVersion: v1"));
    }

    @Test
    void testEscapeYamlValueWithSpecialChars() {
        String input = "val:with:special#chars";
        String escaped = ConfigMapGenerator.escapeYamlValue(input);

        assertEquals("\"val:with:special#chars\"", escaped);
    }

    @Test
    void testEscapeYamlValueEmpty() {
        String escaped = ConfigMapGenerator.escapeYamlValue("");
        assertEquals("\"null\"", escaped);
    }
}


