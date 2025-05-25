package org.acme.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class ConfigValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigValidator.class);

    public static boolean isKeyValid(String key, Map<String, String> keyPatterns, Properties props) {
        if (keyPatterns.containsKey(key)) return true;

        for (Map.Entry<String, String> entry : keyPatterns.entrySet()) {
            String baseKey = entry.getKey();
            String innerKey = entry.getValue();

            if (innerKey != null && key.startsWith(baseKey)) {
                String expectedSuffix = props.getProperty(innerKey);
                if (expectedSuffix == null) {
                    LOGGER.warn("Cannot validate key '{}' - missing '{}' value", key, innerKey);
                    return true;
                }
                if (key.equals(baseKey + expectedSuffix)) {
                    return true;
                } else {
                    LOGGER.warn("Malformed key '{}' - expected suffix '{}'", key, expectedSuffix);
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean isValidResolvedKey(String key, String baseKey, String innerKey, Properties props) {
        if (!key.startsWith(baseKey)) return false;

        String expectedSuffix = props.getProperty(innerKey);
        if (expectedSuffix == null) return false;

        return key.equals(baseKey + expectedSuffix);
    }

    public static Properties validateProperties(Properties props, Map<String, String> keyPatterns) {
        Set<String> dummyKeys = props.stringPropertyNames();
        for (String key : dummyKeys) {
            if (!isKeyValid(key, keyPatterns, props)) {
                LOGGER.info("Ignoring key '{}' as it's not part of any endpoint definition", key);
            }
        }

        for (Map.Entry<String, String> entry : keyPatterns.entrySet()) {
            String baseKey = entry.getKey();
            String innerKey = entry.getValue();

            if (innerKey == null) {
                if (!props.containsKey(baseKey)) {
                    LOGGER.warn("Missing required key '{}'", baseKey);
                    props.setProperty(baseKey, "null");
                } else if (props.getProperty(baseKey).trim().isEmpty()) {
                    LOGGER.warn("Empty value for key '{}', setting to 'null'", baseKey);
                    props.setProperty(baseKey, "null");
                }
            } else {
                boolean hasValidResolved = props.stringPropertyNames().stream()
                        .anyMatch(k -> isValidResolvedKey(k, baseKey, innerKey, props));

                if (!hasValidResolved) {
                    LOGGER.warn("Missing valid resolved key for pattern '{}{{{}}}'", baseKey, innerKey);
                    String resolvedKey = baseKey + props.getProperty(innerKey, "default");
                    props.setProperty(resolvedKey, "null");
                }
            }
        }

        return props;
    }

    public static Properties readAndValidateDummyConfig(String filename, Map<String, String> keyPatterns) throws IOException {
        Properties props = new Properties();
        InputStream input = null;

        File file = new File(filename);
        if (file.exists()) {
            input = new FileInputStream(file);
            LOGGER.info("Reading config from filesystem: {}", filename);
        } else {
            input = ConfigValidator.class.getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                LOGGER.error("Config file not found in filesystem or classpath: {}", filename);
                throw new FileNotFoundException("Config file not found in filesystem or classpath: " + filename);
            }
            LOGGER.info("Reading config from classpath resource: {}", filename);
        }

        try (InputStream is = input) {
            props.load(is);
        }

        // Validation as before
        return validateProperties(props, keyPatterns);
    }
}
