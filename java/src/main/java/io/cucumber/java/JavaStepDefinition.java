package io.cucumber.java;

import io.cucumber.core.runtime.Invoker;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.java.api.ObjectFactory;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.ArgumentMatcher;
import io.cucumber.core.stepexpression.ExpressionArgumentMatcher;
import io.cucumber.core.reflection.MethodFormat;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;
import gherkin.pickles.PickleStep;
import io.cucumber.java.api.Transpose;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.lang.reflect.Type;
import java.util.List;

class JavaStepDefinition implements StepDefinition {
    private final Method method;
    private final StepExpression expression;
    private final long timeoutMillis;
    private final ObjectFactory objectFactory;

    private final List<ParameterInfo> parameterInfos;

    JavaStepDefinition(Method method,
                       String expression,
                       long timeoutMillis,
                       ObjectFactory objectFactory,
                       TypeRegistry typeRegistry) {
        this.method = method;
        this.timeoutMillis = timeoutMillis;
        this.objectFactory = objectFactory;
        this.parameterInfos = ParameterInfo.fromMethod(method);
        this.expression = createExpression(expression, typeRegistry);
    }

    private StepExpression createExpression(String expression, TypeRegistry typeRegistry) {
        if (parameterInfos.isEmpty()) {
            return new StepExpressionFactory(typeRegistry).createExpression(expression);
        } else {
            ParameterInfo parameterInfo = parameterInfos.get(parameterInfos.size() - 1);
            return new StepExpressionFactory(typeRegistry).createExpression(expression, parameterInfo.getType(), parameterInfo.isTransposed());
        }
    }

    public void execute(Object[] args) throws Throwable {
        Invoker.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    public List<Argument> matchedArguments(PickleStep step) {
        ArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(expression);
        Type[] types = new Type[parameterInfos.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = parameterInfos.get(i).getType();
        }
        return argumentMatcher.argumentsFrom(step, types);
    }

    public String getLocation(boolean detail) {
        MethodFormat format = detail ? MethodFormat.FULL : MethodFormat.SHORT;
        return format.format(method);
    }

    @Override
    public Integer getParameterCount() {
        return parameterInfos.size();
    }

    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }

    @Override
    public String getPattern() {
        return expression.getSource();
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }

    /**
     * This class composes all interesting parameter information into one object.
     */
    static class ParameterInfo {
        private final Type type;
        private final boolean transposed;

        static List<ParameterInfo> fromMethod(Method method) {
            List<ParameterInfo> result = new ArrayList<ParameterInfo>();
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            Annotation[][] annotations = method.getParameterAnnotations();
            for (int i = 0; i < genericParameterTypes.length; i++) {
                boolean transposed = false;
                for (Annotation annotation : annotations[i]) {
                    if (annotation instanceof Transpose) {
                        transposed = ((Transpose) annotation).value();
                    }
                }
                result.add(new ParameterInfo(genericParameterTypes[i], transposed));
            }
            return result;
        }

        private ParameterInfo(Type type, boolean transposed) {
            this.type = type;
            this.transposed = transposed;
        }

        Type getType() {
            return type;
        }

        boolean isTransposed() {
            return transposed;
        }

        @Override
        public String toString() {
            return type.toString();
        }

    }
}
