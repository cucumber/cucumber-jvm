package cucumber.runtime.jruby;

import cucumber.runtime.Argument;
import cucumber.runtime.ArgumentMatcher;
import cucumber.runtime.ExpressionArgumentMatcher;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import gherkin.pickles.PickleStep;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.TransformLookup;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class JRubyStepDefinition implements StepDefinition {

    private final JRubyBackend jRubyBackend;
    private final RubyObject stepdefRunner;
    private final Expression expression;
    private String file;
    private Long line;

    public JRubyStepDefinition(JRubyBackend jRubyBackend, RubyObject stepdefRunner, TransformLookup transformLookup) {
        this.jRubyBackend = jRubyBackend;
        this.stepdefRunner = stepdefRunner;
        this.expression = new ExpressionFactory(transformLookup).createExpression(getPattern(), Collections.<Type>emptyList());
    }

    @Override
    public List<Argument> matchedArguments(PickleStep step) {
        ArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(expression);
        return argumentMatcher.argumentsFrom(step.getText());
    }

    @Override
    public String getLocation(boolean detail) {
        if (file == null) {
            List fileAndLine = toJava(stepdefRunner.callMethod("file_and_line"));
            file = (String) fileAndLine.get(0);
            line = (Long) fileAndLine.get(1);
        }
        return file + ":" + line;
    }

    @SuppressWarnings("unchecked")
    private List toJava(IRubyObject list) {
        return (List) list.toJava(List.class);
    }

    @Override
    public Integer getParameterCount() {
        IRubyObject paramCountR = stepdefRunner.callMethod("param_count");
        return Math.max(0, (Integer) paramCountR.toJava(Integer.class));
    }

    @Override
    public ParameterInfo getParameterType(int n, Type argumentType) {
        return new ParameterInfo(argumentType, null, null, null);
    }

    @Override
    public void execute(String language, Object[] args) throws Throwable {
        jRubyBackend.executeStepdef(stepdefRunner, language, args);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return file.equals(stackTraceElement.getFileName()) &&
                line < stackTraceElement.getLineNumber();
    }

    @Override
    public String getPattern() {
        return (String) stepdefRunner.callMethod("pattern").toJava(String.class);
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
