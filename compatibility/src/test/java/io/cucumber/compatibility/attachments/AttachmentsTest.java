package io.cucumber.compatibility.attachments;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.MessageFormatter;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.time.Clock.fixed;
import static java.time.Instant.ofEpochSecond;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttachmentsTest {

    private final AtomicLong id = new AtomicLong();
    private final Supplier<UUID> idGenerator = () -> new UUID(0L, id.getAndIncrement());

    @TempDir
    File temp;

    @Test
    @Disabled
    void test() throws IOException {
        File output = new File(temp, "out.ndjson");

        Runtime.builder()
            .withRuntimeOptions(new RuntimeOptionsBuilder()
                .addGlue(GluePath.parse("io.cucumber.compatibility.attachments"))
                .addFeature(FeatureWithLines.parse("classpath:io/cucumber/compatibility/attachments"))
                .build())
            .withAdditionalPlugins(new MessageFormatter(output))
            .withEventBus(new TimeServiceEventBus(fixed(ofEpochSecond(-1815350400), UTC), idGenerator))
            .build()
            .run();

        Path expectedFile = Paths.get("src/test/resources/io/cucumber/compatibility/attachments/attachments.ndjson");
        assertEquals(
            new String(readAllBytes(expectedFile), UTF_8),
            new String(readAllBytes(output.toPath()), UTF_8)
        );
    }
}
