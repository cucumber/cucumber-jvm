package cucumber.runtime.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs a scenario, or a "synthetic" scenario derived from an Examples row.
 */
public class ExecutionUnitRunner extends ParentRunner<Step> {
    private final Runtime runtime;
    private final CucumberScenario cucumberScenario;
    private final JUnitReporter jUnitReporter;
    private Description description;
    private final Map<Step, Description> stepDescriptions = new HashMap<Step, Description>();
    private final List<Step> runnerSteps = new ArrayList<Step>();

    public ExecutionUnitRunner(Runtime runtime, CucumberScenario cucumberScenario, JUnitReporter jUnitReporter) throws InitializationError {
        super(ExecutionUnitRunner.class);
        this.runtime = runtime;
        this.cucumberScenario = cucumberScenario;
        this.jUnitReporter = jUnitReporter;
    }

    public List<Step> getRunnerSteps() {
    	return runnerSteps;
    }

    @Override
    protected List<Step> getChildren() {
        return cucumberScenario.getSteps();
    }

    @Override
    public String getName() {
        String name = cucumberScenario.getVisualName();
        if (jUnitReporter.useFilenameCompatibleNames()) {
            return makeNameFilenameCompatible(name);
        } else {
            return name;
        }
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), cucumberScenario.getGherkinModel());

            if (cucumberScenario.getCucumberBackground() != null) {
                for (Step backgroundStep : cucumberScenario.getCucumberBackground().getSteps()) {
                    // We need to make a copy of that step, so we have a unique one per scenario
                    Step copy = new Step(
                            backgroundStep.getComments(),
                            backgroundStep.getKeyword(),
                            backgroundStep.getName(),
                            backgroundStep.getLine(),
                            backgroundStep.getRows(),
                            backgroundStep.getDocString()
                    );
                    description.addChild(describeChild(copy));
                    runnerSteps.add(copy);
                }
            }

            for (Step step : getChildren()) {
                description.addChild(describeChild(step));
                runnerSteps.add(step);
            }
        }
        return description;
    }

    @Override
    protected Description describeChild(Step step) {
        Description description = stepDescriptions.get(step);
        if (description == null) {
            String testName;
            if (jUnitReporter.useFilenameCompatibleNames()) {
                testName = makeNameFilenameCompatible(step.getKeyword() + step.getName());
            } else {
                testName = step.getKeyword() + step.getName();
            }
            description = Description.createTestDescription(getName(), testName, step);
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

    private String makeNameFilenameCompatible(String name) {
        return name.replaceAll("[^A-Za-z0-9_]", "_");
    }
}
