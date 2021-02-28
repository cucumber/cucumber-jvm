package io.cucumber.java;

import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static io.cucumber.java.InvalidMethodSignatureException.builder;

class JavaDefaultParameterTransformerDefinition extends AbstractGlueDefinition
        implements DefaultParameterTransformerDefinition {

    private final Lookup lookup;
    private final ParameterByTypeTransformer transformer;

    JavaDefaultParameterTransformerDefinition(Method method, Lookup lookup) {
        super(requireValidMethod(method), lookup);
        this.lookup = lookup;
        this.transformer = this::execute;
    }

    private static Method requireValidMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Void.class.equals(returnType) || void.class.equals(returnType)) {
            throw createInvalidSignatureException(method);
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 2) {
            throw createInvalidSignatureException(method);
        }

        if (!(Object.class.equals(parameterTypes[0]) || String.class.equals(parameterTypes[0]))) {
            throw createInvalidSignatureException(method);
        }

        if (!Type.class.equals(parameterTypes[1])) {
            throw createInvalidSignatureException(method);
        }

        return method;
    }

    private Object execute(String fromValue, Type toValueType) {
        return Invoker.invoke(this, lookup.getInstance(method.getDeclaringClass()), method, fromValue, toValueType);
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(Method method) {
        return builder(method)
                .addAnnotation(DefaultParameterTransformer.class)
                .addSignature("public Object defaultDataTableEntry(String fromValue, Type toValueType)")
                .addSignature("public Object defaultDataTableEntry(Object fromValue, Type toValueType)")
                .build();
    }

    @Override
    public ParameterByTypeTransformer parameterByTypeTransformer() {
        return transformer;
    }

}
