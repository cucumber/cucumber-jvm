package io.cucumber.junit;

import gherkin.events.PickleEvent;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.junit.PickleRunners.PickleRunner;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

public class PickleRunnerWithNoStepDescriptionsTest {

    @Test
    public void shouldUseScenarioNameWithFeatureNameAsClassNameForDisplayName() {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            "feature name",
            mock(RunnerSupplier.class),
            pickles.get(0),
            createJunitOptions()
        );

        assertThat(runner.getDescription().getDisplayName(), is(equalTo("scenario name(feature name)")));
    }

    @Test
    public void shouldConvertTextFromFeatureFileForNamesWithFilenameCompatibleNameOption() {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
            "Feature: feature name\n" +
            "  Scenario: scenario name\n" +
            "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            "feature name",
            mock(RunnerSupplier.class),
            pickles.get(0),
            createFileNameCompatibleJUnitOptions()
        );

        assertThat(runner.getDescription().getDisplayName(), is(equalTo("scenario_name(feature_name)")));
    }

    @Test
    public void shouldConvertTextFromFeatureFileWithRussianLanguage() {
        List<PickleEvent> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
            "#language:ru\n" +
            "Функция: имя функции\n" +
            "  Сценарий: имя сценария\n" +
            "    Тогда он работает\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            "имя функции",
            mock(RunnerSupplier.class),
            pickles.get(0),
            createFileNameCompatibleJUnitOptions()
        );

        assertThat(runner.getDescription().getDisplayName(), is(equalTo("____________(___________)")));
    }

    private JUnitOptions createJunitOptions() {
        return new JUnitOptionsBuilder().setStrict(true).build();
    }

    private JUnitOptions createFileNameCompatibleJUnitOptions() {
        return new JUnitOptionsBuilder().setFilenameCompatibleNames(true).setStrict(true).build();
    }

}
