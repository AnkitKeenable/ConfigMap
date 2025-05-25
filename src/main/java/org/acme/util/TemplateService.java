package org.acme.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TemplateService {
    private static final Configuration cfg;

    static {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(TemplateService.class, "/templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        try {
            cfg.setSetting("time_zone", "Asia/Kolkata");
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }

    }

    public static String processConfigMapTemplate(String templateName, Properties config, Map<String, String> keyPatterns)
            throws IOException, TemplateException {

        Map<String, Object> input = new HashMap<>();
        input.put("configMapName", "app-config");

        // Filter properties based on keyPatterns
        Map<String, String> filteredProps = new HashMap<>();
        config.stringPropertyNames().stream()
                .filter(key -> ConfigValidator.isKeyValid(key, keyPatterns, config))
                .forEach(key -> filteredProps.put(key, config.getProperty(key)));

        input.put("properties", filteredProps);

        Template template = cfg.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(input, writer);
        return writer.toString();
    }
}