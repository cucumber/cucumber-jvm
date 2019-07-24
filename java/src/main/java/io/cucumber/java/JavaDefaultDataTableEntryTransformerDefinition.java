package io.cucumber.java;

import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import static io.cucumber.java.InvalidMethodSignatureExceptionBuilder.builder;

class JavaDefaultDataTableEntryTransformerDefinition extends AbstractGlueDefinition implements DefaultDataTableEntryTransformerDefinition {

    private final Lookup lookup;
    private final TableEntryByTypeTransformer transformer;

    JavaDefaultDataTableEntryTransformerDefinition(Method method, Lookup lookup) {
        super(requireValidMethod(method));
        this.lookup = lookup;
        this.transformer = this::execute;
    }

    private static Method requireValidMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Void.class.equals(returnType) || void.class.equals(returnType)) {
            throw createInvalidSignatureException(method);
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length < 2 || parameterTypes.length > 3) {
            throw createInvalidSignatureException(method);
        }

        if (!(Object.class.equals(parameterTypes[0]) || Map.class.equals(parameterTypes[0]))) {
            throw createInvalidSignatureException(method);
        }

        if (!(Object.class.equals(parameterTypes[1]) || Type.class.equals(parameterTypes[1]) || Class.class.equals(parameterTypes[1]))) {
            throw createInvalidSignatureException(method);
        }

        if (parameterTypes.length == 3) {
            if (!(Object.class.equals(parameterTypes[2]) || TableCellByTypeTransformer.class.equals(parameterTypes[2]))) {
                throw createInvalidSignatureException(method);
            }
        }

        return method;
    }

    private static CucumberException createInvalidSignatureException(Method method) {
        return builder(method)
            .addAnnotation(DefaultDataTableEntryTransformer.class)
            .addSignature("public T defaultDataTableEntry(Map<String, String> fromValue, Class<T> toValueType)")
            .addSignature("public T defaultDataTableCell(Map<String, String> fromValue, Class<T> toValueType, TableCellByTypeTransformer cellTransformer)")
            .build();
    }

    @Override
    public TableEntryByTypeTransformer tableEntryByTypeTransformer() {
        return transformer;
    }

    @SuppressWarnings("unchecked")
    private <T> T execute(Map<String, String> fromValue, Class<T> toValueType, TableCellByTypeTransformer cellTransformer) throws Throwable {
        Object[] args;
        if (method.getParameterTypes().length == 3) {
            args = new Object[]{fromValue, toValueType, cellTransformer};
        } else {
            args = new Object[]{fromValue, toValueType};
        }
        return (T) Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, 0, args);
    }

}
