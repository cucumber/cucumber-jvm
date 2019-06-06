package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.core.stepexpression.TypeRegistry;
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
import java.util.List;

class JavaStepDefinition implements StepDefinition {
    private final Method method;
    private final StepExpression expression;
    private final long timeoutMillis;
    private final Lookup lookup;

    private final ArgumentMatcher argumentMatcher;
    private final Type[] parameterTypes;
    private final String shortFormat;
    private final String fullFormat;

    JavaStepDefinition(Method method,
                       String expression,
                       long timeoutMillis,
                       Lookup lookup,
                       TypeRegistry typeRegistry) {
        this.method = method;
        this.timeoutMillis = timeoutMillis;
        this.lookup = lookup;
        List<ParameterInfo> parameterInfos = ParameterInfo.fromMethod(method);
        this.parameterTypes = getTypes(parameterInfos);
        this.expression = createExpression(parameterInfos, expression, typeRegistry);
        this.argumentMatcher = new ExpressionArgumentMatcher(this.expression);
        this.shortFormat = MethodFormat.SHORT.format(method);
        this.fullFormat = MethodFormat.FULL.format(method);
    }

    private StepExpression createExpression(List<ParameterInfo> parameterInfos, String expression, TypeRegistry typeRegistry) {
        if (parameterInfos.isEmpty()) {
            return new StepExpressionFactory(typeRegistry).createExpression(expression);
        } else {
            ParameterInfo parameterInfo = parameterInfos.get(parameterInfos.size() - 1);
            return new StepExpressionFactory(typeRegistry).createExpression(expression, parameterInfo.getType(), parameterInfo.isTransposed());
        }
    }

    @Override
    public void execute(Object[] args) throws Throwable {
        Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    @Override
    public List<Argument> matchedArguments(PickleStep step) {
        return argumentMatcher.argumentsFrom(step, parameterTypes);
    }

    private static Type[] getTypes(List<ParameterInfo> parameterInfos) {
        Type[] types = new Type[parameterInfos.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = parameterInfos.get(i).getType();
        }
        return types;
    }

    @Override
    public String getLocation(boolean detail) {
        return detail ? fullFormat : shortFormat;
    }

    @Override
    public Integer getParameterCount() {
        return parameterTypes.length;
    }

    @Override
    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }

    @Override
    public String getPattern() {
        return expression.getSource();
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
