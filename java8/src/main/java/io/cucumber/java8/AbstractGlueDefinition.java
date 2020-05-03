package io.cucumber.java8;

import io.cucumber.core.backend.ScenarioScoped;
import net.jodah.typetools.TypeResolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

abstract class AbstractGlueDefinition implements ScenarioScoped {

    final Object body;
    final Method method;
    final StackTraceElement location;

    AbstractGlueDefinition(Object body, StackTraceElement location) {
        this.body = requireNonNull(body);
        this.method = getAcceptMethod(body.getClass());
        this.location = requireNonNull(location);
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

    public final String getLocation() {
        return location.toString();
    }

    public final boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName() != null && location.getFileName().equals(stackTraceElement.getFileName());
    }

    Class<?>[] resolveRawArguments(Class<?> bodyClass, Class<?> body) {
        Class<?>[] rawArguments = TypeResolver.resolveRawArguments(bodyClass, body);
        for (Class<?> aClass : rawArguments) {
            if (TypeResolver.Unknown.class.equals(aClass)) {
                throw new IllegalStateException("" +
                    "Could resolve the return type of the lambda at " + location.getFileName() + ":" + location.getLineNumber() + "\n" +
                    "This version of cucumber-java8 is not compatible with Java 12+\n" +
                    "See: https://github.com/cucumber/cucumber-jvm/issues/1817"
                );
            }
        }
        return rawArguments;
    }

}
