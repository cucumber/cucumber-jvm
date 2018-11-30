package cucumber.runtime.junit;

import cucumber.runtime.CucumberException;
import cucumber.runtime.FeatureCompiler;
import cucumber.runtime.filter.Filters;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runtime.junit.PickleRunners.PickleRunner;
import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.Feature;
import gherkin.events.PickleEvent;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static cucumber.runtime.junit.PickleRunners.withNoStepDescriptions;
import static cucumber.runtime.junit.PickleRunners.withStepDescriptions;

public class FeatureRunner extends ParentRunner<PickleRunner> {
    private final List<PickleRunner> children = new ArrayList<PickleRunner>();

    private final CucumberFeature cucumberFeature;
    private Description description;

    public FeatureRunner(CucumberFeature cucumberFeature, Filters filters, ThreadLocalRunnerSupplier runnerSupplier, JUnitOptions jUnitOptions) throws InitializationError {
        super(null);
        this.cucumberFeature = cucumberFeature;
        buildFeatureElementRunners(filters, runnerSupplier, jUnitOptions);
    }

    @Override
    public String getName() {
        Feature feature = cucumberFeature.getGherkinFeature().getFeature();
        return feature.getKeyword() + ": " + feature.getName();
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), new FeatureId(cucumberFeature));
            for (PickleRunner child : getChildren()) {
                description.addChild(describeChild(child));
            }
        }
        return description;
    }

    public boolean isEmpty() {
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
        child.run(notifier);
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
    }

    private void buildFeatureElementRunners(Filters filters, ThreadLocalRunnerSupplier runnerSupplier, JUnitOptions jUnitOptions) {
        Feature feature = cucumberFeature.getGherkinFeature().getFeature();
        if (feature == null) {
            return;
        }
        FeatureCompiler compiler = new FeatureCompiler();
        List<PickleEvent> pickleEvents = compiler.compileFeature(cucumberFeature);
        for (PickleEvent pickleEvent : pickleEvents) {
            if (filters.matchesFilters(pickleEvent)) {
                try {
                    if (jUnitOptions.stepNotifications()) {
                        PickleRunner picklePickleRunner;
                        picklePickleRunner = withStepDescriptions(runnerSupplier, pickleEvent, jUnitOptions);
                        children.add(picklePickleRunner);
                    } else {
                        PickleRunner picklePickleRunner;
                        picklePickleRunner = withNoStepDescriptions(feature.getName(), runnerSupplier, pickleEvent, jUnitOptions);
                        children.add(picklePickleRunner);
                    }
                } catch (InitializationError e) {
                    throw new CucumberException("Failed to create scenario runner", e);
                }
            }
        }
    }

    private static final class FeatureId implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String uri;

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
            return uri;
        }
    }

}
