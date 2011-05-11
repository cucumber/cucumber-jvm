package cucumber.runtime.junit;

import cucumber.classpath.Classpath;
import cucumber.classpath.Consumer;
import cucumber.classpath.Input;
import cucumber.runtime.Runtime;
import gherkin.GherkinParser;
import gherkin.model.Feature;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

public class FeatureRunner extends ParentRunner<ParentRunner> {
    private final Class featureClass;
    private final Feature feature;
    private final RunnerBuilder builder;

    private static Runtime runtime(Class testClass) {
        String packageName = testClass.getName().substring(0, testClass.getName().lastIndexOf("."));
        // TODO: Using the package name as glueCodePrefix will work for the JavaBackend, but
        // not for other backends that require a script path. We'll have to provide alternative
        // mechanisms to look it up, such as an annotation or a system property.
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
    public FeatureRunner(Class testClass) throws InitializationError {
        this(testClass, runtime(testClass));
    }

    public FeatureRunner(Class testClass, final Runtime runtime) throws InitializationError {
        super(null);
        featureClass = testClass;
        feature = parseFeature();
        builder = new RunnerBuilder(runtime);
        feature.accept(builder);
    }

    @Override
    public String getName() {
        return feature.getKeyword() + ": " + feature.getName();
    }

    @Override
    protected List<ParentRunner> getChildren() {
        return builder.getFeatureElementRunners();
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
    }

    private Feature parseFeature() {
        final String[] gherkin = new String[1];
        String pathName = featureClass.getName().replace('.', '/') + ".feature";
        Classpath.scan(pathName, new Consumer() {
            public void consume(Input input) {
                gherkin[0] = input.getString();
            }
        });
        return new GherkinParser().parse(gherkin[0], pathName, 0);
    }
}
