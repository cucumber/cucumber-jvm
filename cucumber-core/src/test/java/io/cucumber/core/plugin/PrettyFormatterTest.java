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

import static io.cucumber.core.plugin.Bytes.bytes;
import static io.cucumber.core.plugin.IsEqualCompressingLineSeparators.equalCompressingLineSeparators;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.threeReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static org.hamcrest.MatcherAssert.assertThat;

class PrettyFormatterTest {

    @Test
    void writes_pretty_report() {
        Feature feature = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name
                  Scenario: scenario name
                    Given first step
                    When second step
                    Then third step
                """);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new PrettyFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", oneReference()),
                    new StubStepDefinition("second step", twoReference()),
                    new StubStepDefinition("third step", threeReference())))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("""

                Scenario: scenario name # path/test.feature:2
                  ✔ Given first step    # io.cucumber.core.plugin.PrettyFormatterStepDefinition.one()
                  ✔ When second step    # io.cucumber.core.plugin.PrettyFormatterStepDefinition.two()
                  ✔ Then third step     # io.cucumber.core.plugin.PrettyFormatterStepDefinition.three()""")));
    }
}
