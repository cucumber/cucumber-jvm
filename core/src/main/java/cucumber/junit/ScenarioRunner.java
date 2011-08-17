package cucumber.junit;

import cucumber.runtime.Pending;
import cucumber.runtime.Runtime;
import cucumber.runtime.World;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScenarioRunner extends ParentRunner<Step> {
    private final Runtime runtime;
    private final Scenario scenario;
    private final List<Step> steps = new ArrayList<Step>();
    private World world;
    private Locale locale;

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
        return steps;
    }

    @Override
    protected Description describeChild(Step step) {
        return Description.createSuiteDescription(step.getKeyword() + step.getName());
    }

    @Override
    public void run(RunNotifier notifier) {
        world = runtime.newWorld();
        super.run(notifier);
        world.dispose();
    }

    @Override
    protected void runChild(Step step, RunNotifier notifier) {
        Reporter reporter = makeReporter(step, notifier);
        world.runStep(step, getName(), reporter, locale);
    }

    private Reporter makeReporter(Step step, RunNotifier notifier) {
        Description description = describeChild(step);
        final EachTestNotifier eachTestNotifier = new EachTestNotifier(notifier, description);
        return new JUnitReporter(eachTestNotifier);
    }

    public void step(Step step) {
        steps.add(step);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    private static class JUnitReporter implements Reporter {
        private final EachTestNotifier eachTestNotifier;
        private Match match;

        public JUnitReporter(EachTestNotifier eachTestNotifier) {
            this.eachTestNotifier = eachTestNotifier;
        }

        public void match(Match match) {
            this.match = match;
            if (match == Match.NONE) {
                eachTestNotifier.fireTestIgnored();
            } else {
                eachTestNotifier.fireTestStarted();
            }
        }

        public void embedding(String mimeType, byte[] data) {
        }

        public void result(Result result) {
            Throwable error = result.getError();
            if (Result.SKIPPED == result || error instanceof Pending) {
                if (match != Match.NONE) {
                    // No need to say it's ignored twice
                    eachTestNotifier.fireTestIgnored();
                }
            } else {
                if (error != null) {
                    eachTestNotifier.addFailure(error);
                }
                eachTestNotifier.fireTestFinished();
            }
        }
    }
}
