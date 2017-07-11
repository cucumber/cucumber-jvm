package cucumber.runtime.java;

import cucumber.api.java8.StepdefBody;
import cucumber.runtime.ArgumentMatcher;
import cucumber.runtime.CucumberException;
import cucumber.runtime.ExpressionArgumentMatcher;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.pickles.PickleStep;
import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class Java8StepDefinition implements StepDefinition {

    private final long timeoutMillis;
    private final StepdefBody body;

    private final Expression expression;
    private final StackTraceElement location;

    private final List<ParameterInfo> parameterInfos;
    private final Method method;

    public Java8StepDefinition(String expression, long timeoutMillis, StepdefBody body, TypeIntrospector typeIntrospector, ParameterTypeRegistry parameterTypeRegistry) throws Exception {
        this.timeoutMillis = timeoutMillis;
        this.body = body;

        this.location = new Exception().getStackTrace()[3];

        Class<? extends StepdefBody> bodyClass = body.getClass();

        this.method = getAcceptMethod(bodyClass);

        Type[] argumentTypes = getArgumentTypes(bodyClass, typeIntrospector, method.getParameterTypes().length);

        this.expression = new ExpressionFactory(parameterTypeRegistry).createExpression(expression, asList(argumentTypes));
        this.parameterInfos = ParameterInfo.fromTypes(argumentTypes);
    }

    private Type[] getArgumentTypes(Class<? extends StepdefBody> bodyClass, TypeIntrospector typeIntrospector, int parameterCount) throws Exception {
        Type genericInterface = bodyClass.getGenericInterfaces()[0];
        Type[] argumentTypes;
        if (genericInterface instanceof ParameterizedType) {
            argumentTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
        } else {
            Class<? extends StepdefBody> interfac3 = (Class<? extends StepdefBody>) bodyClass.getInterfaces()[0];
            argumentTypes = typeIntrospector.getGenericTypes(bodyClass, interfac3);
        }
        Type[] argumentTypesOfCorrectLength = new Type[parameterCount];
        System.arraycopy(argumentTypes, argumentTypes.length - parameterCount, argumentTypesOfCorrectLength, 0, parameterCount);
        verifyNotListOrMap(argumentTypesOfCorrectLength);

        return argumentTypesOfCorrectLength;
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

    private void verifyNotListOrMap(Type[] argumentTypes) {
        for (Type argumentType : argumentTypes) {
            if (argumentType instanceof Class) {
                Class<?> argumentClass = (Class<?>) argumentType;
                if (List.class.isAssignableFrom(argumentClass) || Map.class.isAssignableFrom(argumentClass)) {
                    throw withLocation(new CucumberException("Can't use " + argumentClass.getName() + " in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps"));
                }
            }
        }
    }

    private CucumberException withLocation(CucumberException exception) {
        exception.setStackTrace(new StackTraceElement[]{this.location});
        return exception;
    }

    @Override
    public List<Argument<?>> matchedArguments(PickleStep step) {
        ArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(expression);
        return argumentMatcher.argumentsFrom(step.getText());
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
        return location.getFileName().equals(stackTraceElement.getFileName());
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
