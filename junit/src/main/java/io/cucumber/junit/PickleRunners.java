package io.cucumber.junit;

import io.cucumber.plugin.event.CucumberStep;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runtime.RunnerSupplier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cucumber.junit.FileNameCompatibleNames.createName;


final class PickleRunners {

    interface PickleRunner {
        void run(RunNotifier notifier);

        Description getDescription();

        Description describeChild(CucumberStep step);

    }

    static PickleRunner withStepDescriptions(RunnerSupplier runnerSupplier, CucumberPickle pickle, JUnitOptions options) {
        try {
            return new WithStepDescriptions(runnerSupplier, pickle, options);
        } catch (InitializationError e) {
            throw new CucumberException("Failed to create scenario runner", e);
        }
    }


    static PickleRunner withNoStepDescriptions(String featureName, RunnerSupplier runnerSupplier, CucumberPickle pickle, JUnitOptions jUnitOptions) {
        return new NoStepDescriptions(featureName, runnerSupplier, pickle, jUnitOptions);
    }


    static class WithStepDescriptions extends ParentRunner<CucumberStep> implements PickleRunner {
        private final RunnerSupplier runnerSupplier;
        private final CucumberPickle pickle;
        private final JUnitOptions jUnitOptions;
        private final Map<CucumberStep, Description> stepDescriptions = new HashMap<>();
        private Description description;

        WithStepDescriptions(RunnerSupplier runnerSupplier, CucumberPickle pickle, JUnitOptions jUnitOptions) throws InitializationError {
            super(null);
            this.runnerSupplier = runnerSupplier;
            this.pickle = pickle;
            this.jUnitOptions = jUnitOptions;
        }

        @Override
        protected List<CucumberStep> getChildren() {
            // Casts io.cucumber.core.feature.CucumberStep
            // to io.cucumber.core.event.CucumberStep
            return new ArrayList<>(pickle.getSteps());
        }

        @Override
        protected String getName() {
            return createName(pickle.getName(), jUnitOptions.filenameCompatibleNames());
        }

        @Override
        public Description getDescription() {
            if (description == null) {
                description = Description.createSuiteDescription(getName(), new PickleId(pickle));
                getChildren().forEach(step -> description.addChild(describeChild(step)));
            }
            return description;
        }

        @Override
        public Description describeChild(CucumberStep step) {
            Description description = stepDescriptions.get(step);
            if (description == null) {
                String testName = createName(step.getText(), jUnitOptions.filenameCompatibleNames());
                description = Description.createTestDescription(getName(), testName, new PickleStepId(pickle, step));
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
        protected void runChild(CucumberStep step, RunNotifier notifier) {
            // The way we override run(RunNotifier) causes this method to never be called.
            // Instead it happens via cucumberScenario.run(jUnitReporter, jUnitReporter, runtime);
            throw new UnsupportedOperationException();
        }

    }


    static final class NoStepDescriptions implements PickleRunner {
        private final String featureName;
        private final RunnerSupplier runnerSupplier;
        private final CucumberPickle pickle;
        private final JUnitOptions jUnitOptions;
        private Description description;

        NoStepDescriptions(String featureName, RunnerSupplier runnerSupplier, CucumberPickle pickle, JUnitOptions jUnitOptions) {
            this.featureName = featureName;
            this.runnerSupplier = runnerSupplier;
            this.pickle = pickle;
            this.jUnitOptions = jUnitOptions;
        }

        @Override
        public Description getDescription() {
            if (description == null) {
                String className = createName(featureName, jUnitOptions.filenameCompatibleNames());
                String name = createName(pickle.getName(), jUnitOptions.filenameCompatibleNames());
                description = Description.createTestDescription(className, name, new PickleId(pickle));
            }
            return description;
        }

        @Override
        public Description describeChild(CucumberStep step) {
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

    static final class PickleId implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String uri;
        private final int pickleLine;

        PickleId(String uri, int pickleLine) {
            this.uri = uri;
            this.pickleLine = pickleLine;
        }

        PickleId(CucumberPickle pickle) {
            this(pickle.getUri(), pickle.getLine());
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

        PickleStepId(CucumberPickle pickleEvent, CucumberStep step) {
            this.uri = pickleEvent.getUri();
            this.pickleLine = pickleEvent.getLine();
            this.pickleStepLine = step.getStepLine();
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
