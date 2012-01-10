package cucumber.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.World;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

/**
 * Runs a scenario, or a "synthetic" scenario derived from an Examples row.
 */
class ExecutionUnitRunner extends ParentRunner<Step> {
    private final Runtime runtime;
    private final CucumberScenario cucumberScenario;
    private final JUnitReporter jUnitReporter;
    private World world;

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
    protected Description describeChild(Step step) {
        return Description.createSuiteDescription(step.getKeyword() + step.getName() + "(" + getName() + ")");
    }

    @Override
    public void run(RunNotifier notifier) {
        jUnitReporter.startExecutionUnit(this, notifier);

        world = cucumberScenario.newWorld(runtime);
        world.buildBackendWorldsAndRunBeforeHooks(jUnitReporter);
        cucumberScenario.runBackground(jUnitReporter.getFormatter(), jUnitReporter.getReporter());
        cucumberScenario.format(jUnitReporter);
        // Run the steps (the children)
        super.run(notifier);
        world.runAfterHooksAndDisposeBackendWorlds(jUnitReporter);

        jUnitReporter.finishExecutionUnit();
    }

    @Override
    protected void runChild(Step step, RunNotifier notifier) {
        cucumberScenario.runStep(step, jUnitReporter, world);
    }
}
