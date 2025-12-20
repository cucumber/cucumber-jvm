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
import java.nio.charset.StandardCharsets;

import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static org.assertj.core.api.Assertions.assertThat;

class UnusedStepsSummaryPrinterTest {

    @Test
    void writes_unused_report() throws UnsupportedEncodingException {
        Feature feature = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name
                  Scenario: scenario name
                    Given first step
                """);

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

        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualToNormalizingNewlines("""

                1 unused step definition(s)

                Location                                                    Expression  \s
                io.cucumber.core.plugin.PrettyFormatterStepDefinition.two() # second step
                """);
    }
}
