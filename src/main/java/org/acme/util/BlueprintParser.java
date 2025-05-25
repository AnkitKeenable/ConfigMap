package org.acme.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.*;

public class BlueprintParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlueprintParser.class);

    private static final Set<String> IGNORED_PROTOCOLS = Set.of("jdbc", "cxfrs", "activemq");

    public static Map<String, String> extractKeyPatternsFromBlueprint(String xmlFile) throws Exception {
        Map<String, String> patterns = new HashMap<>();
        Set<String> simpleKeys = new HashSet<>();

        InputStream is = BlueprintParser.class.getClassLoader().getResourceAsStream(xmlFile);
        if (is == null) {
            LOGGER.error("Blueprint file not found in classpath: {}", xmlFile);
            throw new FileNotFoundException("Blueprint file not found in classpath: " + xmlFile);
        }

        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(is);

            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList endpoints = (NodeList) xpath.evaluate("//from/@uri|//to/@uri", doc, XPathConstants.NODESET);

            for (int i = 0; i < endpoints.getLength(); i++) {
                String uri = endpoints.item(i).getNodeValue();
                Matcher protocolMatcher = Pattern.compile("^([a-z]+):", Pattern.CASE_INSENSITIVE).matcher(uri);

                if (!protocolMatcher.find() || !IGNORED_PROTOCOLS.contains(protocolMatcher.group(1).toLowerCase())) {
                    extractPlaceholderPatterns(uri, patterns, simpleKeys);
                }
            }

            for (String key : simpleKeys) {
                patterns.put(key, null);
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing blueprint XML '{}': {}", xmlFile, e.getMessage(), e);
            throw e;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                LOGGER.warn("Failed to close input stream for blueprint XML '{}'", xmlFile, e);
            }
        }

        LOGGER.info("Extracted {} key patterns from blueprint '{}'", patterns.size(), xmlFile);
        return patterns;
    }

    public static void extractPlaceholderPatterns(String text, Map<String, String> patterns, Set<String> simpleKeys) {
        Matcher simpleMatcher = Pattern.compile("\\{\\{([^{}]+)\\}\\}").matcher(text);
        while (simpleMatcher.find()) {
            simpleKeys.add(simpleMatcher.group(1));
        }

        Matcher nestedMatcher = Pattern.compile("\\{\\{(.*?\\{\\{.*?\\}\\}.*?)\\}\\}").matcher(text);
        while (nestedMatcher.find()) {
            String fullPattern = nestedMatcher.group(1);
            String baseKey = fullPattern.substring(0, fullPattern.indexOf("{{"));
            String innerKey = fullPattern.substring(fullPattern.indexOf("{{") + 2, fullPattern.indexOf("}}"));
            patterns.put(baseKey, innerKey);
        }
    }

    public static Map<String, String> extractKeyPatternsFromStream(InputStream is) throws Exception {
        Map<String, String> patterns = new HashMap<>();
        Set<String> simpleKeys = new HashSet<>();

        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(is);

            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList endpoints = (NodeList) xpath.evaluate("//from/@uri|//to/@uri", doc, XPathConstants.NODESET);

            for (int i = 0; i < endpoints.getLength(); i++) {
                String uri = endpoints.item(i).getNodeValue();
                Matcher protocolMatcher = Pattern.compile("^([a-z]+):", Pattern.CASE_INSENSITIVE).matcher(uri);

                if (!protocolMatcher.find() || !IGNORED_PROTOCOLS.contains(protocolMatcher.group(1).toLowerCase())) {
                    extractPlaceholderPatterns(uri, patterns, simpleKeys);
                }
            }

            for (String key : simpleKeys) {
                patterns.put(key, null);
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing blueprint XML from stream: {}", e.getMessage(), e);
            throw e;
        }
        LOGGER.info("Extracted {} key patterns from blueprint stream", patterns.size());
        return patterns;
    }
}
