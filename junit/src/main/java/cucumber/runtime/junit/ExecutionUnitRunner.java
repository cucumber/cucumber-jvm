package cucumber.runtime.junit;

import cucumber.runner.Runner;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleStep;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs a scenario, or a "synthetic" scenario derived from an Examples row.
 */
public class ExecutionUnitRunner extends ParentRunner<PickleStep> {
    private final Runner runner;
    private final PickleEvent pickleEvent;
    private final JUnitReporter jUnitReporter;
    private final Map<PickleStep, Description> stepDescriptions = new HashMap<PickleStep, Description>();
    private Description description;

    public ExecutionUnitRunner(Runner runner, PickleEvent pickleEvent, JUnitReporter jUnitReporter) throws InitializationError {
        super(ExecutionUnitRunner.class);
        this.runner = runner;
        this.pickleEvent = pickleEvent;
        this.jUnitReporter = jUnitReporter;
    }

    @Override
    protected List<PickleStep> getChildren() {
        return pickleEvent.pickle.getSteps();
    }

    @Override
    public String getName() {
        String name = pickleEvent.pickle.getName();
        if (jUnitReporter.useFilenameCompatibleNames()) {
            return makeNameFilenameCompatible(name);
        } else {
            return name;
        }
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            String nameForDescription = getName().isEmpty() ? "EMPTY_NAME" : getName();
            description = Description.createSuiteDescription(nameForDescription, new PickleWrapper(pickleEvent));

            for (PickleStep step : getChildren()) {
                description.addChild(describeChild(step));
            }
        }
        return description;
    }

    @Override
    protected Description describeChild(PickleStep step) {
        Description description = stepDescriptions.get(step);
        if (description == null) {
            String testName;
            if (jUnitReporter.useFilenameCompatibleNames()) {
                testName = makeNameFilenameCompatible(step.getText());
            } else {
                testName = step.getText();
            }
            description = Description.createTestDescription(getName(), testName, new PickleStepWrapper(step));
            stepDescriptions.put(step, description);
        }
        return description;
    }

    @Override
    public void run(final RunNotifier notifier) {
        jUnitReporter.startExecutionUnit(this, notifier);
        // This causes runChild to never be called, which seems OK.
        runner.runPickle(pickleEvent);
        jUnitReporter.finishExecutionUnit();
    }

    @Override
    protected void runChild(PickleStep step, RunNotifier notifier) {
        // The way we override run(RunNotifier) causes this method to never be called.
        // Instead it happens via cucumberScenario.run(jUnitReporter, jUnitReporter, runtime);
        throw new UnsupportedOperationException();
        // cucumberScenario.runStep(step, jUnitReporter, runtime);
    }

    private String makeNameFilenameCompatible(String name) {
        return name.replaceAll("[^A-Za-z0-9_]", "_");
    }
}

class PickleWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    private PickleEvent pickleEvent;

    public PickleWrapper(PickleEvent pickleEvent) {
        this.pickleEvent = pickleEvent;
    }
}

class PickleStepWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    private PickleStep step;

    public PickleStepWrapper(PickleStep step) {
        this.step = step;
    }
}
