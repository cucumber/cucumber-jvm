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
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static org.assertj.core.api.Assertions.assertThat;

class UsageFormatterTest {

    @Test
    void writes_empty_report() throws UnsupportedEncodingException {
        Feature feature = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name
                """);

        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofSeconds(1));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new UsageFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier())
                .build()
                .run();

        assertThat(out.toString(StandardCharsets.UTF_8)).isEmpty();
    }

    @Test
    void writes_usage_report() throws UnsupportedEncodingException {
        Feature feature = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name
                  Scenario: scenario 1
                    Given first step
                  Scenario: scenario 2
                    Given first step
                  Scenario: scenario 3
                    Given first step
                """);

        StepDurationTimeService timeService = new StepDurationTimeService(
            Duration.ofSeconds(1),
            Duration.ofSeconds(2),
            Duration.ofSeconds(4));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new UsageFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", oneReference()),
                    new StubStepDefinition("second step", twoReference())))
                .build()
                .run();

        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualToNormalizingNewlines("""

                Expression/Text Duration   Mean ±  Error Location                                                  \s
                first step        7.000s 2.333s ± 1.440s io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()
                  first step      4.000s                 path/test.feature:6                                       \s
                  first step      2.000s                 path/test.feature:4                                       \s
                  first step      1.000s                 path/test.feature:2                                       \s
                second step                              io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()
                  UNUSED                                                                                           \s
                """);

    }
}
