package io.cucumber.junit;

import cucumber.runner.RunnerSupplier;
import gherkin.events.PickleEvent;
import io.cucumber.junit.PickleRunners.PickleRunner;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class PickleRunnerWithNoStepDescriptionsTest {

    @Test
    public void shouldUseScenarioNameWithFeatureNameAsClassNameForDisplayName() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
                "feature name",
                mock(RunnerSupplier.class),
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
                mock(RunnerSupplier.class),
                pickles.get(0),
                createJUnitOptions("--filename-compatible-names")
        );

        assertEquals("scenario_name(feature_name)", runner.getDescription().getDisplayName());
    }

    @Test
    public void shouldConvertTextFromFeatureFileWithRussianLanguage() throws Exception {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
            "#language:ru\n" +
            "Функция: имя функции\n" +
            "  Сценарий: имя сценария\n" +
            "    Тогда он работает\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            "имя функции",
            mock(RunnerSupplier.class),
            pickles.get(0),
            createJUnitOptions("--filename-compatible-names")
        );

        assertEquals("____________(___________)", runner.getDescription().getDisplayName());
    }

    private JUnitOptions createJUnitOptions() {
        return new JUnitOptionsParser().parse(Collections.<String>emptyList()).setStrict(true).build();
    }

    private JUnitOptions createJUnitOptions(String option) {
        return new JUnitOptionsParser().parse(Collections.singletonList(option)).setStrict(true).build();
    }
}
