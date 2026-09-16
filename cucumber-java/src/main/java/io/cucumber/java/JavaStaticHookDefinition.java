package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.StaticHookDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.requireNonNull;

final class JavaStaticHookDefinition extends AbstractGlueDefinition implements StaticHookDefinition {

    private final HookType hookType;
    private final int order;
    private final String name;

    JavaStaticHookDefinition(HookType hookType, Method method, int order, String name, Lookup lookup) {
        super(requireValidMethod(method), lookup);
        this.hookType = requireNonNull(hookType);
        this.order = order;
        this.name = requireNonNull(name, "name may not be null");
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
        return InvalidMethodSignatureException.builder(method)
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

    @Override
    public Optional<String> getName() {
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }

    @Override
    public Optional<HookType> getHookType() {
        return Optional.of(hookType);
    }
}
