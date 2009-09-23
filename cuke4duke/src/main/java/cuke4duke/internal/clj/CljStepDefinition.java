package cuke4duke.internal.clj;

import cuke4duke.internal.language.StepDefinition;
import cuke4duke.internal.language.Group;
import org.jruby.RubyArray;

import java.util.List;

public class CljStepDefinition implements StepDefinition {
    public String file_colon_line() {
        throw new RuntimeException("Not implemented");
    }

    public void invoke(RubyArray args) throws Throwable {
        throw new RuntimeException("Not implemented");
    }

    public List<Group> groups(String stepName) {
        throw new RuntimeException("Not implemented");
    }
}
