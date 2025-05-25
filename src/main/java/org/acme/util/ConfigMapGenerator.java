package org.acme.util;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class ConfigMapGenerator {
    public static String generateConfigMapAsString(Properties config, Map<String, String> keyPatterns) {
        try {
            return TemplateService.processConfigMapTemplate("configmap.ftl", config, keyPatterns);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ConfigMap from template", e);
        }
    }
}