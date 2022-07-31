package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestRunStarted;
import io.cucumber.messages.types.Timestamp;
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

        TestRunStarted testRunStarted = new TestRunStarted(new Timestamp(10L, 0L));
        bus.send(Envelope.of(testRunStarted));

        TestRunFinished testRunFinished = new TestRunFinished(null, true, new Timestamp(15L, 0L));
        bus.send(Envelope.of(testRunFinished));

        String ndjson = new String(bytes.toByteArray(), UTF_8);
        assertThat(ndjson, containsString("" +
                "{\"testRunStarted\":{\"timestamp\":{\"seconds\":10,\"nanos\":0}}}\n" +
                "{\"testRunFinished\":{\"success\":true,\"timestamp\":{\"seconds\":15,\"nanos\":0}}}\n"));
    }

}
