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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

class HtmlFormatterTest {

    @Test
    void writes_index_html() throws Throwable {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        HtmlFormatter formatter = new HtmlFormatter(bytes);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        TestRunStarted testRunStarted = new TestRunStarted(new Timestamp(10L, 0L), null);
        bus.send(Envelope.of(testRunStarted));

        TestRunFinished testRunFinished = new TestRunFinished(null, true, new Timestamp(15L, 0L), null, null);
        bus.send(Envelope.of(testRunFinished));

        assertEquals("[" +
                "{\"testRunStarted\":{\"timestamp\":{\"nanos\":0,\"seconds\":10}}}," +
                "{\"testRunFinished\":{\"success\":true,\"timestamp\":{\"nanos\":0,\"seconds\":15}}}" +
                "]",
            extractCucumberMessages(bytes), STRICT);
    }

    private static String extractCucumberMessages(ByteArrayOutputStream bytes) {
        Pattern pattern = Pattern.compile("^.*window\\.CUCUMBER_MESSAGES = (\\[.+]);.*$", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(new String(bytes.toByteArray(), UTF_8));
        assertThat("bytes must match " + pattern, matcher.find());
        return matcher.group(1);
    }
}
