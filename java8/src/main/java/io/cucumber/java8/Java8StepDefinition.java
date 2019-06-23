package io.cucumber.java8;

import gherkin.pickles.PickleStep;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.core.runner.ScenarioScoped;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.ArgumentMatcher;
import io.cucumber.core.stepexpression.ExpressionArgumentMatcher;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.core.stepexpression.TypeResolver;
import net.jodah.typetools.TypeResolver.Unknown;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.cucumber.java8.Java8StepDefinition.ParameterInfo.fromTypes;
import static java.lang.String.format;
import static net.jodah.typetools.TypeResolver.resolveRawArguments;

final class Java8StepDefinition implements StepDefinition, ScenarioScoped {

    public static <T extends StepdefBody> Java8StepDefinition create(
        String expression, Class<T> bodyClass, T body, TypeRegistry typeRegistry) {
        return new Java8StepDefinition(expression, 0, bodyClass, body, typeRegistry);
    }

    public static <T extends StepdefBody> StepDefinition create(
        String expression, long timeoutMillis, Class<T> bodyClass, T body, TypeRegistry typeRegistry) {
        return new Java8StepDefinition(expression, timeoutMillis, bodyClass, body, typeRegistry);
    }

    private final long timeoutMillis;
    private StepdefBody body;

    private final StepExpression expression;
    private final StackTraceElement location;

    private final List<ParameterInfo> parameterInfos;
    private final Method method;

    private <T extends StepdefBody> Java8StepDefinition(String expression,
                                                        long timeoutMillis,
                                                        Class<T> bodyClass,
                                                        T body,
                                                        TypeRegistry typeRegistry) {
        this.timeoutMillis = timeoutMillis;
        this.body = body;

        this.location = new Exception().getStackTrace()[5];
        this.method = getAcceptMethod(body.getClass());
        this.parameterInfos = fromTypes(resolveRawArguments(bodyClass, body.getClass()));
        this.expression = createExpression(expression, typeRegistry);
    }

    private StepExpression createExpression(String expression, TypeRegistry typeRegistry) {
        if (parameterInfos.isEmpty()) {
            return new StepExpressionFactory(typeRegistry).createExpression(expression);
        } else {
            ParameterInfo parameterInfo = parameterInfos.get(parameterInfos.size() - 1);
            return new StepExpressionFactory(typeRegistry).createExpression(expression, new LambdaTypeResolver(parameterInfo));
        }
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

    private CucumberException withLocation(CucumberException exception) {
        exception.setStackTrace(new StackTraceElement[]{this.location});
        return exception;
    }

    @Override
    public List<Argument> matchedArguments(PickleStep step) {
        ArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(expression);
        Type[] types = new Type[parameterInfos.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = parameterInfos.get(i).getType();
        }
        return argumentMatcher.argumentsFrom(step, types);
    }

    @Override
    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public Integer getParameterCount() {
        return parameterInfos.size();
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
    public String getPattern() {
        return expression.getSource();
    }

    private final class LambdaTypeResolver implements TypeResolver {


        private final ParameterInfo parameterInfo;

        LambdaTypeResolver(ParameterInfo parameterInfo) {
            this.parameterInfo = parameterInfo;
        }

        @Override
        public Type resolve() {
            Type type = parameterInfo.getType();
            if (Unknown.class.equals(type)) {
                return Object.class;
            }

            return requireNonMapOrListType(type);
        }

        private Type requireNonMapOrListType(Type argumentType) {
            if (argumentType instanceof Class) {
                Class<?> argumentClass = (Class<?>) argumentType;
                if (List.class.isAssignableFrom(argumentClass) || Map.class.isAssignableFrom(argumentClass)) {
                    throw withLocation(
                        new CucumberException(
                            format("Can't use %s in lambda step definition \"%s\". " +
                                    "Declare a DataTable argument instead and convert " +
                                    "manually with asList/asLists/asMap/asMaps",
                                argumentClass.getName(), expression.getSource())));
                }
            }
            return argumentType;
        }
    }

    @Override
    public void disposeScenarioScope() {
        this.body = null;
    }

    static class ParameterInfo {
        private final Type type;

        static List<ParameterInfo> fromTypes(Type[] genericParameterTypes) {
            List<ParameterInfo> result = new ArrayList<>();
            for (Type genericParameterType : genericParameterTypes) {
                result.add(new ParameterInfo(genericParameterType));
            }
            return result;
        }

        private ParameterInfo(Type type) {
            this.type = type;
        }

        Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return type.toString();
        }

    }
}
