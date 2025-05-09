package org.acme.util;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class ConfigValidator {
    // Make these methods public so they can be accessed by ConfigMapGenerator
    public static boolean isKeyValid(String key, Map<String, String> keyPatterns, Properties props) {
        if (keyPatterns.containsKey(key)) return true;

        for (Map.Entry<String, String> entry : keyPatterns.entrySet()) {
            String baseKey = entry.getKey();
            String innerKey = entry.getValue();

            if (innerKey != null && key.startsWith(baseKey)) {
                String expectedSuffix = props.getProperty(innerKey);
                if (expectedSuffix == null) {
                    System.err.println("Warning: Cannot validate '" + key + "' - missing '" + innerKey + "' value");
                    return true;
                }
                if (key.equals(baseKey + expectedSuffix)) {
                    return true;
                } else {
                    System.err.println("Warning: Malformed key '" + key + "' - expected suffix '" + expectedSuffix + "'");
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

    public static Properties readAndValidateDummyConfig(String filename, Map<String, String> keyPatterns) throws IOException {
        Properties props = new Properties();
        InputStream input = null;

        // First try as filesystem path
        File file = new File(filename);
        if (file.exists()) {
            input = new FileInputStream(file);
        }
        // Then try classpath resource
        else {
            input = ConfigValidator.class.getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                throw new FileNotFoundException("Config file not found in filesystem or classpath: " + filename);
            }
        }

        try (InputStream is = input) {
            props.load(is);
        }

        // Rest of your validation logic...
        Set<String> dummyKeys = props.stringPropertyNames();
        for (String key : dummyKeys) {
            if (!isKeyValid(key, keyPatterns, props)) {
                System.out.println("Info: Ignoring key '" + key + "' as it's not part of any endpoint definition");
            }
        }

        for (Map.Entry<String, String> entry : keyPatterns.entrySet()) {
            String baseKey = entry.getKey();
            String innerKey = entry.getValue();

            if (innerKey == null) {
                if (!props.containsKey(baseKey)) {
                    System.err.println("Warning: Missing required key '" + baseKey + "'");
                    props.setProperty(baseKey, "null");
                } else if (props.getProperty(baseKey).trim().isEmpty()) {
                    System.err.println("Warning: Empty value for key '" + baseKey + "', setting to 'null'");
                    props.setProperty(baseKey, "null");
                }
            } else {
                boolean hasValidResolved = props.stringPropertyNames().stream()
                        .anyMatch(k -> isValidResolvedKey(k, baseKey, innerKey, props));

                if (!hasValidResolved) {
                    System.err.println("Warning: Missing valid resolved key for pattern '" + baseKey + "{{" + innerKey + "}}'");
                    String resolvedKey = baseKey + props.getProperty(innerKey, "default");
                    props.setProperty(resolvedKey, "null");
                }
            }
        }

        return props;
    }
}



/*
Purpose: Validates configuration properties against blueprint patterns

Example Config (sample-config.properties):

properties
api_version=v1
env=prod
app=inventory
Validation Process:

Checks all keys match expected patterns from blueprint

For nested patterns (env_ + app), verifies combined key exists (prod_inventory)

Adds missing required keys with "null" values

Replaces empty values with "null"

Output Properties:

properties
api_version=v1
env=prod
app=inventory
prod_inventory=null  // Auto-added missing resolved key

 */