package cucumber.junit;

import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.Runtime;
import cucumber.runtime.SnippetPrinter;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.PrettyFormatter;
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
    private final JUnitReporter jUnitReporter;
    private final Feature feature;
    private final String featurePath;

    private static Runtime runtime(Class featureClass) {
        // TODO: This creates a Runtime for each test class. That's expensive.
        // We should only have a single Runtime per JVM. This means package/script paths
        // must not be passed to the constructor. Instead, each stepdef must know where it was
        // loaded from so we can apply the correct ones.
        final Runtime runtime = new Runtime(packageNamesOrScriptPaths(featureClass));
        java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                new SnippetPrinter(System.out).printSnippets(runtime);
            }
        });
        return runtime;
    }

    private static List<String> packageNamesOrScriptPaths(Class featureClass) {
        List<String> packageNamesOrScriptPaths = new ArrayList<String>();
        String featurePackageName = featureClass.getName().substring(0, featureClass.getName().lastIndexOf("."));
        packageNamesOrScriptPaths.add(featurePackageName);

        // Add additional ones
        cucumber.junit.Feature featureAnnotation = (cucumber.junit.Feature) featureClass.getAnnotation(cucumber.junit.Feature.class);
        if (featureAnnotation != null) {
            packageNamesOrScriptPaths.addAll(asList(featureAnnotation.packages()));
        }
        return packageNamesOrScriptPaths;
    }

    private static JUnitReporter jUnitReporter() {
        PrettyFormatter pf = new PrettyFormatter(System.out, false, true);
        return new JUnitReporter(pf, pf);
    }
    
    /**
     * Constructor called by JUnit.
     */
    public Cucumber(Class featureClass) throws InitializationError {
        this(featureClass, runtime(featureClass), jUnitReporter());
    }

    public Cucumber(Class featureClass, final Runtime runtime, JUnitReporter jUnitReporter) throws InitializationError {
        super(featureClass);
        this.runtime = runtime;
        this.jUnitReporter = jUnitReporter;

        featurePath = featurePath(featureClass);
        this.feature = parseFeature(featurePath, filters(featureClass));
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
        Object[] filters = new Object[0];
        if (featureAnnotation != null) {
            filters = toLong(featureAnnotation.lines());
            if (filters.length == 0) {
                filters = featureAnnotation.tags();
            }
        }
        return asList(filters);
    }

    @Override
    public String getName() {
        return feature != null ? feature.getKeyword() + ": " + feature.getName() : "No matching features - did you use too restrictive filters?";
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

    @Override
    public void run(RunNotifier notifier) {
        if (feature != null) {
            jUnitReporter.feature(feature);
            jUnitReporter.uri(featurePath);
            super.run(notifier);
            jUnitReporter.eof();
        }
    }

    private Feature parseFeature(String pathName, final List<Object> filters) {
        List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();
        final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);
        Resources.scan(pathName, new Consumer() {
            public void consume(Resource resource) {
                builder.parse(resource, filters);
            }
        });

        if (cucumberFeatures.isEmpty()) {
            return null;
        } else {
            CucumberFeature cucumberFeature = cucumberFeatures.get(0);
            buildScenarioRunners(cucumberFeature);
            return cucumberFeature.getFeature();
        }
    }

    private void buildScenarioRunners(CucumberFeature cucumberFeature) {
        for (CucumberScenario cucumberScenario : cucumberFeature.getCucumberScenarios()) {
            try {
                scenarioRunners.add(new ScenarioRunner(runtime, cucumberScenario, jUnitReporter));
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
