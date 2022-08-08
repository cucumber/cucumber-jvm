package io.cucumber.core.plugin;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runner.ClockStub;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static java.time.Duration.ZERO;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class JsonParallelRuntimeTest {

    @Test
    void testSingleFeature() throws JSONException {
        ByteArrayOutputStream parallel = new ByteArrayOutputStream();

        Runtime.builder()
                .withRuntimeOptions(
                    new RuntimeOptionsBuilder()
                            .setThreads(3)
                            .addFeature(FeatureWithLines.parse(
                                "src/test/resources/io/cucumber/core/plugin/JsonPrettyFormatterTest.feature"))
                            .build())
                .withAdditionalPlugins(new JsonFormatter(parallel))
                .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
                .build()
                .run();

        ByteArrayOutputStream serial = new ByteArrayOutputStream();

        Runtime.builder()
                .withRuntimeOptions(
                    new RuntimeOptionsBuilder()
                            .setThreads(1)
                            .addFeature(FeatureWithLines.parse(
                                "src/test/resources/io/cucumber/core/plugin/JsonPrettyFormatterTest.feature"))
                            .build())
                .withAdditionalPlugins(new JsonFormatter(serial))
                .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
                .build()
                .run();

        assertEquals(serial.toString(), parallel.toString(), false);
    }

    @Test
    void testMultipleFeatures() throws JSONException {
        ByteArrayOutputStream parallel = new ByteArrayOutputStream();

        Runtime.builder()
                .withRuntimeOptions(
                    new RuntimeOptionsBuilder()
                            .setThreads(3)
                            .addFeature(FeatureWithLines.parse(
                                "src/test/resources/io/cucumber/core/plugin/JsonPrettyFormatterTest.feature"))
                            .addFeature(FeatureWithLines
                                    .parse("src/test/resources/io/cucumber/core/plugin/FormatterInParallel.feature"))
                            .build())
                .withAdditionalPlugins(new JsonFormatter(parallel))
                .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
                .build()
                .run();

        ByteArrayOutputStream serial = new ByteArrayOutputStream();

        Runtime.builder()
                .withRuntimeOptions(
                    new RuntimeOptionsBuilder()
                            .setThreads(1)
                            .addFeature(FeatureWithLines.parse(
                                "src/test/resources/io/cucumber/core/plugin/JsonPrettyFormatterTest.feature"))
                            .addFeature(FeatureWithLines
                                    .parse("src/test/resources/io/cucumber/core/plugin/FormatterInParallel.feature"))
                            .build())
                .withAdditionalPlugins(new JsonFormatter(serial))
                .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
                .build()
                .run();

        assertEquals(serial.toString(), parallel.toString(), false);
    }

}
