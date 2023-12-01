package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestRunStarted;
import io.cucumber.messages.types.Timestamp;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

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
        String[] ndjsonArray = ndjson.split("\\n");
        String[] expectedArray = {
                "{\"testRunStarted\":{\"timestamp\":{\"seconds\":10,\"nanos\":0}}}",
                "{\"testRunFinished\":{\"success\":true,\"timestamp\":{\"seconds\":15,\"nanos\":0}}}"
        };
        for (int i = 0; i < ndjsonArray.length; i++) {
            JSONAssert.assertEquals(expectedArray[i], ndjsonArray[i], JSONCompareMode.LENIENT);
        }
    }

}
