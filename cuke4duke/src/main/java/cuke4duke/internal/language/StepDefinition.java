package cuke4duke.internal.language;

import org.jruby.RubyArray;

import java.util.List;

import ioke.lang.exceptions.ControlFlow;

public interface StepDefinition {
    String regexp_source() throws Throwable;
    String file_colon_line() throws Throwable;

    /**
     * Returns a list of arguments if our regexp matches stepName. If it doesn't
     * match, return null.
     * 
     * @param stepName
     * @return
     */
    List<StepArgument> arguments_from(String stepName) throws Throwable;
    void invoke(RubyArray args) throws Throwable;
}
