package cucumber.runtime.jruby;

import cucumber.runtime.Argument;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import gherkin.pickles.PickleStep;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Type;
import java.util.List;

public class JRubyStepDefinition implements StepDefinition {

    private final JRubyBackend jRubyBackend;
    private final RubyObject stepdefRunner;
    private String file;
    private Long line;

    public JRubyStepDefinition(JRubyBackend jRubyBackend, RubyObject stepdefRunner) {
        this.jRubyBackend = jRubyBackend;
        this.stepdefRunner = stepdefRunner;
    }

    @Override
    public List<Argument> matchedArguments(PickleStep step) {
        RubyString stepName = stepdefRunner.getRuntime().newString(step.getText());
        IRubyObject arguments = stepdefRunner.callMethod("matched_arguments", stepName);
        return toJava(arguments);
    }

    @SuppressWarnings("unchecked")
    private List<Argument> toJava(IRubyObject arguments) {
        return (List<Argument>) arguments.toJava(List.class);
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
