package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.cucumberexpressions.ParameterType;

import java.lang.reflect.Method;
import java.util.Collections;

class JavaParameterTypeDefinition implements ParameterTypeDefinition {

    private final Method method;

    private final Lookup lookup;
    private final ParameterType<Object> parameterType;

    JavaParameterTypeDefinition(String name, String pattern, Method method, boolean useForSnippets, boolean preferForRegexpMatch, Lookup lookup) {
        this.method = requireValidMethod(method);
        this.lookup = lookup;
        this.parameterType = new ParameterType<>(
            name.isEmpty() ? method.getName() : name,
            Collections.singletonList(pattern),
            this.method.getReturnType(),
            this::execute,
            useForSnippets,
            preferForRegexpMatch
        );
    }

    private Method requireValidMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Void.class.equals(returnType)) {
            throw createInvalidSignatureException();
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length < 1) {
            throw createInvalidSignatureException();
        }

        for (Class<?> parameterType : parameterTypes) {
            if (!String.class.equals(parameterType)) {
                throw createInvalidSignatureException();
            }
        }

        return method;
    }

    private CucumberException createInvalidSignatureException() {
        return new CucumberException("" +
            "A @ParameterType annotated method must have one of these signatures:\n" +
            " * public Author parameterName(String all)\n" +
            " * public Author parameterName(String captureGroup1, String captureGroup2, ...ect )\n" +
            " * public Author parameterName(String... captureGroups)\n" +
            "Note: Author is an example of the class you want to convert parameter name"
        );
    }

    @Override
    public ParameterType<?> parameterType() {
        return parameterType;
    }

    private Object execute(Object[] args) throws Throwable {
        return Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, 0, args);
    }

}
