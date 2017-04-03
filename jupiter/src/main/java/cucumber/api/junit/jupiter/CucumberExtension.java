package cucumber.api.junit.jupiter;

import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.junit.jupiter.JunitJupiterReporter;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.*;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * A Junit Jupiter extension to inject Cucumber scenarios into a TestFactory.
 * Annotate your Cucumber runner class with {@code @ExtendsWith(CucumberExtension.class)}.
 * Declare a {@code TestFactory} method with a {@code Stream<DynamicTest>} parameter
 * and return the provided parameter from the method. You can annotate the class with
 * {@code CucumberOptions}.
 *
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
  @Override
  public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext)
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
  public Object resolve(ParameterContext parameterContext,
                        ExtensionContext extensionContext) throws ParameterResolutionException {
    if (!extensionContext.getTestClass().isPresent()) {
      throw new IllegalStateException("resolve has been called even though the supports check failed");
    }

    Class testClass = extensionContext.getTestClass().get();
    RuntimeOptions options = new RuntimeOptionsFactory(testClass).create();
    ClassLoader classLoader = testClass.getClassLoader();
    MultiLoader resourceLoader = new MultiLoader(classLoader);
    ResourceLoaderClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
    runtime = new Runtime(resourceLoader, classFinder, classLoader, options);
    List<CucumberFeature> cucumberFeatures = options.cucumberFeatures(resourceLoader);
    formatter = options.formatter(classLoader);
    reporter = options.reporter(classLoader);
    jupiterReporter = new JunitJupiterReporter(reporter);

    return cucumberFeatures.stream().flatMap(feature -> {
      formatter.uri(feature.getPath());
      formatter.feature(feature.getGherkinFeature());


      return feature.getFeatureElements().stream().flatMap(featureElement -> {
        if (featureElement.getClass().equals(CucumberScenario.class)) {
          return Stream.of(createDynamicTest((CucumberScenario) featureElement));
        } else if (featureElement.getClass().equals(CucumberScenarioOutline.class)) {
          CucumberScenarioOutline outline = (CucumberScenarioOutline) featureElement;
          return createDynamicTests(outline.getCucumberExamplesList());
        } else {
          throw new IllegalStateException("Unexpected tag statement " + featureElement.getGherkinModel().getKeyword()
              + " " + featureElement.getVisualName());
        }
      });

    });
  }

  private DynamicTest createDynamicTest(CucumberScenario scenario) {
    return dynamicTest(scenario.getVisualName(), () -> {
      jupiterReporter.scenario((Scenario) scenario.getGherkinModel());
      scenario.run(formatter, reporter, runtime);
      Result result = jupiterReporter.getResult(scenario.getGherkinModel());

      Throwable error = result.getError();
      if (error != null) {
        throw error;
      }

      // If the scenario is skipped, then the test is aborted (neither passes nor fails).
      assumeFalse(Result.SKIPPED == result);
    });
  }

  private Stream<DynamicTest> createDynamicTests(List<CucumberExamples> listOfExamples) {
    return listOfExamples.stream().flatMap(examples ->
        examples.createExampleScenarios().stream().map(this::createDynamicTest));
  }

  @Override
  public void afterAll(ContainerExtensionContext context) throws Exception {
    formatter.done();
    formatter.eof();
    formatter.close();
  }

  private JunitJupiterReporter jupiterReporter;
  private Reporter reporter;
  private Formatter formatter;
  private Runtime runtime;
}
