package io.cucumber.junit;

import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.junit.PickleRunners.PickleRunner;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

class PickleRunnerWithNoStepDescriptionsTest {

    @Test
    void shouldUseScenarioNameWithFeatureNameAsClassNameForDisplayName() {
        List<CucumberPickle> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
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
    void shouldConvertTextFromFeatureFileForNamesWithFilenameCompatibleNameOption() {
        List<CucumberPickle> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
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
    void shouldConvertTextFromFeatureFileWithRussianLanguage() {
        List<CucumberPickle> pickles = TestPickleBuilder.pickleEventsFromFeature("featurePath", "" +
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
