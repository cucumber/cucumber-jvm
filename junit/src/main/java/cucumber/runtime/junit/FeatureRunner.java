package cucumber.runtime.junit;

import static cucumber.runtime.junit.PickleRunners.withNoStepDescriptions;
import static cucumber.runtime.junit.PickleRunners.withStepDescriptions;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.junit.PickleRunners.PickleRunner;
import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.Feature;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FeatureRunner extends ParentRunner<PickleRunner> {
    private final List<PickleRunner> children = new ArrayList<PickleRunner>();

    private final CucumberFeature cucumberFeature;
    private Description description;

    public FeatureRunner(CucumberFeature cucumberFeature, Runtime runtime, JUnitReporter jUnitReporter) throws InitializationError {
        super(null);
        this.cucumberFeature = cucumberFeature;
        buildFeatureElementRunners(runtime, jUnitReporter);
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

    private void buildFeatureElementRunners(Runtime runtime, JUnitReporter jUnitReporter) {
        Feature feature = cucumberFeature.getGherkinFeature().getFeature();
        if (feature == null) {
            return;
        }
        Compiler compiler = new Compiler();
        List<PickleEvent> pickleEvents = new ArrayList<PickleEvent>();
        for (Pickle pickle : compiler.compile(cucumberFeature.getGherkinFeature())) {
            pickleEvents.add(new PickleEvent(cucumberFeature.getUri(), pickle));
        }
        for (PickleEvent pickleEvent : pickleEvents) {
            if (runtime.matchesFilters(pickleEvent)) {
                try {
                    if(jUnitReporter.stepNotifications()) {
                        PickleRunner picklePickleRunner;
                        picklePickleRunner = withStepDescriptions(runtime.getRunner(), pickleEvent, jUnitReporter);
                        children.add(picklePickleRunner);
                    } else {
                        PickleRunner picklePickleRunner;
                        picklePickleRunner = withNoStepDescriptions(feature.getName(), runtime.getRunner(), pickleEvent, jUnitReporter);
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
