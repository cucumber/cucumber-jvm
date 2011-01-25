package cucumber.runtime.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.StepRunner;
import gherkin.formatter.model.FeatureElement;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

public class ScenarioRunner extends ParentRunner<Step> {
    private final Runtime runtime;
    private final FeatureElement scenario;
    private boolean failed;

    public ScenarioRunner(Runtime runtime, Scenario scenario) throws InitializationError {
        super(null);
        this.runtime = runtime;
        this.scenario = scenario;
    }

    @Override
    public String getName() {
        return scenario.getKeyword() + ": " + scenario.getName();
    }

    @Override
    protected List<Step> getChildren() {
        return scenario.getSteps();
    }

    @Override
    protected Description describeChild(Step step) {
        return Description.createSuiteDescription(step.getKeyword() + step.getName());
    }

    @Override
    public void run(RunNotifier notifier) {
        runtime.newWorld();
        super.run(notifier);
    }

    @Override
    protected void runChild(Step step, RunNotifier notifier) {
        EachTestNotifier eachNotifier = makeNotifier(step, notifier);

        if(failed) {
            eachNotifier.fireTestIgnored();
            return;
        }
        
        StepRunner stepRunner = runtime.stepRunner(step);
        if(!stepRunner.canRun()) {
            eachNotifier.fireTestIgnored();
            return;
        }

        eachNotifier.fireTestStarted();
        try {
            stepRunner.run();
        } catch (Throwable e) {
            failed = true;
            eachNotifier.addFailure(e);
        } finally {
            eachNotifier.fireTestFinished();
        }
    }

    private EachTestNotifier makeNotifier(Step step, RunNotifier notifier) {
        Description description = describeChild(step);
        return new EachTestNotifier(notifier, description);
    }
}
