package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.reflection.MethodFormat;

import java.lang.reflect.Method;

abstract class AbstractGlueDefinition {

    protected final Method method;
    protected final Lookup lookup;
    private String shortFormat;
    private String fullFormat;

    AbstractGlueDefinition(Method method, Lookup lookup) {
        this.method = method;
        this.lookup = lookup;
    }

    public final String getLocation(boolean detail) {
        return detail ? getFullLocationLocation() : getShortFormatLocation();
    }

    private String getShortFormatLocation() {
        if (shortFormat == null) {
            shortFormat = MethodFormat.SHORT.format(method);
        }
        return shortFormat;
    }

    private String getFullLocationLocation() {
        if (fullFormat == null) {
            fullFormat = MethodFormat.FULL.format(method);
        }
        return fullFormat;
    }
}
