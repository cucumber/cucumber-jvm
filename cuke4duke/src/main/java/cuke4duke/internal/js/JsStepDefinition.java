package cuke4duke.internal.js;

import cuke4duke.internal.language.StepDefinition;
import cuke4duke.internal.language.StepArgument;
import org.jruby.RubyArray;

import java.util.List;

public class JsStepDefinition implements StepDefinition {
    public String regexp_source() {
        throw new RuntimeException("Not implemented");
    }

    public String file_colon_line() {
        throw new RuntimeException("Not implemented");
    }

    public void invoke(RubyArray args) throws Throwable {
        throw new RuntimeException("Not implemented");
    }

    public List<StepArgument> arguments_from(String stepName) {
        throw new RuntimeException("Not implemented");
    }
}
