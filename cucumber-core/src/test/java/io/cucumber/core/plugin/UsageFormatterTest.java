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
import java.time.Duration;
import java.util.UUID;

import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static org.assertj.core.api.Assertions.assertThat;

class UsageFormatterTest {

    @Test
    void writes_empty_report() throws UnsupportedEncodingException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n");

        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofMillis(1000));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new UsageFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier())
                .build()
                .run();

        assertThat(out.toString("UTF-8")).isEmpty();
    }

    @Test
    void writes_usage_report() throws UnsupportedEncodingException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario 1\n" +
                "    Given first step\n" +
                "  Scenario: scenario 2\n" +
                "    Given first step\n" +
                "  Scenario: scenario 3\n" +
                "    Given first step\n");

        StepDurationTimeService timeService = new StepDurationTimeService(
            Duration.ofMillis(1000),
            Duration.ofMillis(2000),
            Duration.ofMillis(4000));
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

        assertThat(out.toString("UTF-8")).isEqualToNormalizingNewlines("" +
                "\n" +
                "Expression/Text Duration   Mean ±  Error Location                                                   \n"
                +
                "first step        7.000s 2.333s ± 1.440s io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()\n"
                +
                "  first step      4.000s                 path/test.feature:6                                        \n"
                +
                "  first step      2.000s                 path/test.feature:4                                        \n"
                +
                "  first step      1.000s                 path/test.feature:2                                        \n"
                +
                "second step                              io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()\n"
                +
                "  UNUSED                                                                                            \n");

    }
}
