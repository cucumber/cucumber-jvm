package cucumber.runtime;

import gherkin.formatter.model.Step;

public interface StepRunner {
    /**
     * @param skip              whether or not to skip running a step.
     * @param stepResultHandler object handling the result.
     * @param stepLocation      location of the step  @return true if next step should be skipped
     * @param step              the executed step
     * @return true if next step should be skipped.
     */
    boolean execute(boolean skip, StepResultHandler stepResultHandler, StackTraceElement stepLocation, Step step);
}
