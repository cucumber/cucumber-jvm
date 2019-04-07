package cucumber.runtime.junit;

import cucumber.runner.TimeServiceEventBus;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runtime.Backend;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.RuntimeOptions;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.model.CucumberFeature;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class FeatureRunnerTest {

    private static void assertDescriptionIsPredictable(Description description, Set<Description> descriptions) {
        assertTrue(descriptions.contains(description));
        for (Description each : description.getChildren()) {
            assertDescriptionIsPredictable(each, descriptions);
        }
    }

    private static void assertDescriptionIsUnique(Description description, Set<Description> descriptions) {
        // Note: JUnit uses the the serializable parameter as the unique id when comparing Descriptions
        assertTrue(descriptions.add(description));
        for (Description each : description.getChildren()) {
            assertDescriptionIsUnique(each, descriptions);
        }
    }

    @Test
    public void should_not_create_step_descriptions_by_default() throws Exception {
        CucumberFeature cucumberFeature = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background:\n" +
            "    Given background step\n" +
            "  Scenario: A\n" +
            "    Then scenario name\n" +
            "  Scenario: B\n" +
            "    Then scenario name\n" +
            "  Scenario Outline: C\n" +
            "    Then scenario <name>\n" +
            "  Examples:\n" +
            "    | name |\n" +
            "    | C    |\n" +
            "    | D    |\n" +
            "    | E    |\n"

        );

        FeatureRunner runner = createFeatureRunner(cucumberFeature);

        Description feature = runner.getDescription();
        Description scenarioA = feature.getChildren().get(0);
        assertTrue(scenarioA.getChildren().isEmpty());
        Description scenarioB = feature.getChildren().get(1);
        assertTrue(scenarioB.getChildren().isEmpty());
        Description scenarioC0 = feature.getChildren().get(2);
        assertTrue(scenarioC0.getChildren().isEmpty());
        Description scenarioC1 = feature.getChildren().get(3);
        assertTrue(scenarioC1.getChildren().isEmpty());
        Description scenarioC2 = feature.getChildren().get(4);
        assertTrue(scenarioC2.getChildren().isEmpty());
    }

    @Test
    public void should_not_issue_notification_for_steps_by_default_scenario_outline_with_two_examples_table_and_background() throws Throwable {
        CucumberFeature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background\n" +
            "    Given first step\n" +
            "  Scenario Outline: scenario outline name\n" +
            "    When <x> step\n" +
            "    Then <y> step\n" +
            "    Examples: examples 1 name\n" +
            "      |   x    |   y   |\n" +
            "      | second | third |\n" +
            "      | second | third |\n" +
            "    Examples: examples 2 name\n" +
            "      |   x    |   y   |\n" +
            "      | second | third |\n");

        RunNotifier notifier = runFeatureWithNotifier(feature);

        InOrder order = inOrder(notifier);

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
        order.verify(notifier, times(3)).fireTestAssumptionFailed(argThat(new FailureMatcher("scenario outline name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
        order.verify(notifier, times(3)).fireTestAssumptionFailed(argThat(new FailureMatcher("scenario outline name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
        order.verify(notifier, times(3)).fireTestAssumptionFailed(argThat(new FailureMatcher("scenario outline name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
    }

    @Test
    public void should_not_issue_notification_for_steps_by_default_two_scenarios_with_background() throws Throwable {
        CucumberFeature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background\n" +
            "    Given first step\n" +
            "  Scenario: scenario_1 name\n" +
            "    When second step\n" +
            "    Then third step\n" +
            "  Scenario: scenario_2 name\n" +
            "    Then another second step\n");

        RunNotifier notifier = runFeatureWithNotifier(feature);

        InOrder order = inOrder(notifier);

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_1 name(feature name)")));
        order.verify(notifier, times(3)).fireTestAssumptionFailed(argThat(new FailureMatcher("scenario_1 name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_1 name(feature name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_2 name(feature name)")));
        order.verify(notifier, times(2)).fireTestAssumptionFailed(argThat(new FailureMatcher("scenario_2 name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_2 name(feature name)")));
    }

    private RunNotifier runFeatureWithNotifier(CucumberFeature cucumberFeature, String... options) throws InitializationError {
        FeatureRunner runner = createFeatureRunner(cucumberFeature, options);
        RunNotifier notifier = mock(RunNotifier.class);
        runner.run(notifier);
        return notifier;
    }

    private FeatureRunner createFeatureRunner(CucumberFeature cucumberFeature, String... options) throws InitializationError {
    	return createFeatureRunner(RunCukesTest.class, cucumberFeature, options);
    }

    private FeatureRunner createFeatureRunner(Class<?> clazz, CucumberFeature cucumberFeature, String... options) throws InitializationError {
        JUnitOptions junitOption = new JUnitOptions(false, Arrays.asList(options));
        return createFeatureRunner(clazz, cucumberFeature, junitOption);
    }

    private FeatureRunner createFeatureRunner(Class<?> clazz, CucumberFeature cucumberFeature, JUnitOptions junitOption) throws InitializationError {
        final RuntimeOptions runtimeOptions = new RuntimeOptions("");

        final TimeService timeServiceStub = new TimeService() {
            @Override
            public long time() {
                return 0L;
            }

            @Override
            public long timeMillis() {
                return 0L;
            }
        };
        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                return asList(mock(Backend.class));
            }
        };

        EventBus bus = new TimeServiceEventBus(timeServiceStub);
        Filters filters = new Filters(runtimeOptions);
        ThreadLocalRunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier);
        return new FeatureRunner(clazz, cucumberFeature, filters, runnerSupplier, junitOption);
    }
    
    public static class RunCukesTest {
    }

    @Test
    public void should_populate_descriptions_with_stable_unique_ids() throws Exception {
        CucumberFeature cucumberFeature = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background:\n" +
            "    Given background step\n" +
            "  Scenario: A\n" +
            "    Then scenario name\n" +
            "  Scenario: B\n" +
            "    Then scenario name\n" +
            "  Scenario Outline: C\n" +
            "    Then scenario <name>\n" +
            "  Examples:\n" +
            "    | name |\n" +
            "    | C    |\n" +
            "    | D    |\n" +
            "    | E    |\n"

        );

        FeatureRunner runner = createFeatureRunner(cucumberFeature);
        FeatureRunner rerunner = createFeatureRunner(cucumberFeature);

        Set<Description> descriptions = new HashSet<Description>();
        assertDescriptionIsUnique(runner.getDescription(), descriptions);
        assertDescriptionIsPredictable(runner.getDescription(), descriptions);
        assertDescriptionIsPredictable(rerunner.getDescription(), descriptions);

    }
    
    @Test
    public void should_run_junit_rules() throws InitializationError {
        CucumberFeature cucumberFeature = TestPickleBuilder.parseFeature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: A\n" +
                "    Then scenario name\n" +
                "  Scenario: B\n" +
                "    Then scenario name\n"
            );

            FeatureRunner runner = createFeatureRunner(RunCukesTestWithRules.class, cucumberFeature);
            RunNotifier notifier = mock(RunNotifier.class);
            runner.run(notifier);
            
            assertEquals(1, classRuleActivations.size());
            assertEquals(2, ruleActivations.size());
            assertEquals(2, methodRuleActivations.size());
    }
    
	public static final List<Object[]> classRuleActivations = new LinkedList<>();
	public static final List<Object[]> ruleActivations = new LinkedList<>();
	public static final List<Object[]> methodRuleActivations = new LinkedList<>();

    public static class RunCukesTestWithRules {
    	
    	public static class MyTestRule implements TestRule {
    		
    		private List<Object[]> activations;
    		
    		public MyTestRule(List<Object[]> activations) {
    			this.activations = activations;
    		}

    		@Override
    		public Statement apply(Statement base, Description description) {
    			activations.add(new Object[] { base, description });
    			return base;
    		}
    		
    	}

    	public static class MyMethodRule implements MethodRule {

    		private List<Object[]> activations;

    		public MyMethodRule(List<Object[]> activations) {
    			this.activations = activations;
    		}

    		@Override
    		public Statement apply(Statement base, FrameworkMethod method, Object target) {
    			activations.add(new Object[] { base, method, target });
    			return base;
    		}

    	}
    	
    	@ClassRule
    	public static TestRule classRule = new MyTestRule(classRuleActivations);

    	@Rule
    	public TestRule rule = new MyTestRule(ruleActivations);
    	
    	@Rule
    	public MethodRule methodRule = new MyMethodRule(methodRuleActivations);
    	    	
    }

}

