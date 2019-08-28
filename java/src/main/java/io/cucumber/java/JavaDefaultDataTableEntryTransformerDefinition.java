package io.cucumber.java;

import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static io.cucumber.java.InvalidMethodSignatureException.builder;

class JavaDefaultDataTableEntryTransformerDefinition extends AbstractGlueDefinition implements DefaultDataTableEntryTransformerDefinition {

    private final TableEntryByTypeTransformer transformer;
    private final boolean headersConversionRequired;


    JavaDefaultDataTableEntryTransformerDefinition(Method method, Lookup lookup) {
        super(requireValidMethod(method), lookup);
        this.headersConversionRequired = false;
        this.transformer = this::execute;
    }

    JavaDefaultDataTableEntryTransformerDefinition(Method method, Lookup lookup, boolean headersConversionRequired) {
        super(requireValidMethod(method), lookup);
        this.headersConversionRequired = headersConversionRequired;
        this.transformer = this::execute;
    }

    private static Method requireValidMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Void.class.equals(returnType) || void.class.equals(returnType)) {
            throw createInvalidSignatureException(method);
        }

        Type[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        if (parameterTypes.length < 2 || parameterTypes.length > 3) {
            throw createInvalidSignatureException(method);
        }

        Type parameterType = genericParameterTypes[0];

        if (!Object.class.equals(parameterType)) {
            if (!(parameterType instanceof ParameterizedType)) {
                throw createInvalidSignatureException(method);
            }
            ParameterizedType parameterizedType = (ParameterizedType) parameterType;
            Type rawType = parameterizedType.getRawType();
            if (!Map.class.equals(rawType)) {
                throw createInvalidSignatureException(method);
            }
            Type[] typeParameters = parameterizedType.getActualTypeArguments();
            for (Type typeParameter : typeParameters) {
                if (!String.class.equals(typeParameter)) {
                    throw createInvalidSignatureException(method);
                }
            }
        }

        if (!Type.class.equals(parameterTypes[1])) {
            throw createInvalidSignatureException(method);
        }

        if (parameterTypes.length == 3) {
            if (!(Object.class.equals(parameterTypes[2]) || TableCellByTypeTransformer.class.equals(parameterTypes[2]))) {
                throw createInvalidSignatureException(method);
            }
        }

        return method;
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(Method method) {
        return builder(method)
            .addAnnotation(DefaultDataTableEntryTransformer.class)
            .addSignature("public Object defaultDataTableEntry(Map<String, String> fromValue, Type toValueType)")
            .addSignature("public Object defaultDataTableEntry(Object fromValue, Type toValueType)")
            .build();
    }

    @Override
    public TableEntryByTypeTransformer tableEntryByTypeTransformer() {
        return transformer;
    }

    private Object execute(Map<String, String> fromValue, Type toValueType, TableCellByTypeTransformer cellTransformer) throws Throwable {
        if (this.headersConversionRequired) {
            fromValue = convertToCamelCase(fromValue);
        }

        Object[] args;
        if (method.getParameterTypes().length == 3) {
            args = new Object[]{fromValue, toValueType, cellTransformer};
        } else {
            args = new Object[]{fromValue, toValueType};
        }
        return Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, args);
    }

    private Map<String, String> convertToCamelCase(Map<String, String> fromValue) {
        Map<String, String> newMap = new HashMap<>();
        CamelCaseStringConverter converter = new CamelCaseStringConverter();
        for (Map.Entry<String, String> entry : fromValue.entrySet()) {
            newMap.put(converter.toCamelCase(entry.getKey()), entry.getValue());
        }
        return newMap;
    }

    private static class CamelCaseStringConverter {
        private static final String WHITESPACE = " ";
        private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

        String toCamelCase(String string) {
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
            return Character.toTitleCase(string.charAt(0)) + string.substring(1);
        }

        private String uncapitalize(String string) {
            return Character.toLowerCase(string.charAt(0)) + string.substring(1);
        }
    }
}
