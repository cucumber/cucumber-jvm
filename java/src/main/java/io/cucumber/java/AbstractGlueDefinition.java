package io.cucumber.java;

import io.cucumber.core.backend.Located;
import io.cucumber.core.backend.Lookup;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static java.util.Objects.requireNonNull;

abstract class AbstractGlueDefinition implements Located {

    protected final Method method;
    private final Lookup lookup;
    private String fullFormat;

    AbstractGlueDefinition(Method method, Lookup lookup) {
        this.method = requireNonNull(method);
        this.lookup = requireNonNull(lookup);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }

    @Override
    public final String getLocation() {
        return getFullLocationLocation();
    }

    private String getFullLocationLocation() {
        if (fullFormat == null) {
            fullFormat = MethodFormat.FULL.format(method);
        }
        return fullFormat;
    }

    final Object invokeMethod(Object... args) {
        if (Modifier.isStatic(method.getModifiers())) {
            return Invoker.invokeStatic(this, method, args);
        }
        return Invoker.invoke(this, lookup.getInstance(method.getDeclaringClass()), method, args);
    }

}
