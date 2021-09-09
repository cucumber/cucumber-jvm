package io.cucumber.java;

import io.cucumber.core.backend.Snippet;
import io.cucumber.datatable.DataTable;

import java.lang.reflect.Type;
import java.util.Map;

import static java.util.stream.Collectors.joining;

abstract class AbstractJavaSnippet implements Snippet {

    @Override
    public final String tableHint() {
        return "" +
                "    // For automatic transformation, change DataTable to one of\n" +
                "    // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or\n" +
                "    // Map<K, List<V>>. E,K,V must be a String, Integer, Float,\n" +
                "    // Double, Byte, Short, Long, BigInteger or BigDecimal.\n" +
                "    //\n" +
                // TODO: Add doc URL
                "    // For other transformations you can register a DataTableType.\n";
    }

    @Override
    public final String arguments(Map<String, Type> arguments) {
        return arguments.entrySet()
                .stream()
                .map(argType -> getArgType(argType.getValue()) + " " + argType.getKey())
                .collect(joining(", "));
    }

    private String getArgType(Type argType) {
        if (argType instanceof Class) {
            Class<?> cType = (Class<?>) argType;
            if (cType.equals(DataTable.class)) {
                return cType.getName();
            }
            return cType.getSimpleName();
        }

        // Got a better idea? Send a PR.
        return argType.toString();
    }

    @Override
    public final String escapePattern(String pattern) {
        return pattern.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
