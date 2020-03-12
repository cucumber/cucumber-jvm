package io.cucumber.core.plugin;

import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.runner.ClockStub;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

//TODO: Merge with the existing test
class JsonParallelRuntimeTest {

    @Test
    void testSingleFeature() throws IOException {
        final ByteArrayOutputStream parallelOut = new ByteArrayOutputStream();

        Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse(
                        "--threads", "3",
                        "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature")
                    .build()
            )
            .withAdditionalPlugins(new JSONFormatter(parallelOut))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
            .build()
            .run();

        final ByteArrayOutputStream serialOut = new ByteArrayOutputStream();

        Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse(
                        "--threads", "1",
                        "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature")
                    .build()
            )
            .withAdditionalPlugins(new JSONFormatter(serialOut))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
            .build()
            .run();

        String actual = new String(parallelOut.toByteArray(), UTF_8);
        String expected = new String(serialOut.toByteArray(), UTF_8);
        assertThat(actual, sameJSONAs(expected).allowingAnyArrayOrdering());
    }

    @Test
    void testMultipleFeatures() throws IOException {
        final ByteArrayOutputStream parallelOut = new ByteArrayOutputStream();

        Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse("--threads", "3",
                        "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature",
                        "src/test/resources/io/cucumber/core/plugin/FormatterInParallel.feature")
                    .build()
            )
            .withAdditionalPlugins(new JSONFormatter(parallelOut))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
            .build()
            .run();


        final ByteArrayOutputStream serialOut = new ByteArrayOutputStream();

        Runtime.builder()
            .withRuntimeOptions(new CommandlineOptionsParser()
                .parse("--threads", "1",
                    "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature",
                    "src/test/resources/io/cucumber/core/plugin/FormatterInParallel.feature")
                .build())
            .withAdditionalPlugins(new JSONFormatter(serialOut))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO), UUID::randomUUID))
            .build()
            .run();

        String actual = new String(parallelOut.toByteArray(), UTF_8);
        String expected = new String(serialOut.toByteArray(), UTF_8);
        assertThat(actual, sameJSONAs(expected).allowingAnyArrayOrdering());    }

}
