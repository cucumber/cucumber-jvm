package cucumber.runtime;

import gherkin.formatter.Formatter;

public interface StepRunner {
    /**
     * @param skip whether or not to skip running a step.
     * @param formatter formatter that will be notified when the step is run
     * @param stepLocation location of the step
     * @return true if next step should be skipped
     */
    boolean execute(boolean skip, Formatter formatter, StackTraceElement stepLocation);
}
