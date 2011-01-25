package cucumber.runtime;

import gherkin.formatter.Reporter;

public interface StepRunner {
    /**
     *
     * @param skip              whether or not to skip running a step.
     * @param reporter          object handling the result.
     * @param stepLocation      location of the step  @return true if next step should be skipped
     * @return true if next step should be skipped.
     * @deprecated Use run()
     */
    @Deprecated
    boolean execute(boolean skip, Reporter reporter, StackTraceElement stepLocation);

    boolean canRun();

    void run() throws Throwable;
}
