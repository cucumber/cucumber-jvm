package cucumber.runtime.java;

import cucumber.runtime.*;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * User: michael
 * Date: 7/5/12
 * Time: 10:36 PM
 */
class JavaAdviceDefinition implements StepDefinition {
    private final Glue glue;
    private final Method method;
    private final Pattern pattern;
    private final int stepGroup;
    private final List<Class<? extends Annotation>> advises;
    private final int timeout;
    private final JdkPatternArgumentMatcher argumentMatcher;
    private final ObjectFactory objectFactory;
    private List<ParameterType> parameterTypes;

    public JavaAdviceDefinition(Glue glue, Method method, Pattern pattern, int stepGroup, List<Class<? extends Annotation>> advises, int timeout, ObjectFactory objectFactory) {
        this.glue = glue;
        this.method = method;
        this.parameterTypes = ParameterType.fromMethod(method);
        // remove the Runnable type
        this.parameterTypes.remove(0);
        // add the type for the group of the original step
        this.parameterTypes.add(stepGroup - 1, new ParameterType(String.class, null));
        this.pattern = pattern;
        this.stepGroup = stepGroup;
        this.advises = advises;
        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.timeout = timeout;
        this.objectFactory = objectFactory;
    }

    @Override
    public void execute(I18n i18n, Object[] args) throws Throwable {
        Step step = new Step(Collections.<Comment>emptyList(), "Keyword", (String)args[stepGroup-1], 0, null, null);
        StepDefinitionMatch match = glue.stepDefinitionMatch("unknown", step, i18n);

        if (match == null) {
            throw new RuntimeException("Undefined Step: " + step.getName());
        }

        // that's probably the worst part of this patch, both the exposed step definition and the cast
        // however, the step definition has to be checked for the annotations, and we need to ensure
        // that there actually is a step definition to execute
        JavaStepDefinition stepDefinition = (JavaStepDefinition)match.getStepDefinition();
        if (!stepDefinition.isAnnotatedWithOneOf(advises)) {
            throw new RuntimeException("Advice cannot be applied to: " + step.getName());
        }

        Object[] params = new Object[args.length];
        params[0] = new StepRunnable(match, i18n);
        System.arraycopy(args, 0, params, 1, stepGroup - 1);
        System.arraycopy(args, stepGroup, params, stepGroup, args.length - stepGroup);

        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeout, params);
    }

    @Override
    public List<Argument> matchedArguments(Step step) {
        return argumentMatcher.argumentsFrom(step.getName());
    }

    @Override
    public String getLocation(boolean detail) {
        MethodFormat format = detail ? MethodFormat.FULL : MethodFormat.SHORT;
        return format.format(method);
    }

    @Override
    public Integer getParameterCount() {
        return parameterTypes.size();
    }

    @Override
    public ParameterType getParameterType(int n, Type argumentType) throws IndexOutOfBoundsException {
        return parameterTypes.get(n);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement e) {
        return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
    }

    @Override
    public String getPattern() {
        return pattern.pattern();
    }

    private class StepRunnable implements Runnable {
        private final StepDefinitionMatch match;
        private final I18n i18n;

        private StepRunnable(StepDefinitionMatch match, I18n i18n) {
            this.match = match;
            this.i18n = i18n;
        }

        @Override
        public void run() {
            try {
                match.runStep(i18n);
            } catch (Throwable th) {
                throw new RuntimeException(th.getMessage(), th);
            }
        }
    }
}
