package cucumber.runtime.gosu;

import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import gw.lang.function.AbstractBlock;
import gw.lang.reflect.IType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GosuStepDefinition implements StepDefinition {
    private final Pattern pattern;
    private final StackTraceElement location;
    private final AbstractBlock block;

    private final JdkPatternArgumentMatcher argumentMatcher;
    private final List<ParameterInfo> parameterInfos;

    public GosuStepDefinition(Pattern pattern, AbstractBlock block, StackTraceElement location) {
        this.block = block;
        this.pattern = pattern;
        this.location = location;

        this.argumentMatcher = new JdkPatternArgumentMatcher(pattern);
        this.parameterInfos = getParameterInfos();
    }

    private List<ParameterInfo> getParameterInfos() {
        IType[] parameterTypes = block.getFunctionType().getParameterTypes();
        List<ParameterInfo> result = new ArrayList<ParameterInfo>(parameterTypes.length);
        for (IType parameterType : parameterTypes) {
            // TODO: Can we get more info from the parameterType?
            result.add(new ParameterInfo(String.class, null, null, null));
        }
        return result;
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
    public ParameterInfo getParameterType(int n, Type argumentType) {
        return parameterInfos.get(n);
    }

    @Override
    public void execute(I18n i18n, Object[] args) throws Throwable {
        // TODO: Add timeout
        block.invokeWithArgs(args);
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
        return false;
    }
}
