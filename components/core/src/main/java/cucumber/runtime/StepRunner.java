package cucumber.runtime;

import gherkin.formatter.Formatter;

public interface StepRunner {
    boolean execute(boolean skip, Formatter formatter, StackTraceElement stepStackTraceElement);
}
