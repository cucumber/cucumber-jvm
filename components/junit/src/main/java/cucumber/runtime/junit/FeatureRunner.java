package cucumber.runtime.junit;

import cucumber.runtime.*;
import cucumber.runtime.Runtime;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.FeatureElement;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;

public class FeatureRunner extends ParentRunner<ParentRunner> {
    private Feature feature;
    private final Class featureClass;
    private final Runtime runtime;
    private List<ParentRunner> children;

    private static Runtime runtime(Class testClass) {
        String packageName = testClass.getName().substring(0, testClass.getName().lastIndexOf("."));
        // TODO: Using the package name as glueCodePrefix will work for the JavaBackend, but
        // not for other backends that require a script path. We'll have to provide alternative
        // mechanisms to look it up, such as an annotation or a system property.
        return new Runtime(packageName);
    }

    /**
     * Constructor called by JUnit.
     */
    public FeatureRunner(Class testClass) throws InitializationError {
        this(testClass, runtime(testClass));
    }

    public FeatureRunner(Class testClass, Runtime runtime) throws InitializationError {
        super(null);
        featureClass = testClass;
        this.runtime = runtime;
        feature = parseFeature();
        children = new ArrayList<ParentRunner>();
        for (FeatureElement featureElement : feature.getFeatureElements()) {
            try {
                if(featureElement instanceof ScenarioOutline) {
                    children.add(new ScenarioOutlineRunner(runtime, (ScenarioOutline) featureElement));
                } else {
                    children.add(new ScenarioRunner(runtime, (Scenario) featureElement));
                }
            } catch (InitializationError initializationError) {
                throw new CucumberException("This shouldn't be possible", initializationError);
            }
        }
    }

    @Override
    public String getName() {
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
    public void run(RunNotifier notifier) {
        super.run(notifier);
    }

    @Override
    protected void runChild(ParentRunner runner, RunNotifier notifier) {
        runner.run(notifier);
        for (String snippet : runtime.getSnippets()) {
            System.out.println(snippet);
        }
    }

    private Feature parseFeature() {
        final String[] gherkin = new String[1];
        Classpath.scan(featureClass.getName().replace('.', '/') + ".feature", new Consumer() {
            public void consume(Input input) {
                gherkin[0] = input.getString();
            }
        });
        return Feature.parseGherkin(gherkin[0]);
    }
}
