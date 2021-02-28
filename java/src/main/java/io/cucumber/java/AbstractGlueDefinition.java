package io.cucumber.java;

import io.cucumber.core.backend.Located;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.SourceReference;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

abstract class AbstractGlueDefinition implements Located {

    protected final Method method;
    private final Lookup lookup;
    private String fullFormat;
    private SourceReference sourceReference;

    AbstractGlueDefinition(Method method, Lookup lookup) {
        this.method = requireNonNull(method);
        this.lookup = requireNonNull(lookup);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName())
                && e.getMethodName().equals(method.getName());
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

    @Override
    public Optional<SourceReference> getSourceReference() {
        if (sourceReference == null) {
            sourceReference = SourceReference.fromMethod(this.method);
        }
        return Optional.of(sourceReference);
    }

}
