package cucumber.runtime.java;

import io.cucumber.stepexpression.TypeRegistry;
import cucumber.api.java.ObjectFactory;
import io.cucumber.stepexpression.Argument;
import io.cucumber.stepexpression.ArgumentMatcher;
import io.cucumber.stepexpression.ExpressionArgumentMatcher;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.StepDefinition;
import io.cucumber.stepexpression.StepExpression;
import io.cucumber.stepexpression.StepExpressionFactory;
import cucumber.runtime.Utils;
import gherkin.pickles.PickleStep;

import java.lang.reflect.Method;
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
        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
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
}
