package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.cucumberexpressions.ParameterType;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class JavaParameterTypeDefinition implements ParameterTypeDefinition {

    private final String name;
    private final List<String> patterns;
    private final Method method;

    private final Lookup lookup;
    private final boolean preferForRegexpMatch;
    private final boolean useForSnippets;

    JavaParameterTypeDefinition(String name, String pattern, Method method, boolean useForSnippets, boolean preferForRegexpMatch, Lookup lookup) {
        this.name = name.isEmpty() ? method.getName() : name;
        this.patterns = Collections.singletonList(pattern);
        this.method = requireValidMethod(method);
        this.lookup = lookup;
        this.useForSnippets = useForSnippets;
        this.preferForRegexpMatch = preferForRegexpMatch;
    }

    private Method requireValidMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Void.class.equals(returnType)) {
            throw new CucumberException("TODO");
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length < 1) {
            throw new CucumberException("TODO");
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (!String.class.equals(parameterType)) {
                throw new CucumberException("TODO" + i);
            }
        }

        return method;
    }

    @Override
    public ParameterType<?> parameterType() {
        return new ParameterType<>(
            name,
            patterns,
            method.getReturnType(),
            this::execute,
            useForSnippets,
            preferForRegexpMatch
        );
    }

    private Object execute(Object[] args) throws Throwable {
        return Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, 0, args);
    }

}
