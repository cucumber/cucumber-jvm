package io.cucumber.java;

import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.cucumberexpressions.LocaleParameterByTypeTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import static io.cucumber.java.InvalidMethodSignatureException.builder;

class JavaDefaultParameterTransformerDefinition extends AbstractGlueDefinition
        implements DefaultParameterTransformerDefinition {

    private final Lookup lookup;
    private final LocaleParameterByTypeTransformer transformer;

    JavaDefaultParameterTransformerDefinition(Method method, Lookup lookup) {
        super(requireValidMethod(method), lookup);
        this.lookup = lookup;
        this.transformer = new LocaleParameterByTypeTransformer() {
            @Override
            public Object transform(String fromValue, Type toValueType) throws Throwable {
                return this.transform(fromValue, toValueType, null);
            }

            @Override
            public Object transform(String fromValue, Type toValueType, Locale locale) throws Throwable {
                return JavaDefaultParameterTransformerDefinition.this.execute(fromValue, toValueType, locale);
            }
        };
    }

    private static Method requireValidMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Void.class.equals(returnType) || void.class.equals(returnType)) {
            throw createInvalidSignatureException(method);
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if ((parameterTypes.length != 2) && (parameterTypes.length != 3)) {
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
        Object[] args;
        if (method.getParameterTypes().length == 3) {
            args = new Object[] { fromValue, toValueType, locale };
        } else {
            args = new Object[] { fromValue, toValueType };
        }
        return invokeMethod(args);
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(Method method) {
        return builder(method)
                .addAnnotation(DefaultParameterTransformer.class)
                .addSignature("public Object defaultDataTableEntry(String fromValue, Type toValueType)")
                .addSignature("public Object defaultDataTableEntry(Object fromValue, Type toValueType)")
                .addSignature("public Object defaultDataTableEntry(String fromValue, Type toValueType, Locale locale)")
                .addSignature("public Object defaultDataTableEntry(Object fromValue, Type toValueType, Locale locale)")
                .build();
    }

    @Override
    public LocaleParameterByTypeTransformer parameterByTypeTransformer() {
        return transformer;
    }

}
