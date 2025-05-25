package org.acme.unit;

import org.acme.util.ConfigMapGenerator;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
/*
Tests YAML ConfigMap generation:

testGenerateConfigMapAsString()

Creates sample properties and patterns

Verifies the generated YAML contains:

The key-value pair

Required Kubernetes fields

Tests basic YAML generation

testEscapeYamlValueWithSpecialChars()

Tests escaping of special characters in YAML values

Verifies colons and hashes are properly quoted

Ensures safe YAML output formatting

testEscapeYamlValueEmpty()

Tests handling of empty values

Verifies empty strings become quoted "null"

Ensures proper YAML syntax for empty values
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

//    @Test
//    void testEscapeYamlValueWithSpecialChars() {
//        String input = "val:with:special#chars";
//        String escaped = ConfigMapGenerator.escapeYamlValue(input);
//
//        assertEquals("\"val:with:special#chars\"", escaped);
//    }
//
//    @Test
//    void testEscapeYamlValueEmpty() {
//        String escaped = ConfigMapGenerator.escapeYamlValue("");
//        assertEquals("\"null\"", escaped);
//    }
}


