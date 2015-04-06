package cucumber.runtime.java;

import cucumber.api.java8.StepdefBody;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

public class Java8StepDefinition implements StepDefinition {

    private final Pattern pattern;
    private final long timeoutMillis;
    private final StepdefBody body;

    private final JdkPatternArgumentMatcher argumentMatcher;
    private final StackTraceElement location;

    private final List<ParameterInfo> parameterInfos;
    private final Method method;

    public Java8StepDefinition(Pattern pattern, long timeoutMillis, StepdefBody body, TypeIntrospector typeIntrospector) throws Exception {
        this.pattern = pattern;
        this.timeoutMillis = timeoutMillis;
        this.body = body;

        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.location = new Exception().getStackTrace()[3];

        Class<? extends StepdefBody> bodyClass = body.getClass();

        Type genericInterface = bodyClass.getGenericInterfaces()[0];
        Type[] typeArguments;
        if (genericInterface instanceof ParameterizedType) {
            typeArguments = ((ParameterizedType) genericInterface).getActualTypeArguments();
        } else {
            typeArguments = typeIntrospector.getGenericTypes(bodyClass);
        }
        this.parameterInfos = ParameterInfo.fromTypes(typeArguments);

        Class[] parameterTypes = new Class[parameterInfos.size()];
        for (int i = 0; i < parameterInfos.size(); i++) {
            parameterTypes[i] = Object.class;
        }
        this.method = bodyClass.getDeclaredMethod("accept", parameterTypes);
    }

    @Override
    public List<Argument> matchedArguments(Step step) {
        return argumentMatcher.argumentsFrom(step.getName());
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
    public void execute(final I18n i18n, final Object[] args) throws Throwable {
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
