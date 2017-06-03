package cucumber.runtime.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runtime.junit.PickleRunners.PickleRunner;
import gherkin.events.PickleEvent;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PickleRunnerWithNoStepDescriptionsTest {

    @Test
    public void should_use_runner_class_in_pickle_description() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            RunCukesTest.class,
            mock(Runner.class),
            pickles.get(0),
            createStandardJUnitReporter()
        );

        // Surefire 47 uses class names to group test reports. To ensure the tests results
        // end up in the proper report we provide the class of the runner. We can't fake
        // this because the class must be loadable by surefire.
        assertEquals(RunCukesTest.class, runner.getDescription().getTestClass());
    }

    @Test
    public void should_create_unequal_descriptions_for_scenarios_with_equal_names() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Then it works\n" +
            "  Scenario: scenario name\n" +
            "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            RunCukesTest.class,
            mock(Runner.class),
            pickles.get(0),
            createStandardJUnitReporter()
        );

        PickleRunner runner1 = PickleRunners.withNoStepDescriptions(
            RunCukesTest.class,
            mock(Runner.class),
            pickles.get(1),
            createStandardJUnitReporter()
        );

        // Also note that we must provide the canonical name as a string because
        // createTestDescription(Class<?>, String) carries the implicit assumption that the
        // combination of class and name is unique which we can not grantee as the runner
        // run contain one or more feature files with the same scenario.
        assertNotEquals(runner.getDescription(), runner1.getDescription());
    }


    @Test
    public void should_use_scenario_name_for_display_name() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            RunCukesTest.class,
            mock(Runner.class),
            pickles.get(0),
            createStandardJUnitReporter()
        );

        assertEquals("scenario name(cucumber.runtime.junit.RunCukesTest)", runner.getDescription().getDisplayName());
    }

    @Test
    public void should_use_scenario_name_for_description_display_name() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            RunCukesTest.class,
            mock(Runner.class),
            pickles.get(0),
            createStandardJUnitReporter()
        );

        assertEquals("scenario name(cucumber.runtime.junit.RunCukesTest)", runner.getDescription().getDisplayName());
    }

    @Test
    public void should_convert_text_from_feature_file_for_names_with_filename_compatible_name_option() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            RunCukesTest.class,
            mock(Runner.class),
            pickles.get(0),
            createJUnitReporterWithOption("--filename-compatible-names")
        );

        assertEquals("scenario_name(cucumber.runtime.junit.RunCukesTest)", runner.getDescription().getDisplayName());
    }

    private JUnitReporter createStandardJUnitReporter() {
        return new JUnitReporter(mock(EventBus.class), false, new JUnitOptions(Collections.<String>emptyList()));
    }

    private JUnitReporter createJUnitReporterWithOption(String option) {
        return new JUnitReporter(mock(EventBus.class), false, new JUnitOptions(Arrays.asList(option)));
    }
}
