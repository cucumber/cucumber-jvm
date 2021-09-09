package io.cucumber.java8;

import io.cucumber.datatable.DataTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.cucumber.datatable.DataTable.create;
import static java.util.stream.Collectors.toList;

class AbstractDatatableElementTransformerDefinition extends AbstractGlueDefinition {

    private final String[] emptyPatterns;

    AbstractDatatableElementTransformerDefinition(Object body, StackTraceElement location, String[] emptyPatterns) {
        super(body, location);
        this.emptyPatterns = emptyPatterns;
    }

    DataTable replaceEmptyPatternsWithEmptyString(DataTable table) {
        List<List<String>> rawWithEmptyStrings = table.cells().stream()
                .map(this::replaceEmptyPatternsWithEmptyString)
                .collect(toList());

        return create(rawWithEmptyStrings, table.getTableConverter());
    }

    List<String> replaceEmptyPatternsWithEmptyString(List<String> row) {
        return row.stream()
                .map(this::replaceEmptyPatternsWithEmptyString)
                .collect(toList());
    }

    String replaceEmptyPatternsWithEmptyString(String t) {
        for (String emptyPattern : emptyPatterns) {
            if (emptyPattern.equals(t)) {
                return "";
            }
        }
        return t;
    }

    Map<String, String> replaceEmptyPatternsWithEmptyString(Map<String, String> fromValue) {
        Map<String, String> replacement = new LinkedHashMap<>();

        fromValue.forEach((String key, String value) -> {
            String potentiallyEmptyKey = replaceEmptyPatternsWithEmptyString(key);
            String potentiallyEmptyValue = replaceEmptyPatternsWithEmptyString(value);

            if (replacement.containsKey(potentiallyEmptyKey)) {
                throw createDuplicateKeyAfterReplacement(fromValue);
            }
            replacement.put(potentiallyEmptyKey, potentiallyEmptyValue);
        });

        return replacement;
    }

    private IllegalArgumentException createDuplicateKeyAfterReplacement(Map<String, String> fromValue) {
        List<String> conflict = new ArrayList<>(2);
        for (String emptyPattern : emptyPatterns) {
            if (fromValue.containsKey(emptyPattern)) {
                conflict.add(emptyPattern);
            }
        }
        String msg = "After replacing %s and %s with empty strings the datatable entry contains duplicate keys: %s";
        return new IllegalArgumentException(String.format(msg, conflict.get(0), conflict.get(1), fromValue));
    }

}
