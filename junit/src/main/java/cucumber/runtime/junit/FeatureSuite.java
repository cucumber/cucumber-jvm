package cucumber.runtime.junit;

import java.util.ArrayList;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;

public class FeatureSuite extends Suite {

    private final CucumberFeature cucumberFeature;
    private final Runtime runtime;
    private final JUnitReporter jUnitReporter;
    private final NameProvider nameProvider;

    public FeatureSuite(Class<?> testClass, CucumberFeature cucumberFeature, Runtime runtime, JUnitReporter jUnitReporter) throws InitializationError {
        super(testClass, new ArrayList<Runner>());
        this.cucumberFeature = cucumberFeature;
        this.runtime = runtime;
        this.jUnitReporter = jUnitReporter;
        this.nameProvider = new NameProvider(cucumberFeature);
        buildFeatureElementRunners();
    }

    @Override
    public String getName() {
        return nameProvider.getName();
    }

    @Override
    public void run(RunNotifier notifier) {
        jUnitReporter.uri(cucumberFeature.getUri());
        jUnitReporter.feature(cucumberFeature.getGherkinFeature());
        super.run(notifier);
        jUnitReporter.eof();
    }

    private void buildFeatureElementRunners() throws InitializationError {
        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            Runner featureElementRunner;
            if (cucumberTagStatement instanceof CucumberScenario) {
                String name = nameProvider.getName(cucumberTagStatement);
                featureElementRunner = new ExecutionUnitRunner(getTestClass().getJavaClass(), runtime, name, (CucumberScenario) cucumberTagStatement, jUnitReporter);
            } else {
                featureElementRunner = SuiteWithName.newSuite(getTestClass().getJavaClass(), runtime, (CucumberScenarioOutline) cucumberTagStatement, jUnitReporter, nameProvider);
            }
            getChildren().add(featureElementRunner);
        }
    }

}
