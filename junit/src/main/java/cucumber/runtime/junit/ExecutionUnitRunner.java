package cucumber.runtime.junit;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberScenario;

/**
 * Runs a scenario, or a "synthetic" scenario derived from an Examples row.
 */
public class ExecutionUnitRunner extends Runner {
    
    private final Runtime runtime;
    private final CucumberScenario cucumberScenario;
    private final JUnitReporter jUnitReporter;
    private final Description description;

    public ExecutionUnitRunner(Class<?> testClass, Runtime runtime, CucumberScenario cucumberScenario, JUnitReporter jUnitReporter, String uri) throws InitializationError {
        this.runtime = runtime;
        this.cucumberScenario = cucumberScenario;
        this.jUnitReporter = jUnitReporter;
        String uniqueId = uri + ":" + cucumberScenario.getGherkinModel().getLine();
        this.description = Description.createTestDescription(testClass, cucumberScenario.getVisualName() + " -- " + uniqueId);
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public void run(final RunNotifier notifier) {
        jUnitReporter.startExecutionUnit(this, notifier);
        // This causes runChild to never be called, which seems OK.
        cucumberScenario.run(jUnitReporter, jUnitReporter, runtime);
        jUnitReporter.finishExecutionUnit();
    }

}
