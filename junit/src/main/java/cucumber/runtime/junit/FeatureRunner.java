package cucumber.runtime.junit;

import cucumber.runtime.filter.Filters;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runtime.junit.PickleRunners.PickleRunner;
import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.Feature;
import gherkin.events.PickleEvent;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static cucumber.runtime.junit.PickleRunners.withNoStepDescriptions;

public class FeatureRunner extends BlockJUnit4ClassRunner {
    private final List<FrameworkMethod> children = new ArrayList<FrameworkMethod>();

    private final CucumberFeature cucumberFeature;
    private Description description;

    public FeatureRunner(Class<?> clazz, CucumberFeature cucumberFeature, Filters filters, ThreadLocalRunnerSupplier runnerSupplier, JUnitOptions jUnitOptions) throws InitializationError {
        super(clazz);
        this.cucumberFeature = cucumberFeature;
        buildFeatureElementRunners(filters, runnerSupplier, jUnitOptions);
    }

    @Override
    protected void validateInstanceMethods(List<Throwable> errors) {
        super.validateInstanceMethods(errors);
        if (computeTestMethods().size() != 0) {
            errors.add(new Exception("No runnable methods executed using Cucumber runner"));
        } else {
            // remove last error added: new Exception("No runnable methods")
            errors.remove(errors.size() - 1);
        }
    }

    @Override
    protected boolean isIgnored(FrameworkMethod child) {
        return false;
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
            for (FrameworkMethod child : getChildren()) {
                description.addChild(describeChild(child));
            }
        }
        return description;
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(FrameworkMethod child) {
        return ((PickleRunner) child).getDescription();
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    	PickleRunner pickleRunner = (PickleRunner) method;
    	pickleRunner.setNotifier(notifier);
    	try {
			methodBlock(method).evaluate();
		} catch (Throwable e) {
	        Description description = describeChild(method);
			notifier.fireTestFailure(new Failure(description, e));
		}
    	pickleRunner.setNotifier(null);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        final PickleRunner pickleRunner = (PickleRunner) method;
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                pickleRunner.run();
            }

        };
    }

    private void buildFeatureElementRunners(Filters filters, ThreadLocalRunnerSupplier runnerSupplier, JUnitOptions jUnitOptions) {
        for (PickleEvent pickleEvent : cucumberFeature.getPickles()) {
            if (filters.matchesFilters(pickleEvent)) {
                        PickleRunner picklePickleRunner;
                        picklePickleRunner = withNoStepDescriptions(cucumberFeature.getName(), runnerSupplier, pickleEvent, jUnitOptions);
                        children.add(picklePickleRunner);
            }
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
