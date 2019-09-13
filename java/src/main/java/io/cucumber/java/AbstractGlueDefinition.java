package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.reflection.MethodFormat;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

abstract class AbstractGlueDefinition {

    protected final Method method;
    protected final Lookup lookup;
    private String fullFormat;

    AbstractGlueDefinition(Method method, Lookup lookup) {
        this.method = requireNonNull(method);
        this.lookup = requireNonNull(lookup);
    }

    public final String getLocation() {
        return getFullLocationLocation();
    }

    private String getFullLocationLocation() {
        if (fullFormat == null) {
            fullFormat = MethodFormat.FULL.format(method);
        }
        return fullFormat;
    }
}
