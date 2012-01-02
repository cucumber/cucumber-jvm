package cucumber.runtime.jruby;

import cucumber.runtime.ParameterType;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.formatter.Argument;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;
import java.util.Locale;

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
    public List<ParameterType> getParameterTypes() {
        IRubyObject argCountR = stepdef.callMethod("arg_count");
        int argCount = (Integer) argCountR.toJava(Integer.class);
        return Utils.listOf(Math.max(0, argCount), new ParameterType(String.class, null));
    }

    @Override
    public void execute(String uri, Reporter reporter, Locale locale, Object[] args) throws Throwable {
        IRubyObject[] jrybyArgs = new IRubyObject[args.length + 3];
        
        jrybyArgs[0] = JavaEmbedUtils.javaToRuby(stepdef.getRuntime(), uri);
        jrybyArgs[1] = JavaEmbedUtils.javaToRuby(stepdef.getRuntime(), reporter);
        jrybyArgs[2] = JavaEmbedUtils.javaToRuby(stepdef.getRuntime(), locale);

        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                jrybyArgs[i + 3] = stepdef.getRuntime().newString((String) args[i]);
            } else {
                jrybyArgs[i + 3] = null;
            }
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
}
