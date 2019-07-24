package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.reflection.MethodFormat;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.cucumberexpressions.ParameterType;

import java.lang.reflect.Method;

import static io.cucumber.java.InvalidMethodSignatureExceptionBuilder.builder;
import static java.util.Collections.singletonList;

class JavaParameterTypeDefinition implements ParameterTypeDefinition {

    private final Method method;

    private final Lookup lookup;
    private final ParameterType<Object> parameterType;
    private final String shortFormat;
    private final String fullFormat;

    JavaParameterTypeDefinition(String name, String pattern, Method method, boolean useForSnippets, boolean preferForRegexpMatch, Lookup lookup) {
        this.method = requireValidMethod(method);
        this.lookup = lookup;
        this.shortFormat = MethodFormat.SHORT.format(method);
        this.fullFormat = MethodFormat.FULL.format(method);
        this.parameterType = new ParameterType<>(
            name.isEmpty() ? method.getName() : name,
            singletonList(pattern),
            this.method.getReturnType(),
            this::execute,
            useForSnippets,
            preferForRegexpMatch
        );
    }

    private static Method requireValidMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Void.class.equals(returnType)) {
            throw createInvalidSignatureException(method);
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length < 1) {
            throw createInvalidSignatureException(method);
        }

        for (Class<?> parameterType : parameterTypes) {
            if (!String.class.equals(parameterType)) {
                throw createInvalidSignatureException(method);
            }
        }

        return method;
    }

    private static CucumberException createInvalidSignatureException(Method method) {
        return builder(method)
            .addAnnotation(ParameterType.class)
            .addSignature("public Author parameterName(String all)")
            .addSignature("public Author parameterName(String captureGroup1, String captureGroup2, ...ect )")
            .addSignature("public Author parameterName(String... captureGroups)")
            .addNote("Note: Author is an example of the class you want to convert captureGroups to")
            .build();
    }

    @Override
    public ParameterType<?> parameterType() {
        return parameterType;
    }

    @Override
    public String getLocation(boolean detail) {
        return detail ? fullFormat : shortFormat;
    }

    private Object execute(Object[] args) throws Throwable {
        return Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, 0, args);
    }

}
