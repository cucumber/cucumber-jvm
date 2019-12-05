package io.cucumber.core.gherkin8;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.MessageFormatter;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.TimeServiceEventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class Test {

    public static void main(String[] args) throws FileNotFoundException {

        AtomicLong id = new AtomicLong();
        Supplier<UUID> idGenerator = () -> new UUID(0L, id.getAndIncrement());

        RuntimeOptions options = new RuntimeOptionsBuilder()
            .addGlue(GluePath.parse("io.cucumber.core.gherkin8"))
            .addFeature(FeatureWithLines.parse("classpath:io/cucumber/core/gherkin8"))
            .build();
        File file = new File("target/out.ndjson");
        Runtime runtime = Runtime.builder()
            .withAdditionalPlugins(new MessageFormatter(file))
            .withRuntimeOptions(options)
            .withEventBus(new TimeServiceEventBus(Clock.systemUTC(), idGenerator))
            .build();

        runtime.run();

        //TODO: Needs feature files

    }
}
