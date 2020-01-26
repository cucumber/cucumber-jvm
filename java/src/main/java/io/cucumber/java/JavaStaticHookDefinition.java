package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.StaticHookDefinition;

import java.lang.reflect.Method;

import static io.cucumber.java.InvalidMethodSignatureException.builder;
import static java.lang.reflect.Modifier.isStatic;

final class JavaStaticHookDefinition extends AbstractGlueDefinition implements StaticHookDefinition {

    private final int order;
    private final Lookup lookup;

    JavaStaticHookDefinition(Method method, int order, Lookup lookup) {
        super(requireValidMethod(method), lookup);
        this.order = order;
        this.lookup = lookup;
    }

    private static Method requireValidMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 0) {
            throw createInvalidSignatureException(method);
        }

        if (!isStatic(method.getModifiers())) {
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
        Invoker.invoke(this, lookup.getInstance(method.getDeclaringClass()), method);
    }

    @Override
    public int getOrder() {
        return order;
    }
}
