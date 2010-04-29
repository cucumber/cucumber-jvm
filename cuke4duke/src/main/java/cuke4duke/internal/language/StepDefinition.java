package cuke4duke.internal.language;

import java.util.List;

public interface StepDefinition {
    String regexp_source() throws Throwable;

    String file_colon_line() throws Throwable;

    /**
     * Returns a list of arguments if our regexp matches stepName. If it doesn't
     * match, return null.
     *
     * @param stepName the name of the step
     * @return a list of arguments
     * @throws Throwable when anything inside blows up
     */
    List<StepArgument> arguments_from(String stepName) throws Throwable;

    void invoke(List<Object> arguments) throws Throwable;
}
