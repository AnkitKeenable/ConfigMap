package org.acme.unit;

import org.acme.util.ConfigValidator;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/*

testValidSimpleKey()
Tests validation of simple property keys.
patterns: {"simpleKey": ""}
props: {simpleKey=value}
 Returns true (valid)


testInvalidKeyNotInPatterns()
Tests detection of unknown property keys.
patterns: {"expectedKey": ""}
props: {unknownKey=value}


testResolvedKeyValidationSuccess()
Tests successful validation of resolved nested keys.
patterns: {"url_": "env"}
props: {env=dev, url_dev=http://localhost}


testReadAndValidateDummyConfigMissingKey()
 Tests handling of missing required keys in config files.
File content: anotherKey=123
patterns: {"requiredKey": null}

 */

class ConfigValidatorTest {

    @Test
    void testValidSimpleKey() {
        Map<String, String> patterns = Map.of("simpleKey", "");  // Use empty string instead of null
        Properties props = new Properties();
        props.setProperty("simpleKey", "value");

        assertTrue(ConfigValidator.isKeyValid("simpleKey", patterns, props));
    }

    @Test
    void testInvalidKeyNotInPatterns() {
        Map<String, String> patterns = Map.of("expectedKey", "");  // Explicitly define known keys
        Properties props = new Properties();
        props.setProperty("unknownKey", "value");

        assertFalse(ConfigValidator.isKeyValid("unknownKey", patterns, props));
    }

    @Test
    void testResolvedKeyValidationSuccess() {
        Map<String, String> patterns = Map.of("url_", "env");
        Properties props = new Properties();
        props.setProperty("env", "dev");
        props.setProperty("url_dev", "http://localhost");

        assertTrue(ConfigValidator.isKeyValid("url_dev", patterns, props));
    }

    @Test
    void testResolvedKeyValidationFailure() {
        Map<String, String> patterns = Map.of("url_", "env");
        Properties props = new Properties();
        props.setProperty("env", "prod");
        props.setProperty("url_test", "wrong value");

        assertFalse(ConfigValidator.isKeyValid("url_test", patterns, props));
    }
    @Test
    void testKeyValidationWhenSuffixIsMissing() {
        Map<String, String> patterns = Map.of("url_", "env");
        Properties props = new Properties(); // Missing 'env'

        assertTrue(ConfigValidator.isKeyValid("url_test", patterns, props)); // Will warn, but return true
    }

    @Test
    void testIsValidResolvedKeyWithMissingSuffix() {
        Map<String, String> patterns = Map.of("url_", "env");
        Properties props = new Properties(); // Missing 'env'

        assertFalse(ConfigValidator.isValidResolvedKey("url_dev", "url_", "env", props));
    }

    @Test
    void testReadAndValidateDummyConfigMissingKey() throws IOException {
        File temp = File.createTempFile("dummy", ".cfg");
        Files.writeString(temp.toPath(), """
        anotherKey=123
    """);

        Map<String, String> patterns = new HashMap<>();
        patterns.put("requiredKey", null); // Map.of doesn't support null

        Properties props = ConfigValidator.readAndValidateDummyConfig(temp.getAbsolutePath(), patterns);

        assertEquals("null", props.getProperty("requiredKey")); // Missing key should be added as "null"
        temp.deleteOnExit();
    }

    @Test
    void testReadAndValidateDummyConfigEmptyKey() throws IOException {
        File temp = File.createTempFile("dummy", ".cfg");
        Files.writeString(temp.toPath(), """
        requiredKey=
    """);

        Map<String, String> patterns = new HashMap<>();
        patterns.put("requiredKey", null); // Map.of doesn't support null

        Properties props = ConfigValidator.readAndValidateDummyConfig(temp.getAbsolutePath(), patterns);

        assertEquals("null", props.getProperty("requiredKey")); // Empty value replaced with "null"
        temp.deleteOnExit();
    }

    @Test
    void testReadAndValidateDummyConfigMissingResolvedKey() throws IOException {
        File temp = File.createTempFile("dummy", ".cfg");
        Files.writeString(temp.toPath(), """
            env=prod
        """);

        Map<String, String> patterns = Map.of(
                "url_", "env"
        );

        Properties props = ConfigValidator.readAndValidateDummyConfig(temp.getAbsolutePath(), patterns);

        assertEquals("null", props.getProperty("url_prod")); // auto-added resolved key
        temp.deleteOnExit();
    }

}
