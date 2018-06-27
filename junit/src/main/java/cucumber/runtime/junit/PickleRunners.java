package cucumber.runtime.junit;

import cucumber.runner.Runner;
import cucumber.runtime.RunnerSupplier;
import io.cucumber.messages.Messages.Location;
import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.PickleStep;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


class PickleRunners {

    interface PickleRunner {
        void run(RunNotifier notifier);

        Description getDescription();

        Description describeChild(PickleStep step);

    }

    static PickleRunner withStepDescriptions(RunnerSupplier runnerSupplier, Pickle pickle, JUnitOptions jUnitOptions) throws InitializationError {
        return new WithStepDescriptions(runnerSupplier, pickle, jUnitOptions);
    }


    static PickleRunner withNoStepDescriptions(String featureName, RunnerSupplier runnerSupplier, Pickle pickle, JUnitOptions jUnitOptions) {
        return new NoStepDescriptions(featureName, runnerSupplier, pickle, jUnitOptions);
    }


    static class WithStepDescriptions extends ParentRunner<PickleStep> implements PickleRunner {
        private final RunnerSupplier runnerSupplier;
        private final Pickle pickle;
        private final JUnitOptions jUnitOptions;
        private final Map<PickleStep, Description> stepDescriptions = new HashMap<PickleStep, Description>();
        private Description description;

        WithStepDescriptions(RunnerSupplier runnerSupplier, Pickle pickle, JUnitOptions jUnitOptions) throws InitializationError {
            super(null);
            this.runnerSupplier = runnerSupplier;
            this.pickle = pickle;
            this.jUnitOptions = jUnitOptions;
        }

        @Override
        protected List<PickleStep> getChildren() {
            return pickle.getStepsList();
        }

        @Override
        public String getName() {
            return getPickleName(pickle, jUnitOptions.filenameCompatibleNames());
        }

        @Override
        public Description getDescription() {
            if (description == null) {
                description = Description.createSuiteDescription(getName(), new PickleId(pickle));
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
                if (jUnitOptions.filenameCompatibleNames()) {
                    testName = makeNameFilenameCompatible(step.getText());
                } else {
                    testName = step.getText();
                }
                description = Description.createTestDescription(getName(), testName, new PickleStepId(pickle.getUri(), step));
                stepDescriptions.put(step, description);
            }
            return description;
        }

        @Override
        public void run(final RunNotifier notifier) {
            // Possibly invoked by a thread other then the creating thread
            Runner runner = runnerSupplier.get();
            JUnitReporter jUnitReporter = new JUnitReporter(runner.getBus(), jUnitOptions);
            jUnitReporter.startExecutionUnit(this, notifier);
            runner.runPickle(pickle);
            jUnitReporter.finishExecutionUnit();
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
        private final RunnerSupplier runnerSupplier;
        private final Pickle pickle;
        private final JUnitOptions jUnitOptions;
        private Description description;

        NoStepDescriptions(String featureName, RunnerSupplier runnerSupplier, Pickle pickle, JUnitOptions jUnitOptions) {
            this.featureName = featureName;
            this.runnerSupplier = runnerSupplier;
            this.pickle = pickle;
            this.jUnitOptions = jUnitOptions;
        }

        @Override
        public Description getDescription() {
            if (description == null) {
                String className = createName(featureName, jUnitOptions.filenameCompatibleNames());
                String name = getPickleName(pickle, jUnitOptions.filenameCompatibleNames());
                description = Description.createTestDescription(className, name, new PickleId(pickle));
            }
            return description;
        }

        @Override
        public Description describeChild(PickleStep step) {
            throw new UnsupportedOperationException("This pickle runner does not wish to describe its children");
        }

        @Override
        public void run(final RunNotifier notifier) {
            // Possibly invoked by a thread other then the creating thread
            Runner runner = runnerSupplier.get();
            JUnitReporter jUnitReporter = new JUnitReporter(runner.getBus(), jUnitOptions);
            jUnitReporter.startExecutionUnit(this, notifier);
            runner.runPickle(pickle);
            jUnitReporter.finishExecutionUnit();
        }
    }

    private static String getPickleName(Pickle pickleEvent, boolean useFilenameCompatibleNames) {
        final String name = pickleEvent.getName();
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

    static final class PickleId implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String uri;
        private int pickleLine;

        PickleId(String uri, int pickleLine) {
            this.uri = uri;
            this.pickleLine = pickleLine;
        }

        PickleId(Pickle pickle) {
            this(pickle.getUri(), pickle.getLocations(pickle.getLocationsCount() - 1).getLine());
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
        private final String id;

        PickleStepId(String uri, PickleStep pickleStep) {
            StringBuilder b = new StringBuilder(uri);
            for (Location location : pickleStep.getLocationsList()) {
                b.append(":").append(location.getLine());
            }
            this.id = b.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PickleStepId that = (PickleStepId) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return id;
        }
    }

}
