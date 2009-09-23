package cuke4duke.internal.language;

import org.jruby.RubyArray;

import java.util.List;

public interface StepDefinition {
    String regexp_source();
    String file_colon_line();
    void invoke(RubyArray args) throws Throwable;
    List<StepArgument> arguments_from(String stepName);
}
