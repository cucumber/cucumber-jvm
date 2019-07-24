package io.cucumber.java;

import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.datatable.TableCellByTypeTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static io.cucumber.java.InvalidMethodSignatureExceptionBuilder.builder;

class JavaDefaultDataTableCellTransformerDefinition extends AbstractGlueDefinition implements DefaultDataTableCellTransformerDefinition {


    private final Lookup lookup;
    private final TableCellByTypeTransformer transformer;

    JavaDefaultDataTableCellTransformerDefinition(Method method, Lookup lookup) {
        super(requireValidMethod(method));
        this.lookup = lookup;
        this.transformer = this::execute;
    }

    private static Method requireValidMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Void.class.equals(returnType)) {
            throw createInvalidSignatureException(method);
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 2) {
            throw createInvalidSignatureException(method);
        }

        if (!(Object.class.equals(parameterTypes[0]) || String.class.equals(parameterTypes[0]))) {
            throw createInvalidSignatureException(method);
        }

        if (!(Object.class.equals(parameterTypes[1]) || Type.class.equals(parameterTypes[1]))) {
            throw createInvalidSignatureException(method);
        }

        return method;
    }

    private static CucumberException createInvalidSignatureException(Method method) {
        return builder(method)
            .addAnnotation(DefaultDataTableCellTransformer.class)
            .addSignature("public Object defaultDataTableCell(String fromValue, Type toValueType)")
            .addSignature("public Object defaultDataTableCell(Object fromValue, Type toValueType)")
            .build();
    }

    @Override
    public TableCellByTypeTransformer tableCellByTypeTransformer() {
        return transformer;
    }


    @SuppressWarnings("unchecked")
    private <T> T execute(String fromValue, Class<T> toValueType) throws Throwable {
        return (T) Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, 0, fromValue, toValueType);
    }

}
