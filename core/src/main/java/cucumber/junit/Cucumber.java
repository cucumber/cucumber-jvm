package cucumber.junit;

import cucumber.classpath.Classpath;
import cucumber.classpath.Consumer;
import cucumber.io.Resource;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Feature;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class Cucumber extends ParentRunner<ScenarioRunner> {
    private CucumberFeature cucumberFeature;
    private final Runtime runtime;

    private static Runtime runtime(Class testClass) {
        String packageName = testClass.getName().substring(0, testClass.getName().lastIndexOf("."));
        final Runtime runtime = new Runtime(packageName);
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (String snippet : runtime.getSnippets()) {
                    System.out.println(snippet);
                }
            }
        });
        return runtime;
    }

    /**
     * Constructor called by JUnit.
     */
    public Cucumber(Class featureClass) throws InitializationError {
        this(featureClass, runtime(featureClass));
    }

    public Cucumber(Class featureClass, final Runtime runtime) throws InitializationError {
        // Why aren't we passing the class to super? I don't remember, but there is probably a good reason.
        super(null);
        this.runtime = runtime;
        cucumber.junit.Feature featureAnnotation = (cucumber.junit.Feature) featureClass.getAnnotation(cucumber.junit.Feature.class);
        String pathName;
        if (featureAnnotation != null) {
            pathName = featureAnnotation.value();
        } else {
            pathName = featureClass.getName().replace('.', '/') + ".feature";
        }
        cucumberFeature = parseFeature(pathName);
    }

    @Override
    public String getName() {
        Feature feature = cucumberFeature.getFeature();
        return feature.getKeyword() + ": " + feature.getName();
    }

    @Override
    protected List<ScenarioRunner> getChildren() {
        List<ScenarioRunner> scenarioRunners = new ArrayList<ScenarioRunner>();
        for (CucumberScenario cucumberScenario : cucumberFeature.getCucumberScenarios()) {
            try {
                scenarioRunners.add(new ScenarioRunner(runtime, cucumberScenario));
            } catch (InitializationError e) {
                throw new RuntimeException("Failed to create scenario runner", e);
            }
        }
        return scenarioRunners;
    }

    @Override
    protected Description describeChild(ScenarioRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(ScenarioRunner runner, RunNotifier notifier) {
        runner.run(notifier);
    }

    private CucumberFeature parseFeature(String pathName) {
        List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);
        Classpath.scan(pathName, new Consumer() {
            public void consume(Resource resource) {
                builder.parse(resource);
            }
        });
        return cucumberFeatures.get(0);
    }
}
