package cucumber.runtime.jruby;

import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import cucumber.table.Table;
import cucumber.table.TableConverter;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Step;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;

public class JRubyStepDefinition implements StepDefinition {

    private final RubyObject stepdef;
    private String file;
    private Long line;

    public JRubyStepDefinition(RubyObject stepdef) {
        this.stepdef = stepdef;
    }

    @Override
    public List<Argument> matchedArguments(Step step) {
        RubyString stepName = stepdef.getRuntime().newString(step.getName());
        IRubyObject arguments = stepdef.callMethod("matched_arguments", stepName);
        return (List<Argument>) arguments.toJava(List.class);
    }

    @Override
    public String getLocation() {
        if (file == null) {
            List fileAndLine = (List) stepdef.callMethod("file_and_line").toJava(List.class);
            file = (String) fileAndLine.get(0);
            line = (Long) fileAndLine.get(1);
        }
        return file + ":" + line;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        IRubyObject argCountR = stepdef.callMethod("arg_count");
        int argCount = (Integer) argCountR.toJava(Integer.class);
        return Utils.classArray(Math.max(0, argCount), String.class);
    }

    @Override
    public void execute(Object[] args) throws Throwable {
        IRubyObject[] jrybyArgs = new IRubyObject[args.length];
        for (int i = 0; i < args.length; i++) {
            jrybyArgs[i] = stepdef.getRuntime().newString((String) args[i]);
        }
        stepdef.callMethod("execute", jrybyArgs);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return file.equals(stackTraceElement.getFileName()) &&
                line < stackTraceElement.getLineNumber();
    }

    @Override
    public String getPattern() {
        return (String) stepdef.callMethod("pattern").toJava(String.class);
    }

    @Override
    public Object tableArgument(int argIndex, List<Row> rows, TableConverter tableConverter) {
        return new Table(rows);
    }
}
