package cucumber.junit;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;

import java.util.List;

import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.CucumberException;
import cucumber.runtime.PendingException;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberScenario;

public class ScenarioRunner extends ParentRunner<Step> {
    private final Runtime runtime;
    private final CucumberScenario cucumberScenario;

    public ScenarioRunner(Runtime runtime, CucumberScenario cucumberScenario) throws InitializationError {
        super(null);
        this.runtime = runtime;
        this.cucumberScenario = cucumberScenario;
    }

    @Override
    public String getName() {
        Scenario scenario = cucumberScenario.getScenario();
        return scenario.getKeyword() + ": " + scenario.getName();
    }

    @Override
    protected List<Step> getChildren() {
        return cucumberScenario.getSteps();
    }

    @Override
    protected Description describeChild(Step step) {
        return Description.createSuiteDescription(step.getKeyword() + step.getName());
    }

    @Override
    public void run(RunNotifier notifier) {
    	try {
    		cucumberScenario.newWorld(runtime);
    	} catch (CucumberException e) {
    		notifier.fireTestFailure(new Failure(getDescription(),e));
		}        
        super.run(notifier);
        try {
        	cucumberScenario.disposeWorld();
        } catch (CucumberException e) {
    		notifier.fireTestFailure(new Failure(getDescription(),e));
		} 
    }

    @Override
    protected void runChild(Step step, RunNotifier notifier) {
        Reporter reporter = makeReporter(step, notifier);
        cucumberScenario.runStep(step, reporter);
    }

    private Reporter makeReporter(Step step, RunNotifier notifier) {
        Description description = describeChild(step);
        final EachTestNotifier eachTestNotifier = new EachTestNotifier(notifier, description);
        return new JUnitReporter(eachTestNotifier);
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
            if (Result.SKIPPED == result || error instanceof PendingException) {
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
