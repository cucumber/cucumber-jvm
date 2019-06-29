package io.cucumber.core.backend;

import io.cucumber.core.stepexpression.Argument;
import gherkin.pickles.PickleStep;
import org.apiguardian.api.API;

import java.util.List;

@API(status = API.Status.STABLE)
public interface StepDefinition extends io.cucumber.core.event.StepDefinition {
    /**
     * Returns a list of arguments. Return null if the step definition
     * doesn't match at all. Return an empty List if it matches with 0 arguments
     * and bigger sizes if it matches several.
     * 
     * @param step The step to match arguments for
     * @return The arguments in a list when the step matches, null otherwise.
     */
    List<Argument> matchedArguments(PickleStep step);

    /**
     * Invokes the step definition. The method should raise a Throwable
     * if the invocation fails, which will cause the step to fail.
     * 
     * @param args The arguments for the step
     * @throws Throwable in case of step failure.
     */
    void execute(Object[] args) throws Throwable;

    /**
     * @param stackTraceElement The location of the step.
     * @return Return true if this matches the location. This is used to filter
     *         stack traces.
     */
    boolean isDefinedAt(StackTraceElement stackTraceElement);

    /**
     * @return How many declared parameters this step definition has. Returns null if unknown.
     */
    Integer getParameterCount();
}
