package cucumber.runtime.java8;

import static cucumber.runtime.ParameterInfo.fromTypes;
import static net.jodah.typetools.TypeResolver.resolveRawArguments;

import cucumber.api.java8.StepdefBody;
import cucumber.runtime.Argument;
import cucumber.runtime.CucumberException;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.pickles.PickleStep;
import net.jodah.typetools.TypeResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Java8StepDefinition implements StepDefinition {

    private final Pattern pattern;
    private final long timeoutMillis;
    private final StepdefBody body;

    private final JdkPatternArgumentMatcher argumentMatcher;
    private final StackTraceElement location;

    private final List<ParameterInfo> parameterInfos;
    private final Method method;

    public <T extends StepdefBody> Java8StepDefinition(String pattern, long timeoutMillis, Class<T> bodyClass, T body)  {
        this.pattern = Pattern.compile(pattern);
        this.timeoutMillis = timeoutMillis;
        this.body = body;

        this.argumentMatcher = new JdkPatternArgumentMatcher(this.pattern);
        this.location = new Exception().getStackTrace()[3];
        this.method = getAcceptMethod(body.getClass());
        try {
            Class<?>[] arguments = resolveRawArguments(bodyClass, body.getClass());
            this.parameterInfos = fromTypes(verifyNotListOrMap(arguments));
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
        return pattern.pattern();
    }

    @Override
    public boolean isScenarioScoped() {
        return true;
    }
}
