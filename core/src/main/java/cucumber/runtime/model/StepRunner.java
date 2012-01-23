package cucumber.runtime.model;

import cucumber.runtime.Runtime;
import gherkin.formatter.Reporter;

public interface StepRunner {
    void runSteps(Reporter reporter, Runtime runtime);
}
