package io.cucumber.java8;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.runner.ScenarioScoped;
import io.cucumber.core.runtime.Invoker;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static net.jodah.typetools.TypeResolver.resolveRawArguments;

final class Java8StepDefinition implements StepDefinition, ScenarioScoped {

    public static <T extends StepdefBody> Java8StepDefinition create(
        String expression, Class<T> bodyClass, T body) {
        return new Java8StepDefinition(expression, 0, bodyClass, body);
    }

    public static <T extends StepdefBody> StepDefinition create(
        String expression, long timeoutMillis, Class<T> bodyClass, T body) {
        return new Java8StepDefinition(expression, timeoutMillis, bodyClass, body);
    }

    private final long timeoutMillis;
    private StepdefBody body;
    private final StackTraceElement location;
    private final Method method;
    private final List<ParameterInfo> parameterInfos;
    private final String expression;

    private <T extends StepdefBody> Java8StepDefinition(String expression,
                                                        long timeoutMillis,
                                                        Class<T> bodyClass,
                                                        T body) {
        this.timeoutMillis = timeoutMillis;
        this.body = body;
        this.location = new Exception().getStackTrace()[3];
        this.method = getAcceptMethod(body.getClass());
        this.expression = expression;
        this.parameterInfos = fromTypes(expression, location, resolveRawArguments(bodyClass, body.getClass()));
    }

    private Method getAcceptMethod(Class<? extends StepdefBody> bodyClass) {
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
    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public void execute(final Object[] args) throws Throwable {
        Invoker.invoke(body, method, timeoutMillis, args);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName() != null && location.getFileName().equals(stackTraceElement.getFileName());
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return parameterInfos;
    }

    @Override
    public String getPattern() {
        return expression;
    }


    @Override
    public void disposeScenarioScope() {
        this.body = null;
    }

    private static List<ParameterInfo> fromTypes(String expression, StackTraceElement location, Type[] genericParameterTypes) {
        List<ParameterInfo> result = new ArrayList<>();
        for (Type type : genericParameterTypes) {
            LambdaTypeResolver typeResolver = new LambdaTypeResolver(type, expression, location);
            result.add(new Java8ParameterInfo(type, typeResolver));
        }
        return result;
    }

}
