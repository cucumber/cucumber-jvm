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

        TestRunStarted testRunStarted = new TestRunStarted();
        testRunStarted.setTimestamp(new Timestamp(10L, 0L));
        Envelope testRunStartedEnvelope = new Envelope();
        testRunStartedEnvelope.setTestRunStarted(testRunStarted);
        bus.send(testRunStartedEnvelope);

        TestRunFinished testRunFinished = new TestRunFinished();
        testRunFinished.setTimestamp(new Timestamp(15L, 0L));
        Envelope testRunFinishedEnvelope = new Envelope();
        testRunFinishedEnvelope.setTestRunFinished(testRunFinished);
        bus.send(testRunFinishedEnvelope);

        String ndjson = new String(bytes.toByteArray(), UTF_8);
        assertThat(ndjson, containsString("" +
                "{\"testRunStarted\":{\"timestamp\":{\"seconds\":10,\"nanos\":0}}}\n" +
                "{\"testRunFinished\":{\"timestamp\":{\"seconds\":15,\"nanos\":0}}}\n"));
    }

}
