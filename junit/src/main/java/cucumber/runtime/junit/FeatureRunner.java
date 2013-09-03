package cucumber.runtime.junit;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.Feature;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class FeatureRunner extends ParentRunner<Runner> {
    private final List<Runner> children = new ArrayList<Runner>();

    private final CucumberFeature cucumberFeature;
    private final Runtime runtime;
    private final JUnitReporter jUnitReporter;
    private Description description;

    public FeatureRunner(Class<?> testClass, CucumberFeature cucumberFeature, Runtime runtime, JUnitReporter jUnitReporter) throws InitializationError {
        super(testClass);
        this.cucumberFeature = cucumberFeature;
        this.runtime = runtime;
        this.jUnitReporter = jUnitReporter;
        buildFeatureElementRunners();
    }

    @Override
    public String getName() {
        Feature feature = cucumberFeature.getGherkinFeature();
        return feature.getKeyword() + ": " + feature.getName();
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), cucumberFeature.getGherkinFeature());
            for (Runner child : getChildren()) {
                description.addChild(describeChild(child));
            }
        }
        return description;
    }

    @Override
    protected List<Runner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        child.run(notifier);
    }

    @Override
    public void run(RunNotifier notifier) {
        jUnitReporter.uri(cucumberFeature.getUri());
        jUnitReporter.feature(cucumberFeature.getGherkinFeature());
        super.run(notifier);
        jUnitReporter.eof();
    }

    private void buildFeatureElementRunners() {
        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            try {
                Runner featureElementRunner;
                if (cucumberTagStatement instanceof CucumberScenario) {
                    featureElementRunner = new ExecutionUnitRunner(getTestClass().getJavaClass(), runtime, (CucumberScenario) cucumberTagStatement, jUnitReporter);
                } else {
                    featureElementRunner = new ScenarioOutlineRunner(getTestClass().getJavaClass(), runtime, (CucumberScenarioOutline) cucumberTagStatement, jUnitReporter);
                }
                children.add(featureElementRunner);
            } catch (InitializationError e) {
                throw new CucumberException("Failed to create scenario runner", e);
            }
        }
    }

}
