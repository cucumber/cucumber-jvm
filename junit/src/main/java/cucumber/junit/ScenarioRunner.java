package cucumber.junit;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberBackground;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

class ScenarioRunner extends ParentRunner<Step> {
    private final Runtime runtime;
    private final List<String> extraCodePaths;
    private final CucumberScenario cucumberScenario;
    private final JUnitReporter jUnitReporter;

    public ScenarioRunner(Runtime runtime, List<String> extraCodePaths, CucumberScenario cucumberScenario, JUnitReporter jUnitReporter) throws InitializationError {
        super(null);
        this.runtime = runtime;
        this.extraCodePaths = extraCodePaths;
        this.cucumberScenario = cucumberScenario;
        this.jUnitReporter = jUnitReporter;
    }

    @Override
    public String getName() {
        return cucumberScenario.getName();
    }

    @Override
    protected List<Step> getChildren() {
        return cucumberScenario.getSteps();
    }

    @Override
    protected Description describeChild(Step step) {
        // use scenario and step as class and method names (in order to generate useable JUnit reports)
        String className = getName();
        String methodName = step.getKeyword() + step.getName();
        String formattedDescription = String.format("%s(%s)", methodName, className);
        return Description.createSuiteDescription(formattedDescription);
    }

    @Override
    public void run(RunNotifier notifier) {
        jUnitReporter.setRunner(this, notifier);
        try {
            runtime.createWorld(extraCodePaths, cucumberScenario.tags());

            CucumberBackground cucumberBackground = cucumberScenario.getCucumberBackground();
            if (cucumberBackground != null) {
                runBackground(notifier, cucumberBackground);
            }

            cucumberScenario.format(jUnitReporter);
        } catch (CucumberException e) {
            notifier.fireTestFailure(new Failure(getDescription(), e));
        } catch (Throwable e) {
            // bug
            e.printStackTrace();
        }
        super.run(notifier);
        try {
            runtime.disposeWorld();
        } catch (CucumberException e) {
            notifier.fireTestFailure(new Failure(getDescription(), e));
        }
    }

    /*
    We're running the background without reporting the steps as junit children - we don't want them to show up in the
    junit report. However, if any of the background steps fail, we mark the entire scenario as failed. Scenario steps
    will be skipped.
     */
    private void runBackground(RunNotifier notifier, CucumberBackground cucumberBackground) {
        cucumberBackground.format(jUnitReporter.getFormatter());
        List<Step> steps = cucumberBackground.getSteps();
        Throwable failure = null;
        for (Step step : steps) {
            Throwable e = runtime.runStep(cucumberScenario.getUri(), step, jUnitReporter.getReporter(), cucumberScenario.getLocale());
            if (e != null) {
                failure = e;
            }
        }
        if (failure != null) {
            notifier.fireTestFailure(new Failure(getDescription(), failure));
        }
    }

    @Override
    protected void runChild(Step step, RunNotifier notifier) {
        runtime.runStep(cucumberScenario.getUri(), step, jUnitReporter, cucumberScenario.getLocale());
    }
}
