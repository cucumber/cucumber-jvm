package io.cucumber.core.plugin;

import io.cucumber.core.backend.SourceReference;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofMillis;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class JsonFormatterTest {

    final SourceReference thereAreBananas = getMethod("there_are_bananas");

    private static SourceReference getMethod(String name) {
        try {
            return SourceReference.fromMethod(JsonFormatterTestStepDefinitions.class.getMethod(name));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertJsonEquals(String expected, ByteArrayOutputStream actual) throws JSONException {
        assertEquals(expected, new String(actual.toByteArray(), UTF_8), true);
    }

    @Test
    void should_format_scenario_with_a_passed_step() throws JSONException {
        Feature feature = TestFeatureParser.parse("path/test.feature", """
                Feature: Banana party

                  Scenario: Monkey eats bananas
                    Given there are bananas
                """);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StepDurationTimeService timeService = new StepDurationTimeService(ofMillis(1));
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(timeService, new JsonFormatter(out))
                .withEventBus(new TimeServiceEventBus(timeService, new IncrementingUuidGenerator()))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("there are bananas", thereAreBananas)))
                .build()
                .run();

        String expected = """
                [
                  {
                    "id": "banana-party",
                    "uri": "file:path/test.feature",
                    "keyword": "Feature",
                    "name": "Banana party",
                    "line": 1,
                    "description": "",
                    "elements": [
                      {
                        "id": "banana-party;monkey-eats-bananas",
                        "keyword": "Scenario",
                        "start_timestamp": "1970-01-01T00:00:00.000Z",
                        "name": "Monkey eats bananas",
                        "line": 3,
                        "description": "",
                        "type": "scenario",
                        "steps": [
                          {
                            "keyword": "Given ",
                            "name": "there are bananas",
                            "line": 4,
                            "match": {
                              "location": "io.cucumber.core.plugin.JsonFormatterTestStepDefinitions.there_are_bananas()"
                            },
                            "result": {
                              "status": "passed",
                              "duration": 1000000
                            }
                          }
                        ]
                      }
                    ],
                    "tags": []
                  }
                ]""";
        assertJsonEquals(expected, out);
    }
}
