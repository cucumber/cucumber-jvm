package cucumber.runtime.java;

import cucumber.api.TypeRegistry;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.ArgumentMatcher;
import cucumber.runtime.ExpressionArgumentMatcher;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepExpression;
import cucumber.runtime.StepExpressionFactory;
import cucumber.runtime.Utils;
import gherkin.pickles.PickleStep;
import io.cucumber.cucumberexpressions.Argument;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

class JavaStepDefinition implements StepDefinition {
    private final Method method;
    private final StepExpression expression;
    private final long timeoutMillis;
    private final ObjectFactory objectFactory;

    private final List<ParameterInfo> parameterInfos;

    public JavaStepDefinition(Method method, String expression, long timeoutMillis, ObjectFactory objectFactory, TypeRegistry parameterTypeRegistry
            ) {
        this.method = method;
        this.timeoutMillis = timeoutMillis;
        this.objectFactory = objectFactory;
        this.parameterInfos = ParameterInfo.fromMethod(method);

        if(parameterInfos.isEmpty()){
            this.expression = new StepExpressionFactory(parameterTypeRegistry).createExpression(expression);
        } else {
            ParameterInfo parameterInfo = parameterInfos.get(parameterInfos.size() - 1);
            this.expression = new StepExpressionFactory(parameterTypeRegistry).createExpression(expression, parameterInfo.getType(), parameterInfo.isTransposed());
        }
    }

    public void execute(String language, Object[] args) throws Throwable {
        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    public List<Argument<?>> matchedArguments(PickleStep step) {
        ArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(expression);
        return argumentMatcher.argumentsFrom(step);
    }

    public String getLocation(boolean detail) {
        MethodFormat format = detail ? MethodFormat.FULL : MethodFormat.SHORT;
        return format.format(method);
    }

    @Override
    public Integer getParameterCount() {
        return parameterInfos.size();
    }

    @Override
    public ParameterInfo getParameterType(int n, Type argumentType) {
        return parameterInfos.get(n);
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
