package cucumber.runtime.junit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import cucumber.runner.ThreadLocalRunnerSupplier;
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
                mock(ThreadLocalRunnerSupplier.class),
                pickles.get(0),
                createJUnitOptions()
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
                mock(ThreadLocalRunnerSupplier.class),
                pickles.get(0),
                createJUnitOptions("--filename-compatible-names")
        );

        assertEquals("scenario_name(feature_name)", runner.getDescription().getDisplayName());
    }

    private JUnitOptions createJUnitOptions() {
        return new JUnitOptions(true, Collections.<String>emptyList());
    }

    private JUnitOptions createJUnitOptions(String option) {
        return new JUnitOptions(true, Arrays.asList(option));
    }
}
