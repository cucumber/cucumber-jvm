package io.cucumber.java8;

import io.cucumber.core.runner.ScenarioScoped;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

abstract class AbstractGlueDefinition implements ScenarioScoped {

    Object body;
    final Method method;
    final StackTraceElement location;

    AbstractGlueDefinition(Object body, StackTraceElement location) {
        this.body = requireNonNull(body);
        this.method = getAcceptMethod(body.getClass());
        this.location = requireNonNull(location);
    }

    public final String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    public final boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName() != null && location.getFileName().equals(stackTraceElement.getFileName());
    }

    private Method getAcceptMethod(Class<?> bodyClass) {
        List<Method> acceptMethods = new ArrayList<>();
        for (Method method : bodyClass.getDeclaredMethods()) {
            if (!method.isBridge() && !method.isSynthetic() && "accept".equals(method.getName())) {
                acceptMethods.add(method);
            }
        }
        if (acceptMethods.size() != 1) {
            throw new IllegalStateException(format(
                "Expected single 'accept' method on body class, found '%s'", acceptMethods));
        }
        return acceptMethods.get(0);
    }

    @Override
    public final void disposeScenarioScope() {
        this.body = null;
    }
}
