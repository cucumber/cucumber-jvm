package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.cucumberexpressions.ParameterType;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static io.cucumber.java.InvalidMethodSignatureException.builder;
import static java.util.Collections.singletonList;

class JavaParameterTypeDefinition extends AbstractGlueDefinition implements ParameterTypeDefinition {

    private final ParameterType<Object> parameterType;

    JavaParameterTypeDefinition(
            String name, String pattern, Method method, boolean useForSnippets, boolean preferForRegexpMatch,
            boolean useRegexpMatchAsStrongTypeHint, Lookup lookup
    ) {
        super(requireValidMethod(method), lookup);
        this.parameterType = new ParameterType<>(
            name.isEmpty() ? method.getName() : name,
            singletonList(pattern),
            this.method.getGenericReturnType(),
            this::execute,
            useForSnippets,
            preferForRegexpMatch,
            useRegexpMatchAsStrongTypeHint);
    }

    private static Method requireValidMethod(Method method) {
        Type returnType = method.getGenericReturnType();
        if (Void.class.equals(returnType) || void.class.equals(returnType)) {
            throw createInvalidSignatureException(method);
        }

        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes.length == 0) {
            throw createInvalidSignatureException(method);
        }

        if (parameterTypes.length == 1) {
            if (!(String.class.equals(parameterTypes[0]) || String[].class.equals(parameterTypes[0]))) {
                throw createInvalidSignatureException(method);
            }
            return method;
        }

        for (Type parameterType : parameterTypes) {
            if (!String.class.equals(parameterType)) {
                throw createInvalidSignatureException(method);
            }
        }

        return method;
    }

    private Object execute(String[] captureGroups) {
        Object[] args;

        if (String[].class.equals(method.getParameterTypes()[0])) {
            args = new Object[][] { captureGroups };
        } else {
            args = captureGroups;
        }

        return invokeMethod(args);
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(Method method) {
        return builder(method)
                .addAnnotation(io.cucumber.java.ParameterType.class)
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

}
