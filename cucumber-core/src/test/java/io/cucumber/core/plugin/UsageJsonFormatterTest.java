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
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.Duration;
import java.util.UUID;

import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.oneReference;
import static io.cucumber.core.plugin.PrettyFormatterStepDefinition.twoReference;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class UsageJsonFormatterTest {

    @Test
    void writes_empty_report() throws JSONException {
        Feature feature = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name
                """);

        StepDurationTimeService timeService = new StepDurationTimeService(Duration.ofSeconds(1));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withEventBus(new TimeServiceEventBus(timeService, UUID::randomUUID))
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new UsageJsonFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier())
                .build()
                .run();

        assertJsonEquals("{\"stepDefinitions\":[]}", out);
    }

    @Test
    void writes_usage_report() throws JSONException {
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
                .withAdditionalPlugins(timeService, new UsageJsonFormatter(out))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setMonochrome().build())
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step", oneReference()),
                    new StubStepDefinition("second step", twoReference())))
                .build()
                .run();

        String featureFile = new File("").toPath().toUri() + "path/test.feature";
        String expected = """
                {
                  "stepDefinitions": [
                    {
                      "sourceReference": {
                        "javaMethod": {
                          "className": "io.cucumber.core.plugin.PrettyFormatterStepDefinition",
                          "methodName": "one",
                          "methodParameterTypes": []
                        }
                      },
                      "duration": {
                        "sum": {
                          "seconds": 7,
                          "nanos": 0
                        },
                        "mean": {
                          "seconds": 2,
                          "nanos": 333333333
                        },
                        "moe95": {
                          "seconds": 1,
                          "nanos": 440164599
                        }
                      },
                      "matches": [
                        {
                          "text": "first step",
                          "duration": {
                            "seconds": 1,
                            "nanos": 0
                          },
                          "uri": "path/test.feature",
                          "location": {
                            "line": 2,
                            "column": 3
                          }
                        },
                        {
                          "text": "first step",
                          "duration": {
                            "seconds": 2,
                            "nanos": 0
                          },
                          "uri": "path/test.feature",
                          "location": {
                            "line": 4,
                            "column": 3
                          }
                        },
                        {
                          "text": "first step",
                          "duration": {
                            "seconds": 4,
                            "nanos": 0
                          },
                          "uri": "path/test.feature",
                          "location": {
                            "line": 6,
                            "column": 3
                          }
                        }
                      ],
                      "expression": {
                        "source": "first step",
                        "type": "CUCUMBER_EXPRESSION"
                      }
                    },
                    {
                      "sourceReference": {
                        "javaMethod": {
                          "className": "io.cucumber.core.plugin.PrettyFormatterStepDefinition",
                          "methodName": "two",
                          "methodParameterTypes": []
                        }
                      },
                      "matches": [],
                      "expression": {
                        "source": "second step",
                        "type": "CUCUMBER_EXPRESSION"
                      }
                    }
                  ]
                }""";
        assertJsonEquals(expected.replaceAll("path/test.feature", featureFile), out);
    }

    private void assertJsonEquals(String expected, ByteArrayOutputStream actual) throws JSONException {
        assertEquals(expected, actual.toString(UTF_8), true);
    }

}
