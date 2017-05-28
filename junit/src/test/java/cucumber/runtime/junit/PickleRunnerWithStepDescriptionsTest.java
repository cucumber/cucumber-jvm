package cucumber.runtime.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runtime.junit.PickleRunners.PickleRunner;
import cucumber.runtime.junit.PickleRunners.WithStepDescriptions;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleStep;
import org.junit.Test;
import org.junit.runner.Description;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PickleRunnerWithStepDescriptionsTest {

    @Test
    public void should_assign_unequal_descriptions_to_different_occurrences_of_same_step_in_a_scenario() throws Exception {
        CucumberFeature features = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: FB\n" +
            "# Scenario with same step occurring twice\n" +
            "\n" +
            "  Scenario: SB\n" +
            "    When foo\n" +
            "    Then bar\n" +
            "\n" +
            "    When foo\n" +
            "    Then baz\n"
        );

        Compiler compiler = new Compiler();
        List<PickleEvent> pickleEvents = new ArrayList<PickleEvent>();
        for (Pickle pickle : compiler.compile(features.getGherkinFeature())) {
            pickleEvents.add(new PickleEvent(features.getPath(), pickle));
        };

        WithStepDescriptions runner = (WithStepDescriptions) PickleRunners.withStepDescriptions(
                mock(Runner.class),
                pickleEvents.get(0),
                createStandardJUnitReporter()
        );

        // fish out the two occurrences of the same step and check whether we really got them
        PickleStep stepOccurrence1 = runner.getChildren().get(0);
        PickleStep stepOccurrence2 = runner.getChildren().get(2);
        assertEquals(stepOccurrence1.getText(), stepOccurrence2.getText());

        // then check that the descriptions are unequal
        Description runnerDescription = runner.getDescription();

        Description stepDescription1 = runnerDescription.getChildren().get(0);
        Description stepDescription2 = runnerDescription.getChildren().get(2);

        assertFalse("Descriptions must not be equal.", stepDescription1.equals(stepDescription2));
    }

    @Test
    public void should_include_scenario_name_as_class_name_in_step_descriptions() throws Exception {
        CucumberFeature features = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: In cucumber.junit\n" +
            "  Scenario: first\n" +
            "    When step\n" +
            "    Then another step\n" +
            "\n" +
            "  Scenario: second\n" +
            "    When step\n" +
            "    Then another step\n"
        );

        Compiler compiler = new Compiler();
        List<PickleEvent> pickleEvents = new ArrayList<PickleEvent>();
        for (Pickle pickle : compiler.compile(features.getGherkinFeature())) {
            pickleEvents.add(new PickleEvent(features.getPath(), pickle));
        }

        PickleRunner runner = PickleRunners.withStepDescriptions(
                mock(Runner.class),
                pickleEvents.get(0),
                createStandardJUnitReporter()
        );

        // fish out the data from runner
        Description runnerDescription = runner.getDescription();
        Description stepDescription = runnerDescription.getChildren().get(0);

        assertEquals("description includes scenario name as class name", "first", stepDescription.getClassName());
        assertEquals("description includes step keyword and name as method name", "step", stepDescription.getMethodName());
        assertEquals("description includes scenario and step name in display name", "step(first)", stepDescription.getDisplayName());

    }

    @Test
    public void should_use_scenario_name_for_display_name() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withStepDescriptions(
                mock(Runner.class),
                pickles.get(0),
                createStandardJUnitReporter()
        );

        assertEquals("scenario name", runner.getDescription().getDisplayName());
    }

    @Test
    public void should_use_scenario_name_for_description_display_name() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Then it works\n");

        PickleRunner runner = PickleRunners.withStepDescriptions(
            mock(Runner.class),
            pickles.get(0),
            createStandardJUnitReporter()
        );

        assertEquals("scenario name", runner.getDescription().getDisplayName());
    }

    @Test
    public void should_use_step_keyword_and_name_for_child_name() throws Exception {
        List<PickleEvent> pickleEvents = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withStepDescriptions(
                mock(Runner.class),
                pickleEvents.get(0),
                createStandardJUnitReporter()
        );

        assertEquals("it works", runner.getDescription().getChildren().get(0).getMethodName());
    }

    @Test
    public void should_convert_text_from_feature_file_for_names_with_filename_compatible_name_option() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withStepDescriptions(
                mock(Runner.class),
                pickles.get(0),
                createJUnitReporterWithOption("--filename-compatible-names")
        );

        assertEquals("scenario_name", runner.getDescription().getDisplayName());
        assertEquals("scenario_name", runner.getDescription().getChildren().get(0).getClassName());
        assertEquals("it_works", runner.getDescription().getChildren().get(0).getMethodName());
    }

    private JUnitReporter createStandardJUnitReporter() {
        return new JUnitReporter(mock(EventBus.class), false, new JUnitOptions(Collections.<String>emptyList()));
    }

    private JUnitReporter createJUnitReporterWithOption(String option) {
        return new JUnitReporter(mock(EventBus.class), false, new JUnitOptions(Arrays.asList(option)));
    }
}
