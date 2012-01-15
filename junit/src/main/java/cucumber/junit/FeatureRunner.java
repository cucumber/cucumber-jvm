package cucumber.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.World;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.Feature;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class FeatureRunner extends ParentRunner<ParentRunner> {
    private final List<ParentRunner> children = new ArrayList<ParentRunner>();

    private final CucumberFeature cucumberFeature;
    private final List<String> gluePaths;
    private final Runtime runtime;
    private final JUnitReporter jUnitReporter;

    protected FeatureRunner(CucumberFeature cucumberFeature, List<String> gluePaths, Runtime runtime, JUnitReporter jUnitReporter) throws InitializationError {
        super(null);
        this.cucumberFeature = cucumberFeature;
        this.gluePaths = gluePaths;
        this.runtime = runtime;
        this.jUnitReporter = jUnitReporter;
        buildFeatureElementRunners();
    }

    @Override
    public String getName() {
        Feature feature = cucumberFeature.getFeature();
        return feature.getKeyword() + ": " + feature.getName();
    }

    @Override
    protected List<ParentRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(ParentRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(ParentRunner child, RunNotifier notifier) {
        child.run(notifier);
    }

    @Override
    public void run(RunNotifier notifier) {
        jUnitReporter.uri(cucumberFeature.getUri());
        jUnitReporter.feature(cucumberFeature.getFeature());
        super.run(notifier);
        jUnitReporter.eof();
    }

    private void buildFeatureElementRunners() {
        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            try {
                ParentRunner featureElementRunner;
                if (cucumberTagStatement instanceof CucumberScenario) {
                    featureElementRunner = new ExecutionUnitRunner(runtime, (CucumberScenario) cucumberTagStatement, jUnitReporter);
                } else {
                    featureElementRunner = new ScenarioOutlineRunner(runtime, gluePaths, (CucumberScenarioOutline) cucumberTagStatement, jUnitReporter);
                }
                children.add(featureElementRunner);
            } catch (InitializationError e) {
                throw new RuntimeException("Failed to create scenario runner", e);
            }
        }
    }

}
