package io.cucumber.junit;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.Options;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runtime.CucumberExecutionContext;
import io.cucumber.core.runtime.ExitStatus;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.junit.PickleRunners.PickleRunner;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

class PickleRunnerWithNoStepDescriptionsTest {

    final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    final Options options = RuntimeOptions.defaultOptions();
    final RunnerSupplier runnerSupplier = new MockRunnerSupplier();
    final CucumberExecutionContext context = new CucumberExecutionContext(bus, new ExitStatus(options), runnerSupplier);

    @Test
    void shouldUseScenarioNameWithFeatureNameAsClassNameForDisplayName() {
        List<Pickle> pickles = TestPickleBuilder.picklesFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            "feature name",
            context,
            pickles.get(0),
            null,
            createJunitOptions());

        assertThat(runner.getDescription().getDisplayName(), is(equalTo("scenario name(feature name)")));
    }

    private JUnitOptions createJunitOptions() {
        return new JUnitOptionsBuilder().build();
    }

    @Test
    void shouldConvertTextFromFeatureFileForNamesWithFilenameCompatibleNameOption() {
        List<Pickle> pickles = TestPickleBuilder.picklesFromFeature("featurePath", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Then it works\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            "feature name",
            context,
            pickles.get(0),
            null,
            createFileNameCompatibleJUnitOptions());

        assertThat(runner.getDescription().getDisplayName(), is(equalTo("scenario_name(feature_name)")));
    }

    private JUnitOptions createFileNameCompatibleJUnitOptions() {
        return new JUnitOptionsBuilder().setFilenameCompatibleNames(true).build();
    }

    @Test
    void shouldConvertTextFromFeatureFileWithRussianLanguage() {
        List<Pickle> pickles = TestPickleBuilder.picklesFromFeature("featurePath", "" +
                "#language:ru\n" +
                "Функция: имя функции\n" +
                "  Сценарий: имя сценария\n" +
                "    Тогда он работает\n");

        PickleRunner runner = PickleRunners.withNoStepDescriptions(
            "имя функции",
            context,
            pickles.get(0),
            null,
            createFileNameCompatibleJUnitOptions());

        assertThat(runner.getDescription().getDisplayName(), is(equalTo("____________(___________)")));
    }

    private class MockRunnerSupplier implements RunnerSupplier {
        @Override
        public Runner get() {
            return null;
        }
    }
}
