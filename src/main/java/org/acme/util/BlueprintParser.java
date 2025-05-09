package org.acme.util;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.*;

public class BlueprintParser {
    private static final Set<String> IGNORED_PROTOCOLS = Set.of("jdbc", "cxfrs", "activemq");

    public static Map<String, String> extractKeyPatternsFromBlueprint(String xmlFile) throws Exception {
        Map<String, String> patterns = new HashMap<>();
        Set<String> simpleKeys = new HashSet<>();

        // Use classloader to get resource as stream
        InputStream is = BlueprintParser.class.getClassLoader().getResourceAsStream(xmlFile);
        if (is == null) {
            throw new FileNotFoundException("Blueprint file not found in classpath: " + xmlFile);
        }

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



/*
Purpose: Extracts placeholder patterns from Camel blueprint XML files

Example XML (sample-blueprint.xml):

<blueprint xmlns="http://camel.apache.org/schema/blueprint">
  <route>
    <from uri="http://service/{{api_version}}/users"/>
    <to uri="jdbc:{{db_name}}"/> <!-- Ignored protocol -->
    <to uri="http://logs/{{env}}_{{app}}/details"/>
  </route>
</blueprint>

How it works:

Parses XML and extracts all from/to URI attributes

Filters out URIs with ignored protocols (jdbc, cxfrs, activemq)

Extracts patterns:

Simple placeholders: {{api_version}} → api_version

Nested placeholders: {{env}}_{{app}} → maps env_ to app

Output:

java
{
  "api_version": null,       // Simple placeholder
  "env_": "app",            // Nested pattern
  "db_name": null           // From ignored protocol (would be filtered out)
}
 */