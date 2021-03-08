package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.StaticHookDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static io.cucumber.java.InvalidMethodSignatureException.builder;
import static java.lang.reflect.Modifier.isStatic;

final class JavaStaticHookDefinition extends AbstractGlueDefinition implements StaticHookDefinition {

    private final int order;

    JavaStaticHookDefinition(Method method, int order, Lookup lookup) {
        super(requireValidMethod(method), lookup);
        this.order = order;
    }

    private static Method requireValidMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 0) {
            throw createInvalidSignatureException(method);
        }

        if (!isStatic(method.getModifiers())) {
            throw createInvalidSignatureException(method);
        }

        Type returnType = method.getGenericReturnType();
        if (!Void.class.equals(returnType) && !void.class.equals(returnType)) {
            throw createInvalidSignatureException(method);
        }

        return method;
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(Method method) {
        return builder(method)
                .addAnnotation(BeforeAll.class)
                .addAnnotation(AfterAll.class)
                .addSignature("public static void before_or_after_all()")
                .build();
    }

    @Override
    public void execute() {
        invokeMethod();
    }

    @Override
    public int getOrder() {
        return order;
    }
}
