package io.cucumber.core.plugin;

import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.runner.ClockStub;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

class JsonParallelRuntimeTest {

    @Test
    void testSingleFeature() {
        ByteArrayOutputStream parallel = new ByteArrayOutputStream();

        Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse(
                        "--threads", "3",
                        "src/test/resources/io/cucumber/core/plugin/JsonPrettyFormatterTest.feature")
                    .build()
            )
            .withAdditionalPlugins(new JsonFormatter(parallel))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
            .build()
            .run();

        ByteArrayOutputStream serial = new ByteArrayOutputStream();

        Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse(
                        "--threads", "1",
                        "src/test/resources/io/cucumber/core/plugin/JsonPrettyFormatterTest.feature")
                    .build()
            )
            .withAdditionalPlugins(new JsonFormatter(serial))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
            .build()
            .run();

        assertThat(parallel.toString(), sameJSONAs(serial.toString()).allowingAnyArrayOrdering());
    }

    @Test
    void testMultipleFeatures() {
        ByteArrayOutputStream parallel = new ByteArrayOutputStream();

        Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse("--threads", "3",
                        "src/test/resources/io/cucumber/core/plugin/JsonPrettyFormatterTest.feature",
                        "src/test/resources/io/cucumber/core/plugin/FormatterInParallel.feature")
                    .build()
            )
            .withAdditionalPlugins(new JsonFormatter(parallel))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
            .build()
            .run();

        ByteArrayOutputStream serial = new ByteArrayOutputStream();

        Runtime.builder()
            .withRuntimeOptions(new CommandlineOptionsParser()
                .parse("--threads", "1",
                    "src/test/resources/io/cucumber/core/plugin/JsonPrettyFormatterTest.feature",
                    "src/test/resources/io/cucumber/core/plugin/FormatterInParallel.feature")
                .build())
            .withAdditionalPlugins(new JsonFormatter(serial))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
            .build()
            .run();

        assertThat(parallel.toString(), sameJSONAs(serial.toString()).allowingAnyArrayOrdering());
    }

}
