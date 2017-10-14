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
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.*;

import java.util.List;
import java.util.stream.Stream;

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
 *     public Stream&lt;DynamicTest&gt; runAllCucumberScenarios(Stream&lt;DynamicTest&gt; scenarios) {
 *       return scenarios;
 *     }
 *   }
 * </code></pre>
 */
public class CucumberExtension implements ParameterResolver, AfterAllCallback {

    private Runtime runtime;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
        return (extensionContext.getTestClass().isPresent()
            // Only applies on TestFactory methods.
            && extensionContext.getTestMethod().isPresent()
            && extensionContext.getTestMethod().get().getAnnotationsByType(TestFactory.class).length > 0
            && Stream.class == parameterContext.getParameter().getType()
            && "java.util.stream.Stream<org.junit.jupiter.api.DynamicTest>" // Can't find a better way to do this :(
            .equals(parameterContext.getParameter().getParameterizedType().getTypeName()));
    }

    @Override
    public Stream<DynamicTest> resolveParameter(ParameterContext parameterContext,
                                                ExtensionContext extensionContext) throws ParameterResolutionException {
        if (!extensionContext.getTestClass().isPresent()) {
            throw new IllegalStateException("resolve has been called even though the supports check failed");
        }

        Class testClass = extensionContext.getTestClass().get();
        RuntimeOptions runtimeOptions = new RuntimeOptionsFactory(testClass).create();
        ClassLoader classLoader = testClass.getClassLoader();
        MultiLoader resourceLoader = new MultiLoader(classLoader);
        ResourceLoaderClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
        List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader, runtime.getEventBus());
        Compiler compiler = new Compiler();

        Stream<PickleEvent> compiledPickles = cucumberFeatures.stream().flatMap(
            cucumberFeature -> compiler.compile(cucumberFeature.getGherkinFeature())
                .stream()
                .map(pickle -> new PickleEvent(cucumberFeature.getUri(), pickle)));

        return compiledPickles
            .filter(runtime::matchesFilters)
            .map(pickleEvent ->
                dynamicTest(pickleEvent.pickle.getName(), () -> executeTest(runtime, runtimeOptions, pickleEvent)));
    }

    private void executeTest(Runtime runtime, RuntimeOptions runtimeOptions, PickleEvent pickleEvent) throws Throwable {
        JunitJupiterReporter reporter = new JunitJupiterReporter(runtime.getEventBus());
        runtime.getRunner().runPickle(pickleEvent);
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
