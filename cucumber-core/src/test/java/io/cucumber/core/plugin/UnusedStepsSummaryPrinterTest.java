package io.cucumber.core.plugin;

import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static org.assertj.core.api.Assertions.assertThat;

class UnusedStepsSummaryPrinterTest {

    @Test
    void writes_unused_report() throws UnsupportedEncodingException {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new UnusedStepsSummaryPrinter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", oneReference()),
                    new StubStepDefinition("second step", twoReference())))
                .build()
                .run();

        assertThat(out.toString("UTF-8")).isEqualToNormalizingNewlines("\n" +
                "1 unused step definition(s):\n" +
                "io.cucumber.core.plugin.PrettyFormatterStepDefinition.two() # second step\n");
    }
}
