package cucumber.runtime.java;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Argument;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.ArgumentMatcher;
import cucumber.runtime.ExpressionArgumentMatcher;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.pickles.PickleStep;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.TransformLookup;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class JavaStepDefinition implements StepDefinition {
    private final Method method;
    private final Expression expression;
    private final long timeoutMillis;
    private final ObjectFactory objectFactory;

    private final List<ParameterInfo> parameterInfos;

    public JavaStepDefinition(Method method, String expression, long timeoutMillis, ObjectFactory objectFactory, TransformLookup transformLookup) {
        this.method = method;
        this.expression = new ExpressionFactory(transformLookup).createExpression(expression, getArgumentTypes(method));
        this.timeoutMillis = timeoutMillis;
        this.objectFactory = objectFactory;

        this.parameterInfos = ParameterInfo.fromMethod(method);
    }

    public void execute(String language, Object[] args) throws Throwable {
        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    public List<Argument> matchedArguments(PickleStep step) {
        ArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(expression);
        return argumentMatcher.argumentsFrom(step.getText());
    }

    private static List<Type> getArgumentTypes(Method method) {
        List<Type> types = new ArrayList<Type>(method.getGenericParameterTypes().length);
        for (Type type : method.getGenericParameterTypes()) {
            types.add(type);
        }
        return types;
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
