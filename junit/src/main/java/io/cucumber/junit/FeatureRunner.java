package io.cucumber.junit;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
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

import static io.cucumber.junit.PickleRunners.withNoStepDescriptions;
import static io.cucumber.junit.PickleRunners.withStepDescriptions;
import static java.util.stream.Collectors.toList;

final class FeatureRunner extends ParentRunner<PickleRunner> {

    private final List<PickleRunner> children;
    private final CucumberFeature cucumberFeature;
    private Description description;

    static FeatureRunner create(CucumberFeature feature, Predicate<CucumberPickle> filter, RunnerSupplier runners, JUnitOptions options) {
        try {
            return new FeatureRunner(feature, filter, runners, options);
        } catch (InitializationError e) {
            throw new CucumberException("Failed to create scenario runner", e);
        }
    }

    private FeatureRunner(CucumberFeature feature, Predicate<CucumberPickle> filter, RunnerSupplier runners, JUnitOptions options) throws InitializationError {
        super(null);
        this.cucumberFeature = feature;
        this.children = feature.getPickles().stream()
            .filter(filter).
                map(pickleEvent -> options.stepNotifications()
                    ? withStepDescriptions(runners, pickleEvent, options)
                    : withNoStepDescriptions(feature.getName(), runners, pickleEvent, options))
            .collect(toList());
    }

    @Override
    protected String getName() {
        return cucumberFeature.getKeyword() + ": " + cucumberFeature.getName();
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), new FeatureId(cucumberFeature));
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
        notifier.fireTestStarted(getDescription());
        try {
            child.run(notifier);
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(getDescription(), e));
            notifier.pleaseStop();
        } finally {
            notifier.fireTestFinished(getDescription());
        }
    }

    private static final class FeatureId implements Serializable {
        private static final long serialVersionUID = 1L;
        private final URI uri;

        FeatureId(CucumberFeature feature) {
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
