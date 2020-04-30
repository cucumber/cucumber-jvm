package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.TestRunFinished;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.Clock;
import java.time.ZoneId;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace;

class DefaultSummaryPrinterTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final DefaultSummaryPrinter summaryPrinter = new DefaultSummaryPrinter(out);
    private final EventBus bus = new TimeServiceEventBus(
        Clock.fixed(ofEpochSecond(0), ZoneId.of("UTC")),
        UUID::randomUUID
    );

    @BeforeEach
    void setup() {
        summaryPrinter.setEventPublisher(bus);
    }

    @Test
    void does_not_print_duplicate_snippets() {
        bus.send(new SnippetsSuggestedEvent(
            bus.getInstant(),
            URI.create("classpath:com/example.feature"),
            12,
            13,
            singletonList("snippet")
        ));

        bus.send(new SnippetsSuggestedEvent(
            bus.getInstant(),
            URI.create("classpath:com/example.feature"),
            12,
            14,
            singletonList("snippet")
        ));

        bus.send(new TestRunFinished(
            bus.getInstant()
        ));

        assertThat(new String(out.toByteArray(), UTF_8), equalToCompressingWhiteSpace("" +
            "\n" +
            "0 Scenarios\n" +
            "0 Steps\n" +
            "0m0.000s\n" +
            "\n" +
            "\n" +
            "You can implement missing steps with the snippets below:\n" +
            "\n" +
            "snippet\n" +
            "\n" +
            "\n"
        ));

    }

}