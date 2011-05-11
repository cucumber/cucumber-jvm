package cucumber.runtime.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.World;
import gherkin.formatter.Reporter;
import gherkin.model.Match;
import gherkin.model.Result;
import gherkin.model.Step;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

public class ScenarioRunner extends ParentRunner<Step> {
    private final Runtime runtime;
    private final String name;
    private final List<Step> steps;
    private World world;

    public ScenarioRunner(Runtime runtime, String name, List<Step> steps) throws InitializationError {
        super(null);
        this.runtime = runtime;
        this.name = name;
        this.steps = steps;
    }

    @Override
    public String getName() {
        return name;
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
        world.runStep(step, reporter);
    }

    private Reporter makeReporter(Step step, RunNotifier notifier) {
        Description description = describeChild(step);
        final EachTestNotifier eachTestNotifier = new EachTestNotifier(notifier, description);
        return new JUnitReporter(eachTestNotifier);
    }

    private static class JUnitReporter implements Reporter {
        private final EachTestNotifier eachTestNotifier;

        public JUnitReporter(EachTestNotifier eachTestNotifier) {
            this.eachTestNotifier = eachTestNotifier;
        }

        public void match(Match match) {
            if (match == Match.UNDEFINED) {
                eachTestNotifier.fireTestIgnored();
            } else {
                eachTestNotifier.fireTestStarted();
            }
        }

        public void embedding(String mimeType, byte[] data) {
        }

        public void result(Result result) {
            if (result == Result.SKIPPED) {
                eachTestNotifier.fireTestIgnored();
            } else {
                if (result.getError() != null) {
                    eachTestNotifier.addFailure(result.getError());
                }
                eachTestNotifier.fireTestFinished();
            }
        }
    }
}
