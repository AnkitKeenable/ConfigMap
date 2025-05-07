package org.acme.util;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.File;
import java.util.*;
import java.util.regex.*;

public class BlueprintParser {
    private static final Set<String> IGNORED_PROTOCOLS = Set.of("jdbc", "cxfrs", "activemq");

    public static Map<String, String> extractKeyPatternsFromBlueprint(String xmlFile) throws Exception {
        Map<String, String> patterns = new HashMap<>();
        Set<String> simpleKeys = new HashSet<>();

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new File(xmlFile));

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList endpoints = (NodeList) xpath.evaluate("//from/@uri|//to/@uri", doc, XPathConstants.NODESET);

        for (int i = 0; i < endpoints.getLength(); i++) {
            String uri = endpoints.item(i).getNodeValue();
            Matcher protocolMatcher = Pattern.compile("^([a-z]+):", Pattern.CASE_INSENSITIVE).matcher(uri);

            if (!protocolMatcher.find() || !IGNORED_PROTOCOLS.contains(protocolMatcher.group(1).toLowerCase())) {
                extractPlaceholderPatterns(uri, patterns, simpleKeys);
            }
        }

        // Add simple keys to patterns map with null pattern
        for (String key : simpleKeys) {
            patterns.put(key, null);
        }

        return patterns;
    }

    public static void extractPlaceholderPatterns(String text, Map<String, String> patterns, Set<String> simpleKeys) {
        // Extract simple placeholders
        Matcher simpleMatcher = Pattern.compile("\\{\\{([^{}]+)\\}\\}").matcher(text);
        while (simpleMatcher.find()) {
            simpleKeys.add(simpleMatcher.group(1));
        }

        // Extract nested placeholders
        Matcher nestedMatcher = Pattern.compile("\\{\\{(.*?\\{\\{.*?\\}\\}.*?)\\}\\}").matcher(text);
        while (nestedMatcher.find()) {
            String fullPattern = nestedMatcher.group(1);
            String baseKey = fullPattern.substring(0, fullPattern.indexOf("{{"));
            String innerKey = fullPattern.substring(fullPattern.indexOf("{{") + 2, fullPattern.indexOf("}}"));

            patterns.put(baseKey, innerKey);
        }
    }
}