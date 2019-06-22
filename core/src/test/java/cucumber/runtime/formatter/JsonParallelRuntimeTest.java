package cucumber.runtime.formatter;

import cucumber.runner.TimeServiceEventBus;
import cucumber.runner.TimeServiceStub;
import io.cucumber.core.options.CommandlineOptionsParser;
import cucumber.runtime.Runtime;
import io.cucumber.core.options.RuntimeOptions;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;


//TODO: Merge with the existing test
public class JsonParallelRuntimeTest {

    @Test
    public void testSingleFeature() {
        StringBuilder parallel = new StringBuilder();

        Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse("src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature")
                    .build()
            )
            .withAdditionalPlugins(new JSONFormatter(parallel))
            .withEventBus(new TimeServiceEventBus(new TimeServiceStub(0)))
            .build()
            .run();

        StringBuilder serial = new StringBuilder();

        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();

        Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse("src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature")
                    .build(runtimeOptions)
            )
            .withAdditionalPlugins(new JSONFormatter(serial))
            .withEventBus(new TimeServiceEventBus(new TimeServiceStub(0)))
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
                    .parse("src/test/resources/cucumber/runtime/formatter/FormatterInParallel.feature")
                    .build()
            )
            .withAdditionalPlugins(new JSONFormatter(parallel))
            .withEventBus(new TimeServiceEventBus(new TimeServiceStub(0)))
            .build()
            .run();


        StringBuilder serial = new StringBuilder();

        Runtime.builder()
            .withRuntimeOptions(new CommandlineOptionsParser()
                .parse("src/test/resources/cucumber/runtime/formatter/FormatterInParallel.feature")
                .build())
            .withAdditionalPlugins(new JSONFormatter(serial))
            .withEventBus(new TimeServiceEventBus(new TimeServiceStub(0)))
            .build()
            .run();

        assertThat(parallel.toString(), sameJSONAs(serial.toString()).allowingAnyArrayOrdering());
    }
}
