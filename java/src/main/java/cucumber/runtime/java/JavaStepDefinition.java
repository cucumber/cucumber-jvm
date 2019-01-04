package cucumber.runtime.java;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.pickles.PickleStep;
import io.cucumber.stepexpression.Argument;
import io.cucumber.stepexpression.ArgumentMatcher;
import io.cucumber.stepexpression.ExpressionArgumentMatcher;
import io.cucumber.stepexpression.StepExpression;
import io.cucumber.stepexpression.StepExpressionFactory;
import io.cucumber.stepexpression.TypeRegistry;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

class JavaStepDefinition implements StepDefinition {
    private final Method method;
    private final StepExpression expression;
    private final long timeoutMillis;
    private final ObjectFactory objectFactory;

    private final ArgumentMatcher argumentMatcher;
    private final Type[] parameterTypes;
    private final String shortFormat;
    private final String fullFormat;

    JavaStepDefinition(Method method,
                       String expression,
                       long timeoutMillis,
                       ObjectFactory objectFactory,
                       TypeRegistry typeRegistry) {
        this.method = method;
        this.timeoutMillis = timeoutMillis;
        this.objectFactory = objectFactory;
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
        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
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

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
