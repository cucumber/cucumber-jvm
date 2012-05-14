package cucumber.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cucumber.junit.DescriptionFactory.createDescription;

/**
 * Runs a scenario, or a "synthetic" scenario derived from an Examples row.
 */
class ExecutionUnitRunner extends ParentRunner<Step> {
    private final Runtime runtime;
    private final CucumberScenario cucumberScenario;
    private final JUnitReporter jUnitReporter;
    private Description description;
    private final Map<Step, Description> stepDescriptions = new HashMap<Step, Description>();

    public ExecutionUnitRunner(Runtime runtime, CucumberScenario cucumberScenario, JUnitReporter jUnitReporter) throws InitializationError {
        super(ExecutionUnitRunner.class);
        this.runtime = runtime;
        this.cucumberScenario = cucumberScenario;
        this.jUnitReporter = jUnitReporter;
    }

    @Override
    protected List<Step> getChildren() {
        return cucumberScenario.getSteps();
    }

    @Override
    public String getName() {
        return cucumberScenario.getVisualName();
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = createDescription(getName(), cucumberScenario);

            if (cucumberScenario.getCucumberBackground() != null) {
                for (Step backgroundStep : cucumberScenario.getCucumberBackground().getSteps()) {
                    description.addChild(describeChild(backgroundStep));
                }
            }

            for (Step step : getChildren()) {
                description.addChild(describeChild(step));
            }
        }
        return description;
    }

    @Override
    protected Description describeChild(Step step) {
        Description description = stepDescriptions.get(step);
        if (description == null) {
            description = createDescription(step.getKeyword() + step.getName(), step);
            stepDescriptions.put(step, description);
        }
        return description;
    }

    @Override
    public void run(final RunNotifier notifier) {
        jUnitReporter.startExecutionUnit(this, notifier);
        // This causes runChild to never be called, which seems OK.
        cucumberScenario.run(jUnitReporter, jUnitReporter, runtime);
        jUnitReporter.finishExecutionUnit();
    }

    @Override
    protected void runChild(Step step, RunNotifier notifier) {
        // The way we override run(RunNotifier) causes this method to never be called.
        // Instead it happens via cucumberScenario.run(jUnitReporter, jUnitReporter, runtime);
        throw new UnsupportedOperationException();
        // cucumberScenario.runStep(step, jUnitReporter, runtime);
    }
}
