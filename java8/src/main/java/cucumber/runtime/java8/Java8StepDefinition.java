package cucumber.runtime.java8;

import static cucumber.runtime.ParameterInfo.fromTypes;
import static net.jodah.typetools.TypeResolver.resolveRawArguments;

import cucumber.api.Argument;
import cucumber.api.TypeRegistry;
import cucumber.api.java8.StepdefBody;
import cucumber.runtime.ArgumentMatcher;
import cucumber.runtime.CucumberException;
import cucumber.runtime.ExpressionArgumentMatcher;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepExpression;
import cucumber.runtime.StepExpressionFactory;
import cucumber.runtime.Utils;
import gherkin.pickles.PickleStep;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Java8StepDefinition implements StepDefinition {

    private final long timeoutMillis;
    private final StepdefBody body;

    private final StepExpression expression;
    private final StackTraceElement location;

    private final List<ParameterInfo> parameterInfos;
    private final Method method;

    public <T extends StepdefBody> Java8StepDefinition(String expression, long timeoutMillis, Class<T> bodyClass, T body, TypeRegistry parameterTypeRegistry)  {
        this.timeoutMillis = timeoutMillis;
        this.body = body;

        this.location = new Exception().getStackTrace()[4];
        this.method = getAcceptMethod(body.getClass());
        try {
            this.parameterInfos = fromTypes(verifyNotListOrMap(resolveRawArguments(bodyClass, body.getClass())));
            if(parameterInfos.isEmpty()){
                this.expression = new StepExpressionFactory(parameterTypeRegistry).createExpression(expression);
            } else {
                ParameterInfo parameterInfo = parameterInfos.get(parameterInfos.size() - 1);
                this.expression = new StepExpressionFactory(parameterTypeRegistry).createExpression(expression, parameterInfo.getType());
            }
        } catch (CucumberException e){
            throw e;
        } catch (Exception e) {
            throw new CucumberException(e);
        }
    }

    private Method getAcceptMethod(Class<? extends StepdefBody> bodyClass) {
        List<Method> acceptMethods = new ArrayList<Method>();
        for (Method method : bodyClass.getDeclaredMethods()) {
            if (!method.isBridge() && !method.isSynthetic() && "accept".equals(method.getName())) {
                acceptMethods.add(method);
            }
        }
        if (acceptMethods.size() != 1) {
            throw new IllegalStateException(String.format("Expected single 'accept' method on body class, found " +
                    "'%s'", acceptMethods));
        }
        return acceptMethods.get(0);
    }

    private Type[] verifyNotListOrMap(Type[] argumentTypes) {
        for (Type argumentType : argumentTypes) {
            if (argumentType instanceof Class) {
                Class<?> argumentClass = (Class<?>) argumentType;
                if (List.class.isAssignableFrom(argumentClass) || Map.class.isAssignableFrom(argumentClass)) {
                    throw withLocation(new CucumberException("Can't use " + argumentClass.getName() + " in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps"));
                }
            }
        }
        return argumentTypes;
    }

    private CucumberException withLocation(CucumberException exception) {
        exception.setStackTrace(new StackTraceElement[]{this.location});
        return exception;
    }

    @Override
    public List<Argument> matchedArguments(PickleStep step) {
        ArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(expression);
        return argumentMatcher.argumentsFrom(step);
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
    public ParameterInfo getParameterType(int n, Type argumentType) throws IndexOutOfBoundsException {
        return parameterInfos.get(n);
    }

    @Override
    public void execute(final String language, final Object[] args) throws Throwable {
        Utils.invoke(body, method, timeoutMillis, args);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName() != null && location.getFileName().equals(stackTraceElement.getFileName());
    }

    @Override
    public String getPattern() {
        return expression.getSource();
    }

    @Override
    public boolean isScenarioScoped() {
        return true;
    }
}
