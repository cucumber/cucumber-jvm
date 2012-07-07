package cucumber.runtime.jruby;

import cucumber.runtime.ParameterType;
import cucumber.runtime.StepDefinition;
import cucumber.table.DataTable;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
    public String getLocation(boolean detail) {
        if (file == null) {
            List fileAndLine = (List) stepdef.callMethod("file_and_line").toJava(List.class);
            file = (String) fileAndLine.get(0);
            line = (Long) fileAndLine.get(1);
        }
        return file + ":" + line;
    }

    @Override
    public Integer getParameterCount() {
        IRubyObject paramCountR = stepdef.callMethod("param_count");
        return Math.max(0, (Integer) paramCountR.toJava(Integer.class));
    }

    @Override
    public ParameterType getParameterType(int n, Type argumentType) {
        return new ParameterType(argumentType, null, null);
    }

    @Override
    public void execute(I18n i18n, Object[] args) throws Throwable {
        ArrayList<IRubyObject> jrubyArgs = new ArrayList<IRubyObject>();

        jrubyArgs.add(JavaEmbedUtils.javaToRuby(stepdef.getRuntime(), i18n));

        for (Object o : args) {
            if (o == null) {
                jrubyArgs.add(null);
            } else if (o instanceof DataTable) {
                //Add a datatable as it stands...
                jrubyArgs.add(JavaEmbedUtils.javaToRuby(stepdef.getRuntime(), o));
            } else {
                jrubyArgs.add(stepdef.getRuntime().newString((String) o));
            }
        }

        stepdef.callMethod("execute", jrubyArgs.toArray(new IRubyObject[jrubyArgs.size()]));
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
}
