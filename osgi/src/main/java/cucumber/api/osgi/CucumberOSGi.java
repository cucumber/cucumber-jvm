package cucumber.api.osgi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import cucumber.api.CucumberOptions;
import cucumber.api.event.TestRunFinished;
import cucumber.api.java.ObjectFactory;
import cucumber.api.junit.Cucumber;
import cucumber.java.runtime.osgi.OsgiClassFinder;
import cucumber.java.runtime.osgi.OsgiObjectFactory;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Glue;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.io.FileResourceLoader;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.junit.Assertions;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitOptions;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;

/**
 * <p>
 * Classes annotated with {@code @RunWith(CucumberOSGi.class)} will run a Cucumber Feature
 * inside an OSGi container using tycho.
 * 
 * @see Cucumber
 * @see CucumberOptions
 */
public class CucumberOSGi extends ParentRunner<FeatureRunner> {

	private final JUnitReporter jUnitReporter;
    private final List<FeatureRunner> children = new ArrayList<FeatureRunner>();
    private final Runtime runtime;
    private final ResourceLoader resourceLoader;
    private final boolean isRunningInContainer;

    /**
     * Constructor called by JUnit.
     *
     * @param testClass the class with the @RunWith annotation.
     * @throws InitializationError  if there is a problem
     * @throws java.io.IOException  if there is a problem
     */
    public CucumberOSGi(Class<?> testClass) throws InitializationError, IOException {
		super(testClass);
		Assertions.assertNoCucumberAnnotatedMethods(testClass);
		
		Bundle testBundle = FrameworkUtil.getBundle(testClass);
		isRunningInContainer = (testBundle != null);
		
		RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(testClass);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        
        /**
         * The <code>isRunningInContainer</code> variable is used to only execute
         * the Cucumber tests if this runner is actually executed inside an OSGi 
         * container.  In a maven build, this would be the case.  However, during 
         * active development in an IDE, this would not necessarily be the case, 
         * and the runner would fail. 
         */
		if (isRunningInContainer) {
		
			BundleContext bundleContext = testBundle.getBundleContext();
			ClassLoader classLoader = Runtime.class.getClassLoader();
			resourceLoader = new MultiLoader(classLoader);
            ObjectFactory objectFactory = new OsgiObjectFactory(bundleContext);
            ClassFinder classFinder = new OsgiClassFinder(bundleContext);
            Backend backend = new JavaBackend(objectFactory, classFinder);
            
            runtime = new Runtime(resourceLoader, classLoader, Collections.singleton(backend), runtimeOptions);
		}
		else {
			
			resourceLoader = new FileResourceLoader();
			ClassLoader classLoader = Runtime.class.getClassLoader();
			Backend backend = new DummyBackend();
			
			runtime = new Runtime(resourceLoader, classLoader, Collections.singleton(backend), runtimeOptions);
		}
		
		final JUnitOptions junitOptions = new JUnitOptions(runtimeOptions.getJunitOptions());
		final List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader, runtime.getEventBus());
        jUnitReporter = new JUnitReporter(runtime.getEventBus(), runtimeOptions.isStrict(), junitOptions);
        addChildren(cucumberFeatures);
	}

    @Override
    public List<FeatureRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(FeatureRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(FeatureRunner child, RunNotifier notifier) {
    	if (isRunningInContainer) {
    		child.run(notifier);
    	}
    }

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        final Statement features = super.childrenInvoker(notifier);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                features.evaluate();
                runtime.getEventBus().send(new TestRunFinished(runtime.getEventBus().getTime()));
                runtime.printSummary();
            }
        };
    }

    private void addChildren(List<CucumberFeature> cucumberFeatures) throws InitializationError {
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            children.add(new FeatureRunner(cucumberFeature, runtime, jUnitReporter));
        }
    }
    
    
    private class DummyBackend implements Backend {
    	
    	@Override
		public void loadGlue(Glue glue, List<String> gluePaths) {
			// Do nothing
		}

		@Override
		public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
			// Do nothing
		}

		@Override
		public void buildWorld() {
			// Do nothing
		}

		@Override
		public void disposeWorld() {
			// Do nothing
		}

		@Override
		public String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
			return null;
		}
    }
}
