package io.cucumber.java8;

import io.cucumber.datatable.DataTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.cucumber.datatable.DataTable.create;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class AbstractDatatableElementTransformerDefinition extends AbstractGlueDefinition {
    private final String[] emptyPatterns;

    AbstractDatatableElementTransformerDefinition(Object body, StackTraceElement location, String[] emptyPatterns) {
        super(body, location);
        this.emptyPatterns = emptyPatterns;
    }


    List<String> replaceEmptyPatternsWithEmptyString(List<String> row) {
        return row.stream()
            .map(this::replaceEmptyPatternsWithEmptyString)
            .collect(toList());
    }

    DataTable replaceEmptyPatternsWithEmptyString(DataTable table) {
        List<List<String>> rawWithEmptyStrings = table.cells().stream()
            .map(this::replaceEmptyPatternsWithEmptyString)
            .collect(toList());

        return create(rawWithEmptyStrings, table.getTableConverter());
    }

    Map<String, String> replaceEmptyPatternsWithEmptyString(Map<String, String> fromValue) {
        return fromValue.entrySet().stream()
            .collect(toMap(
                entry -> replaceEmptyPatternsWithEmptyString(entry.getKey()),
                entry -> replaceEmptyPatternsWithEmptyString(entry.getValue()),
                (s, s2) -> {
                    throw createDuplicateKeyAfterReplacement(fromValue);
                }
            ));
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

    String replaceEmptyPatternsWithEmptyString(String t) {
        for (String emptyPattern : emptyPatterns) {
            if (t.equals(emptyPattern)) {
                return "";
            }
        }
        return t;
    }
}
