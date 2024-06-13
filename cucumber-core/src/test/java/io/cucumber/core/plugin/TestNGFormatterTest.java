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

import static io.cucumber.core.plugin.Bytes.bytes;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class TestNGFormatterTest {

    @Test
    void writes_report_xml() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        TestNGFormatter formatter = new TestNGFormatter(bytes);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        TestRunStarted testRunStarted = new TestRunStarted(new Timestamp(10L, 0L));
        bus.send(Envelope.of(testRunStarted));

        TestRunFinished testRunFinished = new TestRunFinished(null, true, new Timestamp(15L, 0L), null);
        bus.send(Envelope.of(testRunFinished));

        assertThat(bytes, bytes(equalTo("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testng-results failed=\"0\" passed=\"0\" skipped=\"0\" total=\"0\">\n" +
                "<suite name=\"Cucumber\" duration-ms=\"5000\">\n" +
                "<test name=\"Cucumber\" duration-ms=\"5000\">\n" +
                "</test>\n" +
                "</suite>\n" +
                "</testng-results>\n")));
    }

}
