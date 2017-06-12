package cucumber.runtime.junit;

import cucumber.runner.Runner;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



class PickleRunners {

    interface PickleRunner {
        void run(RunNotifier notifier);

        Description getDescription();

        Description describeChild(PickleStep step);

    }

    static PickleRunner withStepDescriptions(cucumber.runner.Runner runner, PickleEvent pickleEvent, JUnitReporter jUnitReporter) throws InitializationError {
        return new WithStepDescriptions(runner, pickleEvent, jUnitReporter);
    }


    static PickleRunner withNoStepDescriptions(String featureName, cucumber.runner.Runner runner, PickleEvent pickleEvent, JUnitReporter jUnitReporter) throws InitializationError {
        return new NoStepDescriptions(featureName, runner, pickleEvent, jUnitReporter);
    }


    static class WithStepDescriptions extends ParentRunner<PickleStep> implements PickleRunner {
        private final Runner runner;
        private final PickleEvent pickleEvent;
        private final JUnitReporter jUnitReporter;
        private final Map<PickleStep, Description> stepDescriptions = new HashMap<PickleStep, Description>();
        private Description description;

        WithStepDescriptions(Runner runner, PickleEvent pickleEvent, JUnitReporter jUnitReporter) throws InitializationError {
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
            return getPickleName(pickleEvent, jUnitReporter.useFilenameCompatibleNames());
        }

        @Override
        public Description getDescription() {
            if (description == null) {
                description = Description.createSuiteDescription(getName(), new PickleId(pickleEvent));
                for (PickleStep step : getChildren()) {
                    description.addChild(describeChild(step));
                }
            }
            return description;
        }

        @Override
        public Description describeChild(PickleStep step) {
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
        }

        @Override
        protected void runChild(PickleStep step, RunNotifier notifier) {
            // The way we override run(RunNotifier) causes this method to never be called.
            // Instead it happens via cucumberScenario.run(jUnitReporter, jUnitReporter, runtime);
            throw new UnsupportedOperationException();
        }

    }


    static final class NoStepDescriptions implements PickleRunner {
        private final String featureName;
        private final cucumber.runner.Runner runner;
        private final PickleEvent pickleEvent;
        private final JUnitReporter jUnitReporter;
        private Description description;

        NoStepDescriptions(String featureName, cucumber.runner.Runner runner, PickleEvent pickleEvent, JUnitReporter jUnitReporter) throws InitializationError {
            this.featureName = featureName;
            this.runner = runner;
            this.pickleEvent = pickleEvent;
            this.jUnitReporter = jUnitReporter;
        }

        @Override
        public Description getDescription() {
            if (description == null) {
                String className = createName(featureName, jUnitReporter.useFilenameCompatibleNames());
                String name = getPickleName(pickleEvent, jUnitReporter.useFilenameCompatibleNames());
                description = Description.createTestDescription(className, name, new PickleId(pickleEvent));
            }
            return description;
        }

        @Override
        public Description describeChild(PickleStep step) {
            throw new UnsupportedOperationException("This pickle runner does not wish to describe its children");
        }

        @Override
        public void run(final RunNotifier notifier) {
            jUnitReporter.startExecutionUnit(this, notifier);
            runner.runPickle(pickleEvent);
        }
    }

    private static String getPickleName(PickleEvent pickleEvent, boolean useFilenameCompatibleNames) {
        final String name = pickleEvent.pickle.getName();
        return createName(name, useFilenameCompatibleNames);
    }


    private static String createName(final String name, boolean useFilenameCompatibleNames) {
        if (name.isEmpty()) {
            return "EMPTY_NAME";
        }

        if (useFilenameCompatibleNames) {
            return makeNameFilenameCompatible(name);
        }

        return name;
    }

    private static String makeNameFilenameCompatible(String name) {
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
        private int pickleStepLine;

        PickleStepId(PickleEvent pickleEvent, PickleStep pickleStep) {
            this.uri = pickleEvent.uri;
            this.pickleLine = pickleEvent.pickle.getLocations().get(0).getLine();
            List<PickleLocation> stepLocations = pickleStep.getLocations();
            this.pickleStepLine = stepLocations.get(stepLocations.size() - 1).getLine();
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
