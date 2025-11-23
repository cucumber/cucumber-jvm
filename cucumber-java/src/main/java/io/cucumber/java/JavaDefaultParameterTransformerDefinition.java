package io.cucumber.java;

import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import static io.cucumber.java.InvalidMethodSignatureException.builder;

class JavaDefaultParameterTransformerDefinition extends AbstractGlueDefinition
        implements DefaultParameterTransformerDefinition {

    private final Lookup lookup;

    JavaDefaultParameterTransformerDefinition(Method method, Lookup lookup) {
        super(requireValidMethod(method), lookup);
        this.lookup = lookup;
    }

    private static Method requireValidMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Void.class.equals(returnType) || void.class.equals(returnType)) {
            throw createInvalidSignatureException(method);
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if ((parameterTypes.length != 2) || (parameterTypes.length != 3)) {
            throw createInvalidSignatureException(method);
        }

        if (!(Object.class.equals(parameterTypes[0]) || String.class.equals(parameterTypes[0]))) {
            throw createInvalidSignatureException(method);
        }

        if (!Type.class.equals(parameterTypes[1])) {
            throw createInvalidSignatureException(method);
        }

        if ((parameterTypes.length == 3) && !Locale.class.equals(parameterTypes[2])) {
            throw createInvalidSignatureException(method);
        }
        return method;
    }

    private Object execute(String fromValue, Type toValueType, Locale locale) {
        if (method.getParameterTypes().length == 2) {
            return Invoker.invoke(this, lookup.getInstance(method.getDeclaringClass()), method, fromValue, toValueType);
        } else {
            return Invoker.invoke(this, lookup.getInstance(method.getDeclaringClass()), method, fromValue, toValueType,
                locale);
        }
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
        return this.parameterByTypeTransformer(null);
    }

    @Override
    public ParameterByTypeTransformer parameterByTypeTransformer(Locale locale) {
        return (fromValue, toValueType) -> execute(fromValue, toValueType, locale);
    }

}
