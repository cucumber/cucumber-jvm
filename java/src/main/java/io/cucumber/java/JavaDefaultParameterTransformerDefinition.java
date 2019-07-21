package io.cucumber.java;

import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.reflection.MethodFormat;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

class JavaDefaultParameterTransformerDefinition implements DefaultParameterTransformerDefinition {

    private final Method method;

    private final Lookup lookup;
    private final ParameterByTypeTransformer transformer;
    private final String shortFormat;
    private final String fullFormat;

    JavaDefaultParameterTransformerDefinition(Method method, Lookup lookup) {
        this.method = requireValidMethod(method);
        this.lookup = lookup;
        this.shortFormat = MethodFormat.SHORT.format(method);
        this.fullFormat = MethodFormat.FULL.format(method);
        this.transformer = this::execute;
    }

    private Method requireValidMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Void.class.equals(returnType)) {
            throw createInvalidSignatureException();
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 2) {
            throw createInvalidSignatureException();
        }

        if (!(String.class.equals(parameterTypes[0]) || Object.class.equals(parameterTypes[0]))) {
            throw createInvalidSignatureException();
        }

        if (!Type.class.equals(parameterTypes[1])) {
            throw createInvalidSignatureException();
        }

        return method;
    }

    private CucumberException createInvalidSignatureException() {
        return new CucumberException("" +
            "A @DefaultParameterTransformer annotated method must have one of these signature:\n" +
            " * public Object defaultParameter(String fromValue, Type toValueType)\n" +
            " * public Object defaultParameter(Object fromValue, Type toValueType)\n"
        );
    }

    @Override
    public ParameterByTypeTransformer parameterByTypeTransformer() {
        return transformer;
    }

    @Override
    public String getLocation(boolean detail) {
        return detail ? fullFormat : shortFormat;
    }

    private Object execute(String fromValue, Type toValueType) throws Throwable {
        return Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, 0, fromValue, toValueType);
    }

}
