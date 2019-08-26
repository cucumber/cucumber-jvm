package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class JavaDefaultDataTableCapitalCaseTransformerDefinition extends JavaDefaultDataTableEntryTransformerDefinition {
    JavaDefaultDataTableCapitalCaseTransformerDefinition(Method method, Lookup lookup) {
        super(method, lookup);
    }

    @Override
    public TableEntryByTypeTransformer tableEntryByTypeTransformer() {
        return super.tableEntryByTypeTransformer();
    }

    @Override
    Object execute(Map<String, String> fromValue, Type toValueType, TableCellByTypeTransformer cellTransformer) throws Throwable {
        Object[] args;
        if (method.getParameterTypes().length == 3) {
            args = new Object[]{fromValue, toValueType, cellTransformer};
        } else {
            args = new Object[]{fromValue, toValueType};
        }
        args[0] = convertToCamelCase((Map<String, Object>)args[0]);
        return Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, args);
    }

    private Map<String, Object> convertToCamelCase(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<>();
        CamelCaseStringConverter converter = new CamelCaseStringConverter();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            newMap.put(converter.map(entry.getKey()), entry.getValue());
        }
        return newMap;
    }

    static class CamelCaseStringConverter {
        private static final String WHITESPACE = " ";
        private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

        public String map(String string) {
            String[] splitted = normalizeSpace(string).split(WHITESPACE);
            splitted[0] = uncapitalize(splitted[0]);
            for (int i = 1; i < splitted.length; i++) {
                splitted[i] = capitalize(splitted[i]);
            }
            return join(splitted);
        }

        private String join(String[] splitted) {
            StringBuilder sb = new StringBuilder();
            for (String s : splitted) {
                sb.append(s);
            }
            return sb.toString();
        }

        private String normalizeSpace(String originalHeaderName) {
            return WHITESPACE_PATTERN.matcher(originalHeaderName.trim()).replaceAll(WHITESPACE);
        }

        private String capitalize(String string) {
            return new StringBuilder(string.length()).append(Character.toTitleCase(string.charAt(0))).append(string.substring(1)).toString();
        }

        private String uncapitalize(String string) {
            return new StringBuilder(string.length()).append(Character.toLowerCase(string.charAt(0))).append(string.substring(1)).toString();
        }
    }
}
