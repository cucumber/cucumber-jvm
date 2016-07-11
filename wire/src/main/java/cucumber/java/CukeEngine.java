package cucumber.java;

import cucumber.java.exception.InvokeException;
import cucumber.java.exception.InvokeFailureException;
import cucumber.java.exception.PendingStepException;
import cucumber.java.step.StepManager;

import java.util.List;

public interface CukeEngine {
    /**
     * Finds steps whose regexp match some text.
     */
    List<StepMatch> stepMatches(String name);

    /**
     * Starts a scenario.
     */
    void beginScenario(List<String> tags) throws Throwable;

    /**
     * Invokes a step passing arguments to it.
     *
     * @throws InvokeException if the test fails or it is pending
     */
    void invokeStep(String id, List<String> args, List<List<String>> tableArg) throws InvokeException, InvokeFailureException, PendingStepException;

    /**
     * Ends a scenario.
     */
    void endScenario(List<String> tags) throws Throwable;

    /**
     * Returns the step definition for a pending step.
     */
    String snippetText(String keyword, String name, String multilineArgClass);
}
