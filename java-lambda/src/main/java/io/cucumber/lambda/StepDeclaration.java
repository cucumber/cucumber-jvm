package io.cucumber.lambda;

import io.cucumber.core.backend.Located;
import io.cucumber.core.backend.SourceReference;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.cucumber.core.backend.SourceReference.fromStackTraceElement;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

class StepDeclaration implements Located {
    private final String expression;
    private final Class<?> context;
    private final StepDefinitionFunction function;
    private final StackTraceElement location;
    private SourceReference sourceReference;

    StepDeclaration(String expression, Class<?> context, StepDefinitionFunction function, StackTraceElement location) {
        this.expression = requireNonNull(expression, "expression should not be null");
        this.context = requireNonNull(context, "context should not be null");
        this.function = requireNonNull(function, "function should not be null");
        this.location = requireNonNull(location, "location should not be null");

    }

    String getExpression() {
        return expression;
    }

    Class<?> getContext() {
        return context;
    }

    @Override
    public final boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName() != null && location.getFileName().equals(stackTraceElement.getFileName());
    }

    @Override
    public Optional<SourceReference> getSourceReference() {
        if (sourceReference == null) {
            sourceReference = fromStackTraceElement(location);
        }
        return Optional.of(sourceReference);
    }

    @Override
    public final String getLocation() {
        return location.toString();
    }

    void invoke(Object contextInstance, Object[] arguments) {
        Method acceptMethod = getAcceptMethod(function.getClass());
        Object body = Invoker.invoke(this, function, acceptMethod, contextInstance);
        Method bodyAcceptMethod = getAcceptMethod(body.getClass());
        Invoker.invoke(this, body, bodyAcceptMethod, arguments);
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

}
