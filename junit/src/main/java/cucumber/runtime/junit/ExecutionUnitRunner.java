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
class ExecutionUnitRunner extends ParentRunner<PickleStep> {
    private final Runner runner;
    private final PickleEvent pickleEvent;
    private final JUnitReporter jUnitReporter;
    private final Map<PickleStep, Description> stepDescriptions = new HashMap<PickleStep, Description>();
    private Description description;

    ExecutionUnitRunner(Runner runner, PickleEvent pickleEvent, JUnitReporter jUnitReporter) throws InitializationError {
        super(null);
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
            description = Description.createSuiteDescription(nameForDescription, new PickleId(pickleEvent));

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
            description = Description.createTestDescription(getName(), testName, new PickleStepId(pickleEvent, step));
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

    private static final class PickleId implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String uri;
        private int pickleLine;

        PickleId(PickleEvent pickleEvent) {
            this.uri = pickleEvent.uri;
            this.pickleLine = pickleEvent.pickle.getLocations().get(0).getLine();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PickleId that = (PickleId) o;
            return pickleLine == that.pickleLine && uri.equals(that.uri);
        }

        @Override
        public int hashCode() {
            int result = uri.hashCode();
            result = 31 * result + pickleLine;
            return result;
        }

        @Override
        public String toString() {
            return uri + ":" + pickleLine;
        }
    }

    private static final class PickleStepId implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String uri;
        private final int pickleLine;
        private final int pickleStepLine;

        PickleStepId(PickleEvent pickleEvent, PickleStep pickleStep) {
            this.uri = pickleEvent.uri;
            this.pickleLine = pickleEvent.pickle.getLocations().get(0).getLine();
            this.pickleStepLine = pickleStep.getLocations().get(0).getLine();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PickleStepId that = (PickleStepId) o;
            return pickleLine == that.pickleLine && pickleStepLine == that.pickleStepLine && uri.equals(that.uri);
        }

        @Override
        public int hashCode() {
            int result = pickleLine;
            result = 31 * result + uri.hashCode();
            result = 31 * result + pickleStepLine;
            return result;
        }

        @Override
        public String toString() {
            return uri + ":" + pickleLine + ":" + pickleStepLine;
        }
    }
}