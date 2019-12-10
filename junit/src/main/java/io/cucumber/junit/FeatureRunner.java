package io.cucumber.junit;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.junit.PickleRunners.PickleRunner;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.function.Predicate;

import static io.cucumber.junit.FileNameCompatibleNames.createName;
import static io.cucumber.junit.PickleRunners.withNoStepDescriptions;
import static io.cucumber.junit.PickleRunners.withStepDescriptions;
import static java.util.stream.Collectors.toList;

final class FeatureRunner extends ParentRunner<PickleRunner> {

    private final List<PickleRunner> children;
    private final Feature feature;
    private final JUnitOptions options;
    private Description description;

    static FeatureRunner create(Feature feature, Predicate<Pickle> filter, RunnerSupplier runners, JUnitOptions options) {
        try {
            return new FeatureRunner(feature, filter, runners, options);
        } catch (InitializationError e) {
            throw new CucumberException("Failed to create scenario runner", e);
        }
    }

    private FeatureRunner(Feature feature, Predicate<Pickle> filter, RunnerSupplier runners, JUnitOptions options) throws InitializationError {
        super(null);
        this.feature = feature;
        this.options = options;
        this.children = feature.getPickles().stream()
            .filter(filter).
                map(pickle -> options.stepNotifications()
                    ? withStepDescriptions(runners, pickle, options)
                    : withNoStepDescriptions(feature.getName(), runners, pickle, options))
            .collect(toList());
    }

    @Override
    protected String getName() {
        return createName(feature.getName(), options.filenameCompatibleNames());
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), new FeatureId(feature));
            getChildren().forEach(child -> description.addChild(describeChild(child)));
        }
        return description;
    }

    boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    protected List<PickleRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(PickleRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(PickleRunner child, RunNotifier notifier) {
        notifier.fireTestStarted(describeChild(child));
        try {
            child.run(notifier);
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(describeChild(child), e));
            notifier.pleaseStop();
        } finally {
            notifier.fireTestFinished(describeChild(child));
        }
    }

    private static final class FeatureId implements Serializable {
        private static final long serialVersionUID = 1L;
        private final URI uri;

        FeatureId(Feature feature) {
            this.uri = feature.getUri();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FeatureId featureId = (FeatureId) o;
            return uri.equals(featureId.uri);
        }

        @Override
        public int hashCode() {
            return uri.hashCode();
        }

        @Override
        public String toString() {
            return uri.toString();
        }
    }

}
