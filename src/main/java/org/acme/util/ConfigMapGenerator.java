package org.acme.util;

import java.io.*;
import java.util.Properties;
import java.util.Map;

public class ConfigMapGenerator {



    public static String generateConfigMapAsString(Properties config, Map<String, String> keyPatterns) {
        StringWriter stringWriter = new StringWriter();
        try (BufferedWriter writer = new BufferedWriter(stringWriter)) {
            writeConfigMapContent(config, keyPatterns, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private static void writeConfigMapContent(Properties config, Map<String, String> keyPatterns, BufferedWriter writer)
            throws IOException {
        writer.write("apiVersion: v1\n");
        writer.write("kind: ConfigMap\n");
        writer.write("metadata:\n");
        writer.write("  name: app-config\n");
        writer.write("  labels:\n");
        writer.write("    generated-by: strict-configmap-generator\n");
        writer.write("data:\n");

        config.stringPropertyNames().stream()
                .filter(key -> ConfigValidator.isKeyValid(key, keyPatterns, config))
                .sorted()
                .forEach(key -> {
                    try {
                        String value = config.getProperty(key);
                        writer.write(String.format("  %s: %s\n", key, escapeYamlValue(value)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static String escapeYamlValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "\"null\"";
        }
        if (value.matches(".*[:{}\\[\\]\\*\\?#|].*")) {
            return "\"" + value.replace("\"", "\\\"") + "\"";
        }
        return value;
    }
}


/*
Purpose: Generates Kubernetes ConfigMap YAML from validated properties

Input:

Properties from above

Patterns from blueprint parser

Output YAML:

yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  labels:
    generated-by: strict-configmap-generator
data:
  api_version: v1
  app: inventory
  env: prod
  prod_inventory: "null"
Special Handling:

Escapes special characters in values

Quotes null/empty values

Maintains YAML syntax


 */