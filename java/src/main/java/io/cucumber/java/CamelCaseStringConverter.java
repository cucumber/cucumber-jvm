package io.cucumber.java;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

class CamelCaseStringConverter {
    private static final String WHITESPACE = " ";
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    Map<String, String> toCamelCase(Map<String, String> fromValue) {
        Map<String, String> newMap = new HashMap<>();
        CamelCaseStringConverter converter = new CamelCaseStringConverter();
        for (Map.Entry<String, String> entry : fromValue.entrySet()) {
            newMap.put(converter.toCamelCase(entry.getKey()), entry.getValue());
        }
        return newMap;
    }

    private String toCamelCase(String string) {
        String[] parts = normalizeSpace(string).split(WHITESPACE);
        parts[0] = uncapitalize(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            parts[i] = capitalize(parts[i]);
        }
        return join(parts);
    }

    private String join(String[] parts) {
        StringBuilder sb = new StringBuilder();
        for (String s : parts) {
            sb.append(s);
        }
        return sb.toString();
    }

    private String normalizeSpace(String originalHeaderName) {
        return WHITESPACE_PATTERN.matcher(originalHeaderName.trim()).replaceAll(WHITESPACE);
    }

    private String capitalize(String string) {
        return Character.toTitleCase(string.charAt(0)) + string.substring(1);
    }

    private String uncapitalize(String string) {
        return Character.toLowerCase(string.charAt(0)) + string.substring(1);
    }
}
