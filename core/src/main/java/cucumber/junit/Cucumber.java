package cucumber.junit;

import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.Runtime;
import cucumber.runtime.SnippetPrinter;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Feature;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class Cucumber extends ParentRunner<ScenarioRunner> {
    private final Runtime runtime;
    private final List<ScenarioRunner> scenarioRunners = new ArrayList<ScenarioRunner>();
    private String name;

    private static Runtime runtime(Class testClass) {
        String packageName = testClass.getName().substring(0, testClass.getName().lastIndexOf("."));
        final Runtime runtime = new Runtime(packageName);
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                new SnippetPrinter(System.out).printSnippets(runtime);
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
        String pathName = featurePath(featureClass);
        parseFeature(pathName, filters(featureClass));
        addAdditionalScanPaths(featureClass, this.runtime);
    }

    private void addAdditionalScanPaths(Class featureClass, final Runtime runtime) {
        cucumber.junit.Feature featureAnnotation = (cucumber.junit.Feature) featureClass.getAnnotation(cucumber.junit.Feature.class);
        if (featureAnnotation != null) {
            runtime.addStepdefScanPath(featureAnnotation.packages());
        }
    }

    private String featurePath(Class featureClass) {
        cucumber.junit.Feature featureAnnotation = (cucumber.junit.Feature) featureClass.getAnnotation(cucumber.junit.Feature.class);
        String pathName;
        if (featureAnnotation != null) {
            pathName = featureAnnotation.value();
        } else {
            pathName = featureClass.getName().replace('.', '/') + ".feature";
        }
        return pathName;
    }

    private List<Object> filters(Class featureClass) {
        cucumber.junit.Feature featureAnnotation = (cucumber.junit.Feature) featureClass.getAnnotation(cucumber.junit.Feature.class);
        Object[] filters = null;
        if (featureAnnotation != null) {
            Long[] lines = toLong(featureAnnotation.lines());
            filters = lines;
            if (filters.length == 0) {
                String[] tags = featureAnnotation.tags();
                filters = tags;
            }
        }
        return asList(filters);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected List<ScenarioRunner> getChildren() {
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

    private void parseFeature(String pathName, final List<Object> filters) {
        List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);
        Resources.scan(pathName, new Consumer() {
            public void consume(Resource resource) {
                builder.parse(resource, filters);
            }
        });

        if (cucumberFeatures.isEmpty()) {
            name = "No matching features";
        } else {
            CucumberFeature cucumberFeature = cucumberFeatures.get(0);
            Feature feature = cucumberFeature.getFeature();
            name = feature.getKeyword() + ": " + feature.getName();
            buildScenarioRunners(cucumberFeature);
        }
    }

    private void buildScenarioRunners(CucumberFeature cucumberFeature) {
        for (CucumberScenario cucumberScenario : cucumberFeature.getCucumberScenarios()) {
            try {
                scenarioRunners.add(new ScenarioRunner(runtime, cucumberScenario));
            } catch (InitializationError e) {
                throw new RuntimeException("Failed to create scenario runner", e);
            }
        }

    }

    private Long[] toLong(long[] plongs) {
        Long[] longs = new Long[plongs.length];
        for (int i = 0; i < plongs.length; i++) {
            longs[i] = plongs[i];
        }
        return longs;
    }
}
