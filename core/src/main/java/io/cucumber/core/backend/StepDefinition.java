package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.util.List;

@API(status = API.Status.STABLE)
public interface StepDefinition extends io.cucumber.core.event.StepDefinition {
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
     * stack traces.
     */
    boolean isDefinedAt(StackTraceElement stackTraceElement);

    /**
     * @return parameter information or null when the language does not provide parameter information
     */
    List<ParameterInfo> parameterInfos();

}
