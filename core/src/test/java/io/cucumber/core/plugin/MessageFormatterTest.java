package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.Messages;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class MessageFormatterTest {

    @Test
    void test() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MessageFormatter formatter = new MessageFormatter(bytes);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
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
                "{\"testRunStarted\":{\"timestamp\":{\"seconds\":\"10\"}}}\n" +
                "{\"testRunFinished\":{\"timestamp\":{\"seconds\":\"15\"}}}"));
    }

}
