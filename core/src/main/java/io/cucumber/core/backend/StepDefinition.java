package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.util.List;

@API(status = API.Status.STABLE)
public interface StepDefinition extends Located {

    /**
     * Invokes the step definition. The method should raise a Throwable if the
     * invocation fails, which will cause the step to fail.
     *
     * @param  args                              The arguments for the step
     * @throws CucumberBackendException          of a failure to invoke the step
     * @throws CucumberInvocationTargetException in case of a failure in the
     *                                           step.
     */
    void execute(Object[] args) throws CucumberBackendException, CucumberInvocationTargetException;

    /**
     * @return parameter information, may not return null
     */
    List<ParameterInfo> parameterInfos();

    /**
     * @return the pattern associated with this instance. Used for error
     *         reporting only.
     */
    String getPattern();

}
