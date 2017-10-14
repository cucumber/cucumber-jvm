package cucumber.api.junit.jupiter;

import cucumber.api.event.TestRunFinished;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.junit.jupiter.JunitJupiterReporter;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.*;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * A Junit Jupiter extension to inject Cucumber scenarios into a TestFactory.
 * Annotate your Cucumber runner class with {@code @ExtendsWith(CucumberExtension.class)}.
 * Declare a {@code TestFactory} method with a {@code Stream<DynamicTest>} parameter
 * and return the provided parameter from the method. You can annotate the class with
 * {@code CucumberOptions}.
 * <p>
 * <pre><code>
 *   &#64;CucumberOptions(
 *     plugin = {"pretty"},
 *     features = {"classpath:features"}
 *   )
 *   &#64;ExtendWith(CucumberExtension.class)
 *   public class BehaviourRunner {
 *     &#64;TestFactory
 *     public Stream&lt;DynamicContainer&gt; runAllCucumberScenarios(Stream&lt;DynamicTest&gt; scenarios) {
 *       return scenarios;
 *     }
 *   }
 * </code></pre>
 */
public class CucumberExtension implements ParameterResolver, AfterAllCallback {

    private final Compiler compiler = new Compiler();

    private Runtime runtime;
    private RuntimeOptions runtimeOptions;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
        return (extensionContext.getTestClass().isPresent()
            // Only applies on TestFactory methods.
            && extensionContext.getTestMethod().isPresent()
            && extensionContext.getTestMethod().get().getAnnotationsByType(TestFactory.class).length > 0
            && Stream.class == parameterContext.getParameter().getType()
            && "java.util.stream.Stream<org.junit.jupiter.api.DynamicContainer>" // Can't find a better way to do this :(
            .equals(parameterContext.getParameter().getParameterizedType().getTypeName()));
    }

    @Override
    public Stream<DynamicContainer> resolveParameter(ParameterContext parameterContext,
                                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        if (!extensionContext.getTestClass().isPresent()) {
            throw new IllegalStateException("resolve has been called even though the supports check failed");
        }

        Class testClass = extensionContext.getTestClass().get();
        runtimeOptions = new RuntimeOptionsFactory(testClass).create();
        ClassLoader classLoader = testClass.getClassLoader();
        MultiLoader resourceLoader = new MultiLoader(classLoader);
        ResourceLoaderClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);

        List<CucumberFeature> features = runtimeOptions.cucumberFeatures(resourceLoader, runtime.getEventBus());

        return features.stream()
            .map(feature -> dynamicContainer(
                feature.getGherkinFeature().getFeature().getName(),
                compile(feature)
                    .filter(runtime::matchesFilters)
                    .map(pickle -> dynamicTest(pickle.pickle.getName(), () -> executeTest(pickle)))));
    }

    private Stream<PickleEvent> compile(CucumberFeature feature) {
        return compiler.compile(feature.getGherkinFeature()).stream()
            .map(pickle -> new PickleEvent(feature.getUri(), pickle));
    }

    private void executeTest(PickleEvent pickle) throws Throwable {
        JunitJupiterReporter reporter = new JunitJupiterReporter(runtime.getEventBus());
        runtime.getRunner().runPickle(pickle);
        if (reporter.isOk(runtimeOptions.isStrict())) {
            return;
        }
        throw reporter.getError();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        runtime.getEventBus().send(new TestRunFinished(runtime.getEventBus().getTime()));
        runtime.printSummary();
    }
}
