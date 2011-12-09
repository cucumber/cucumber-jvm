package cucumber.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.World;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

/**
 * Runs a scenario, or a "synthetic" scenario derived from an Examples row.
 */
class ExecutionUnitRunner extends ParentRunner<Step> {
    private final Runtime runtime;
    private final List<String> gluePaths;
    private final CucumberScenario cucumberScenario;
    private final JUnitReporter jUnitReporter;
    private World world;

    public ExecutionUnitRunner(Runtime runtime, List<String> gluePaths, CucumberScenario cucumberScenario, JUnitReporter jUnitReporter) throws InitializationError {
        super(ExecutionUnitRunner.class);
        this.runtime = runtime;
        this.gluePaths = gluePaths;
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
        jUnitReporter.setStepParentRunner(this, notifier);
        /*
           We're running the hooks and background without reporting the steps as junit children - we don't want them to show up in the
           junit report. However, if any of the before hooks or background steps fail, we mark the entire scenario as failed. Scenario steps
           will be skipped.
        */
        try {
            world = cucumberScenario.buildWorldAndRunBeforeHooks(gluePaths, runtime);
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(getDescription(), e));
        }
        try {
            cucumberScenario.runBackground(jUnitReporter.getFormatter(), jUnitReporter.getReporter());
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(getDescription(), e));
        }

        // Run the steps
        cucumberScenario.format(jUnitReporter);
        super.run(notifier);
        try {
            cucumberScenario.runAfterHooksAndDisposeWorld();
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(getDescription(), e));
        }
    }

    @Override
    protected void runChild(Step step, RunNotifier notifier) {
        try {
            cucumberScenario.runStep(step, jUnitReporter, world);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
