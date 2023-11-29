package io.cucumber.core.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestRunStarted;
import io.cucumber.messages.types.Timestamp;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

public class MessageFormatterTest {

    @Test
    void test() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MessageFormatter formatter = new MessageFormatter(bytes);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        TestRunStarted testRunStarted = new TestRunStarted(new Timestamp(10L, 0L));
        bus.send(Envelope.of(testRunStarted));

        TestRunFinished testRunFinished = new TestRunFinished(null, true, new Timestamp(15L, 0L), null);
        bus.send(Envelope.of(testRunFinished));

        String ndjson = new String(bytes.toByteArray(), UTF_8);
        String expectedJson = "{\"testRunStarted\":{\"timestamp\":{\"seconds\":10,\"nanos\":0}},\"testRunFinished\":{\"success\":true,\"timestamp\":{\"seconds\":15,\"nanos\":0}}}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> expectedMap = objectMapper.readValue(expectedJson, Map.class);

            String[] ndjsonObjects = ndjson.split("\\n");
            Map<String, Object> ndjsonMap = new HashMap<>();
            for (String ndjsonObject : ndjsonObjects) {
                ndjsonMap.putAll(objectMapper.readValue(ndjsonObject, Map.class));
            }

            assertThat(expectedMap.entrySet(), everyItem(is(in(ndjsonMap.entrySet()))));
        } catch (IOException e) {
            fail("An exception occurred while processing JSON: " + e.getMessage());
        }
    }

}
