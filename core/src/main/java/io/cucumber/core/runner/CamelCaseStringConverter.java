package io.cucumber.core.runner;

import io.cucumber.core.exception.CucumberException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

class CamelCaseStringConverter {

    private static final String WHITESPACE = " ";
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    Map<String, String> toCamelCase(Map<String, String> fromValue) {
        // First we create a map from converted keys to unconverted keys
        // This will allow us to spot duplicate keys and inform the user
        // exactly which key caused the problem.
        Map<String, String> map = new HashMap<>();
        fromValue.keySet().forEach(key -> {
            String newKey = toCamelCase(key);
            String conflictingKey = map.get(newKey);
            if (conflictingKey != null) {
                throw createDuplicateKeyException(key, conflictingKey, newKey);
            }
            map.put(newKey, key);
        });

        // Then once we have a unique mapping from converted keys to unconverted
        // keys we replace the unconverted keys with the value associated with
        // with that key
        map.replaceAll((newKey, oldKey) -> fromValue.get(oldKey));
        return map;
    }

    private static String toCamelCase(String string) {
        String[] parts = normalizeSpace(string).split(WHITESPACE);
        parts[0] = uncapitalize(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            parts[i] = capitalize(parts[i]);
        }
        return join(parts);
    }

    private static CucumberException createDuplicateKeyException(String key, String conflictingKey, String newKey) {
        return new CucumberException(String.format(
            "Failed to convert header '%s' to property name. '%s' also converted to '%s'",
            key, conflictingKey, newKey));
    }

    private static String normalizeSpace(String originalHeaderName) {
        return WHITESPACE_PATTERN.matcher(originalHeaderName.trim()).replaceAll(WHITESPACE);
    }

    private static String uncapitalize(String string) {
        return Character.toLowerCase(string.charAt(0)) + string.substring(1);
    }

    private static String capitalize(String string) {
        return Character.toTitleCase(string.charAt(0)) + string.substring(1);
    }

    private static String join(String[] parts) {
        StringBuilder sb = new StringBuilder();
        for (String s : parts) {
            sb.append(s);
        }
        return sb.toString();
    }

}
