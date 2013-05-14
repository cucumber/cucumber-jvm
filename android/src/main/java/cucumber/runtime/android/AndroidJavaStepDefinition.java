package cucumber.runtime.android;

import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import cucumber.runtime.java.ObjectFactory;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

class AndroidJavaStepDefinition implements StepDefinition {
    private final Method mMethod;
    private final Pattern mPattern;
    private final int mTimeout;
    private final JdkPatternArgumentMatcher mArgumentMatcher;
    private final ObjectFactory mObjectFactory;
    private List<ParameterInfo> mParameterInfos;

    public AndroidJavaStepDefinition(Method method, Pattern pattern, int timeout, ObjectFactory objectFactory) {
        mMethod = method;
        mParameterInfos = ParameterInfo.fromMethod(method);
        mPattern = pattern;
        mArgumentMatcher = new JdkPatternArgumentMatcher(pattern);
        mTimeout = timeout;
        mObjectFactory = objectFactory;
    }

    public void execute(I18n i18n, Object[] args) throws Throwable {
        Utils.invoke(mObjectFactory.getInstance(mMethod.getDeclaringClass()), mMethod, mTimeout, args);
    }

    public List<Argument> matchedArguments(Step step) {
        return mArgumentMatcher.argumentsFrom(step.getName());
    }

    public String getLocation(boolean detail) {
        AndroidMethodFormat format = detail ? AndroidMethodFormat.FULL : AndroidMethodFormat.SHORT;
        return format.format(mMethod);
    }

    @Override
    public Integer getParameterCount() {
        return mParameterInfos.size();
    }

    @Override
    public ParameterInfo getParameterType(int n, Type argumentType) {
        return mParameterInfos.get(n);
    }

    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(mMethod.getDeclaringClass().getName()) && e.getMethodName().equals(mMethod.getName());
    }

    @Override
    public String getPattern() {
        return mPattern.pattern();
    }
}
