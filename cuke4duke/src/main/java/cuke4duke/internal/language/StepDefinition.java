package cuke4duke.internal.language;

import org.jruby.RubyArray;

import java.util.List;

public interface StepDefinition {
    String file_colon_line();

    void invoke(RubyArray args) throws Throwable;

    List<Group> groups(String stepName);
}
