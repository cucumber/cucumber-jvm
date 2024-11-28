package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestRunStarted;
import io.cucumber.messages.types.Timestamp;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Clock;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

public class MessageFormatterTest {

    @Test
    void test() throws JSONException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MessageFormatter formatter = new MessageFormatter(bytes);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        TestRunStarted testRunStarted = new TestRunStarted(new Timestamp(10L, 0L));
        bus.send(Envelope.of(testRunStarted));

        TestRunFinished testRunFinished = new TestRunFinished(null, true, new Timestamp(15L, 0L), null);
        bus.send(Envelope.of(testRunFinished));

        String ndjson = new String(bytes.toByteArray(), UTF_8);
        String[] actual = ndjson.split("\\n");
        String[] expected = {
                "{\"testRunStarted\":{\"timestamp\":{\"seconds\":10,\"nanos\":0}}}",
                "{\"testRunFinished\":{\"success\":true,\"timestamp\":{\"seconds\":15,\"nanos\":0}}}"
        };
        assertThat(actual.length, equalTo(expected.length));
        for (int i = 0; i < actual.length; i++) {
            assertEquals(expected[i], actual[i], STRICT);
        }
    }

    @Test
    void testIOExceptionInWrite() {
        OutputStream failingOutputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Simulated IO exception");
            }
        };
        MessageFormatter formatter = new MessageFormatter(failingOutputStream);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        TestRunStarted testRunStarted = new TestRunStarted(new Timestamp(10L, 0L));
        Envelope envelope = Envelope.of(testRunStarted);

        assertThrows(IllegalStateException.class, () -> bus.send(envelope));
    }

    @Test
    void testIOExceptionInClose() {
        OutputStream outputStream = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                throw new IOException("Simulated IO exception");
            }
        };
        MessageFormatter formatter = new MessageFormatter(outputStream);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        TestRunFinished testRunFinished = new TestRunFinished(null, true, new Timestamp(15L, 0L), null);
        Envelope envelope = Envelope.of(testRunFinished);

        assertThrows(IllegalStateException.class, () -> bus.send(envelope));
    }
}
