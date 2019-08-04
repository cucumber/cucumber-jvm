package io.cucumber.core.plugin;

import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.runner.ClockStub;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

//TODO: Merge with the existing test
public class JsonParallelRuntimeTest {

    @Test
    public void testSingleFeature() {
        StringBuilder parallel = new StringBuilder();

        Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse(
                        "--threads", "3",
                        "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature")
                    .build()
            )
            .withAdditionalPlugins(new JSONFormatter(parallel))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO)))
            .build()
            .run();

        StringBuilder serial = new StringBuilder();

        Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse(
                        "--threads", "1",
                        "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature")
                    .build()
            )
            .withAdditionalPlugins(new JSONFormatter(serial))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO)))
            .build()
            .run();

        assertThat(parallel.toString(), sameJSONAs(serial.toString()).allowingAnyArrayOrdering());
    }

    @Test
    public void testMultipleFeatures() {
        StringBuilder parallel = new StringBuilder();

        Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse("--threads", "3",
                        "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature",
                        "src/test/resources/io/cucumber/core/plugin/FormatterInParallel.feature")
                    .build()
            )
            .withAdditionalPlugins(new JSONFormatter(parallel))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO)))
            .build()
            .run();


        StringBuilder serial = new StringBuilder();

        Runtime.builder()
            .withRuntimeOptions(new CommandlineOptionsParser()
                .parse("--threads", "1",
                    "src/test/resources/io/cucumber/core/plugin/JSONPrettyFormatterTest.feature",
                    "src/test/resources/io/cucumber/core/plugin/FormatterInParallel.feature")
                .build())
            .withAdditionalPlugins(new JSONFormatter(serial))
            .withEventBus(new TimeServiceEventBus(new ClockStub(ZERO)))
            .build()
            .run();

        assertThat(parallel.toString(), sameJSONAs(serial.toString()).allowingAnyArrayOrdering());
    }

}
