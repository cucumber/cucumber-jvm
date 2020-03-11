package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.Messages;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

class HTMLFormatterTest {

    @Test
    void writes_index_html() throws Throwable {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        HTMLFormatter formatter = new HTMLFormatter(bytes);
        EventBus bus = new TimeServiceEventBus(Clock.fixed(Instant.now(), ZoneId.of("UTC")), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        bus.send(Messages.Envelope.newBuilder()
            .setTestRunStarted(Messages.TestRunStarted.newBuilder()
                .setTimestamp(Messages.Timestamp.newBuilder()
                    .setSeconds(10)
                    .build())
                .build())
            .build());

        bus.send(
            Messages.Envelope.newBuilder()
                .setTestRunFinished(Messages.TestRunFinished.newBuilder()
                    .setTimestamp(Messages.Timestamp.newBuilder()
                        .setSeconds(15)
                        .build())
                    .build())
                .build());

        String html = new String(bytes.toByteArray(), UTF_8);
        assertThat(html, containsString("" +
            "window.CUCUMBER_MESSAGES = [" +
            "{\"testRunStarted\":{\"timestamp\":{\"seconds\":\"10\"}}}," +
            "{\"testRunFinished\":{\"timestamp\":{\"seconds\":\"15\"}}}" +
            "];"));
    }


}
