package cucumber.runtime.junit;

import static org.junit.Assert.assertEquals;
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
    public void shouldUseScenarioNameWithFeatureNameAsClassNameForDisplayName() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
                "feature name",
                mock(Runner.class),
                pickles.get(0),
                createStandardJUnitReporter()
        );

        assertEquals("scenario name(feature name)", runner.getDescription().getDisplayName());
    }

    @Test
    public void shouldConvertTextFromFeatureFileForNamesWithFilenameCompatibleNameOption() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
                "feature name",
                mock(Runner.class),
                pickles.get(0),
                createJUnitReporterWithOption("--filename-compatible-names")
        );

        assertEquals("scenario_name(feature_name)", runner.getDescription().getDisplayName());
    }

    private JUnitReporter createStandardJUnitReporter() {
        return new JUnitReporter(mock(EventBus.class), false, new JUnitOptions(Collections.<String>emptyList()));
    }

    private JUnitReporter createJUnitReporterWithOption(String option) {
        return new JUnitReporter(mock(EventBus.class), false, new JUnitOptions(Arrays.asList(option)));
    }
}
