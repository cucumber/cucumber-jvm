package io.cucumber.junit;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.runtime.CucumberExecutionContext;
import io.cucumber.plugin.event.Step;
import org.jspecify.annotations.Nullable;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.cucumber.junit.FileNameCompatibleNames.createName;

final class PickleRunners {

    private PickleRunners() {
        /* no-op */
    }

    static PickleRunner withStepDescriptions(
            CucumberExecutionContext context, Pickle pickle, @Nullable Integer uniqueSuffix, JUnitOptions options
    ) {
        try {
            return new WithStepDescriptions(context, pickle, uniqueSuffix, options);
        } catch (InitializationError e) {
            throw new CucumberException("Failed to create scenario runner", e);
        }
    }

    static PickleRunner withNoStepDescriptions(
            String featureName, CucumberExecutionContext context, Pickle pickle, @Nullable Integer uniqueSuffix,
            JUnitOptions jUnitOptions
    ) {
        return new NoStepDescriptions(featureName, context, pickle, uniqueSuffix, jUnitOptions);
    }

    interface PickleRunner {

        void run(RunNotifier notifier);

        Description getDescription();

        Description describeChild(Step step);

    }

    static class WithStepDescriptions extends ParentRunner<Step> implements PickleRunner {

        private final CucumberExecutionContext context;
        private final Pickle pickle;
        private final JUnitOptions jUnitOptions;
        private final Map<Step, Description> stepDescriptions = new HashMap<>();
        private final @Nullable Integer uniqueSuffix;
        private @Nullable Description description;

        WithStepDescriptions(
                CucumberExecutionContext context, Pickle pickle, @Nullable Integer uniqueSuffix,
                JUnitOptions jUnitOptions
        )
                throws InitializationError {
            super((Class<?>) null);
            this.context = context;
            this.pickle = pickle;
            this.jUnitOptions = jUnitOptions;
            this.uniqueSuffix = uniqueSuffix;
        }

        @Override
        protected List<Step> getChildren() {
            // Casts io.cucumber.core.gherkin.Step
            // to io.cucumber.core.event.CucumberStep
            return new ArrayList<>(pickle.getSteps());
        }

        @Override
        protected String getName() {
            return createName(pickle.getName(), uniqueSuffix, jUnitOptions.filenameCompatibleNames());
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
        public Description describeChild(Step step) {
            Description description = stepDescriptions.get(step);
            if (description == null) {
                String className = getName();
                String name = createName(step.getText(), jUnitOptions.filenameCompatibleNames());
                description = Description.createTestDescription(className, name, new PickleStepId(pickle, step));
                stepDescriptions.put(step, description);
            }
            return description;
        }

        @Override
        public void run(final RunNotifier notifier) {
            // Possibly invoked by a thread other then the creating thread
            context.runTestCase(runner -> {
                JUnitReporter jUnitReporter = new JUnitReporter(runner.getBus(), jUnitOptions);
                jUnitReporter.startExecutionUnit(this, notifier);
                runner.runPickle(pickle);
                jUnitReporter.finishExecutionUnit();
            });
        }

        @Override
        protected void runChild(Step step, RunNotifier notifier) {
            // The way we override run(RunNotifier) causes this method to never
            // be called.
            // Instead it happens via cucumberScenario.run(jUnitReporter,
            // jUnitReporter, runtime);
            throw new UnsupportedOperationException();
        }

    }

    static final class NoStepDescriptions implements PickleRunner {

        private final String featureName;
        private final CucumberExecutionContext context;
        private final Pickle pickle;
        private final JUnitOptions jUnitOptions;
        private final @Nullable Integer uniqueSuffix;
        private @Nullable Description description;

        NoStepDescriptions(
                String featureName, CucumberExecutionContext context, Pickle pickle, @Nullable Integer uniqueSuffix,
                JUnitOptions jUnitOptions
        ) {
            this.featureName = featureName;
            this.context = context;
            this.pickle = pickle;
            this.jUnitOptions = jUnitOptions;
            this.uniqueSuffix = uniqueSuffix;
        }

        @Override
        public void run(final RunNotifier notifier) {
            // Possibly invoked by a thread other then the creating thread
            context.runTestCase(runner -> {
                JUnitReporter jUnitReporter = new JUnitReporter(runner.getBus(), jUnitOptions);
                jUnitReporter.startExecutionUnit(this, notifier);
                runner.runPickle(pickle);
                jUnitReporter.finishExecutionUnit();
            });
        }

        @Override
        public Description getDescription() {
            if (description == null) {
                String className = createName(featureName, jUnitOptions.filenameCompatibleNames());
                String name = createName(pickle.getName(), uniqueSuffix, jUnitOptions.filenameCompatibleNames());
                description = Description.createTestDescription(className, name, new PickleId(pickle));
            }
            return description;
        }

        @Override
        public Description describeChild(Step step) {
            throw new UnsupportedOperationException("This pickle runner does not wish to describe its children");
        }

    }

    static final class PickleId implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;
        private final URI uri;
        private final int pickleLine;

        PickleId(Pickle pickle) {
            this(pickle.getUri(), pickle.getLocation().getLine());
        }

        PickleId(URI uri, int pickleLine) {
            this.uri = uri;
            this.pickleLine = pickleLine;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PickleId pickleId))
                return false;
            return pickleLine == pickleId.pickleLine && Objects.equals(uri, pickleId.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri, pickleLine);
        }

        @Override
        public String toString() {
            return uri + ":" + pickleLine;
        }

    }

    private static final class PickleStepId implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;
        private final URI uri;
        private final int pickleLine;
        private final int pickleStepLine;

        PickleStepId(Pickle pickle, Step step) {
            this.uri = pickle.getUri();
            this.pickleLine = pickle.getLocation().getLine();
            this.pickleStepLine = step.getLine();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PickleStepId that))
                return false;
            return pickleLine == that.pickleLine && pickleStepLine == that.pickleStepLine
                    && Objects.equals(uri, that.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri, pickleLine, pickleStepLine);
        }

        @Override
        public String toString() {
            return uri + ":" + pickleLine + ":" + pickleStepLine;
        }

    }

}
