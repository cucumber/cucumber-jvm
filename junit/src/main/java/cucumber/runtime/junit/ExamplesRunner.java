package cucumber.runtime.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenario;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class ExamplesRunner extends Suite {
    private final CucumberExamples cucumberExamples;
    private final IdProvider idProvider;
    private Description description;
    private JUnitReporter jUnitReporter;

    protected ExamplesRunner(Runtime runtime, CucumberExamples cucumberExamples, JUnitReporter jUnitReporter, IdProvider idProvider) throws InitializationError {
        super(ExamplesRunner.class, buildRunners(runtime, cucumberExamples, jUnitReporter, idProvider));
        this.cucumberExamples = cucumberExamples;
        this.jUnitReporter = jUnitReporter;
        this.idProvider = idProvider;
    }

    private static List<Runner> buildRunners(Runtime runtime, CucumberExamples cucumberExamples, JUnitReporter jUnitReporter, IdProvider idProvider) {
        List<Runner> runners = new ArrayList<Runner>();
        List<CucumberScenario> exampleScenarios = cucumberExamples.createExampleScenarios();
        for (CucumberScenario scenario : exampleScenarios) {
            try {
                ExecutionUnitRunner exampleScenarioRunner = new ExecutionUnitRunner(runtime, scenario, jUnitReporter, idProvider);
                runners.add(exampleScenarioRunner);
            } catch (InitializationError initializationError) {
                initializationError.printStackTrace();
            }
        }
        return runners;
    }

    @Override
    protected String getName() {
        return cucumberExamples.getExamples().getKeyword() + ": " + cucumberExamples.getExamples().getName();
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), idProvider.next());
            for (Runner child : getChildren()) {
                description.addChild(describeChild(child));
            }
        }
        return description;
    }

    @Override
    public void run(final RunNotifier notifier) {
        jUnitReporter.examples(cucumberExamples.getExamples());
        super.run(notifier);
    }
}
