package io.cucumber.junit;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.runtime.CucumberExecutionContext;
import io.cucumber.junit.PickleRunners.PickleRunner;
import org.jspecify.annotations.Nullable;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static io.cucumber.core.exception.UnrecoverableExceptions.rethrowIfUnrecoverable;
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
    private final @Nullable Integer uniqueSuffix;
    private final CucumberExecutionContext context;
    private @Nullable Description description;

    private FeatureRunner(
            Feature feature, @Nullable Integer uniqueSuffix, Predicate<Pickle> filter, CucumberExecutionContext context,
            JUnitOptions options
    )
            throws InitializationError {
        super((Class<?>) null);
        this.feature = feature;
        this.uniqueSuffix = uniqueSuffix;
        this.options = options;
        this.context = context;

        Map<String, List<Pickle>> groupedByName = feature.getPickles().stream()
                .collect(groupingBy(Pickle::getName));
        this.children = feature.getPickles()
                .stream()
                .filter(filter)
                .map(pickle -> {
                    String featureName = getName();
                    Integer exampleId = uniqueSuffix(groupedByName, pickle, Pickle::getName);
                    return options.stepNotifications()
                            ? withStepDescriptions(context, pickle, exampleId, options)
                            : withNoStepDescriptions(featureName, context, pickle, exampleId, options);
                })
                .collect(toList());
    }

    static FeatureRunner create(
            Feature feature, @Nullable Integer uniqueSuffix, Predicate<Pickle> filter, CucumberExecutionContext context,
            JUnitOptions options
    ) {
        try {
            return new FeatureRunner(feature, uniqueSuffix, filter, context, options);
        } catch (InitializationError e) {
            throw new CucumberException("Failed to create scenario runner", e);
        }
    }

    boolean isEmpty() {
        return children.isEmpty();
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
    public void run(RunNotifier notifier) {
        context.beforeFeature(feature);
        super.run(notifier);
    }

    @Override
    protected void runChild(PickleRunner child, RunNotifier notifier) {
        notifier.fireTestStarted(describeChild(child));
        try {
            child.run(notifier);
        } catch (Throwable t) {
            rethrowIfUnrecoverable(t);
            notifier.fireTestFailure(new Failure(describeChild(child), t));
            notifier.pleaseStop();
        } finally {
            notifier.fireTestFinished(describeChild(child));
        }
    }

    private static final class FeatureId implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;
        private final URI uri;

        FeatureId(Feature feature) {
            this.uri = feature.getUri();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FeatureId featureId))
                return false;
            return Objects.equals(uri, featureId.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(uri);
        }

        @Override
        public String toString() {
            return uri.toString();
        }

    }

}
