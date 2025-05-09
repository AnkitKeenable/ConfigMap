

package org.acme.unit;

import org.acme.util.ConfigValidator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


/*
Tests configuration validation logic:

testMissingKey()

Tests handling of missing required keys

Verifies missing keys are added with "null" values

Existing keys remain unchanged

testEmptyKey()

Tests handling of empty values

Verifies empty values are normalized to "null"

Other values remain intact

testMissingResolvedKey()

Tests nested placeholder resolution

Verifies missing resolved keys are created

Checks base values are preserved

testUnusedKey()

Tests handling of extra configuration keys

Verifies unrelated keys pass through unchanged

Ensures no false validation failures

Additional Edge Case Tests:

testInvalidKeyWithMissingInnerValue: Tests invalid nested patterns

testNonExistentFile: Tests file not found handling

testKeyValidationWithMissingInnerValue: Tests validation logic

testInvalidResolvedKey: Tests pattern matching failures


 */
class ConfigValidatorTest {

    private File createTempConfigFile(String content) throws Exception {
        File tempFile = File.createTempFile("test-config", ".properties");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }

    @Test
    void testMissingKey() throws Exception {
        String cfg = "key2=value2\n";
        File file = createTempConfigFile(cfg);

        try {
            Map<String, String> keyPatterns = new HashMap<>();
            keyPatterns.put("key1", null);

            Properties props = ConfigValidator.readAndValidateDummyConfig(file.getAbsolutePath(), keyPatterns);

            assertTrue(props.containsKey("key1"));
            assertEquals("null", props.getProperty("key1"));
            assertEquals("value2", props.getProperty("key2"));
        } finally {
            file.delete();
        }
    }

    @Test
    void testEmptyKey() throws Exception {
        String cfg = "key1=\nkey2=value2\n";
        File file = createTempConfigFile(cfg);

        try {
            Map<String, String> keyPatterns = new HashMap<>();
            keyPatterns.put("key1", null);

            Properties props = ConfigValidator.readAndValidateDummyConfig(file.getAbsolutePath(), keyPatterns);

            assertEquals("null", props.getProperty("key1"));
            assertEquals("value2", props.getProperty("key2"));
        } finally {
            file.delete();
        }
    }

    @Test
    void testMissingResolvedKey() throws Exception {
        String cfg = "env=dev\nkey2=value2\n";
        File file = createTempConfigFile(cfg);

        try {
            Map<String, String> keyPatterns = new HashMap<>();
            keyPatterns.put("key_", "env");

            Properties props = ConfigValidator.readAndValidateDummyConfig(file.getAbsolutePath(), keyPatterns);

            assertEquals("dev", props.getProperty("env"));
            assertTrue(props.containsKey("key_dev"));
            assertEquals("null", props.getProperty("key_dev"));
        } finally {
            file.delete();
        }
    }

    @Test
    void testUnusedKey() throws Exception {
        String cfg = "keyX=valueX\n";
        File file = createTempConfigFile(cfg);

        try {
            Map<String, String> keyPatterns = new HashMap<>();

            Properties props = ConfigValidator.readAndValidateDummyConfig(file.getAbsolutePath(), keyPatterns);

            assertEquals("valueX", props.getProperty("keyX"));
        } finally {
            file.delete();
        }
    }



    @Test
    void testInvalidKeyWithMissingInnerValue() throws Exception {
        String cfg = "base_abc=value\n"; // Missing 'env' property
        File file = createTempConfigFile(cfg);

        try {
            Map<String, String> keyPatterns = new HashMap<>();
            keyPatterns.put("base_", "env"); // Requires env property

            Properties props = ConfigValidator.readAndValidateDummyConfig(file.getAbsolutePath(), keyPatterns);

            // Should print warning about missing inner key
            assertTrue(props.containsKey("base_abc"));
        } finally {
            file.delete();
        }
    }

    @Test
    void testNonExistentFile() {
        assertThrows(FileNotFoundException.class, () -> {
            ConfigValidator.readAndValidateDummyConfig("nonexistent.file", new HashMap<>());
        });
    }

    @Test
    void testKeyValidationWithMissingInnerValue() {
        Map<String, String> keyPatterns = new HashMap<>();
        keyPatterns.put("base_", "env");

        Properties props = new Properties();
        props.setProperty("base_test", "value");

        // Should print warning about missing 'env' property but return true
        assertTrue(ConfigValidator.isKeyValid("base_test", keyPatterns, props));
    }

    @Test
    void testInvalidResolvedKey() {
        Properties props = new Properties();
        props.setProperty("env", "dev");

        // Key doesn't match the expected pattern
        assertFalse(ConfigValidator.isValidResolvedKey("wrong_dev", "key_", "env", props));
    }
}