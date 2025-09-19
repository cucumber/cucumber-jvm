package io.cucumber.core.plugin;

import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.UUID;

import static io.cucumber.core.plugin.Bytes.bytes;
import static io.cucumber.core.plugin.IsEqualCompressingLineSeparators.equalCompressingLineSeparators;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.threeReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static org.hamcrest.MatcherAssert.assertThat;

class DefaultSummaryPrinterTest {

    @Test
    void writesSummary() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");

        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofMillis(1128));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new DefaultSummaryPrinter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", oneReference()),
                    new StubStepDefinition("second step", twoReference()),
                    new StubStepDefinition("third step", threeReference())))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("" +
                "\n" +
                "1 scenarios (1 passed)\n" +
                "3 steps (3 passed)\n" +
                "0m 3.384s\n")));
    }

}
