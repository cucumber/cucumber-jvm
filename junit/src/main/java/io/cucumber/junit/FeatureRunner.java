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
import java.util.Map;
import java.util.function.Predicate;

import static io.cucumber.junit.FileNameCompatibleNames.createName;
import static io.cucumber.junit.FileNameCompatibleNames.uniqueSuffix;
import static io.cucumber.junit.PickleRunners.withNoStepDescriptions;
import static io.cucumber.junit.PickleRunners.withStepDescriptions;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

final class FeatureRunner extends ParentRunner<PickleRunner> {

    private final List<PickleRunner> children;
    private final Feature feature;
    private final JUnitOptions options;
    private final Integer uniqueSuffix;
    private Description description;

    private FeatureRunner(
            Feature feature, Integer uniqueSuffix, Predicate<Pickle> filter, RunnerSupplier runners,
            JUnitOptions options
    )
            throws InitializationError {
        super((Class<?>) null);
        this.feature = feature;
        this.uniqueSuffix = uniqueSuffix;
        this.options = options;

        Map<String, List<Pickle>> groupedByName = feature.getPickles().stream()
                .collect(groupingBy(Pickle::getName));
        this.children = feature.getPickles()
                .stream()
                .filter(filter)
                .map(pickle -> {
                    String featureName = getName();
                    Integer exampleId = uniqueSuffix(groupedByName, pickle, Pickle::getName);
                    return options.stepNotifications()
                            ? withStepDescriptions(runners, pickle, exampleId, options)
                            : withNoStepDescriptions(featureName, runners, pickle, exampleId, options);
                })
                .collect(toList());
    }

    static FeatureRunner create(
            Feature feature, Integer uniqueSuffix, Predicate<Pickle> filter, RunnerSupplier runners,
            JUnitOptions options
    ) {
        try {
            return new FeatureRunner(feature, uniqueSuffix, filter, runners, options);
        } catch (InitializationError e) {
            throw new CucumberException("Failed to create scenario runner", e);
        }
    }

    boolean isEmpty() {
        return children.isEmpty();
    }

    private static final class FeatureId implements Serializable {

        private static final long serialVersionUID = 1L;
        private final URI uri;

        FeatureId(Feature feature) {
            this.uri = feature.getUri();
        }

        @Override
        public int hashCode() {
            return uri.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FeatureId featureId = (FeatureId) o;
            return uri.equals(featureId.uri);
        }

        @Override
        public String toString() {
            return uri.toString();
        }

    }

    @Override
    protected String getName() {
        String name = feature.getName().orElse("EMPTY_NAME");
        return createName(name, uniqueSuffix, options.filenameCompatibleNames());
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), new FeatureId(feature));
            getChildren().forEach(child -> description.addChild(describeChild(child)));
        }
        return description;
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

}
