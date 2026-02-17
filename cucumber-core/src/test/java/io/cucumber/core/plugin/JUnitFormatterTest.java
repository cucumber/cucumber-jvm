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

class JUnitFormatterTest {

    @Test
    void writes_report_xml() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        JUnitFormatter formatter = new JUnitFormatter(bytes);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        TestRunStarted testRunStarted = new TestRunStarted(new Timestamp(10L, 0), null);
        bus.send(Envelope.of(testRunStarted));

        TestRunFinished testRunFinished = new TestRunFinished(null, true, new Timestamp(15L, 0), null, null);
        bus.send(Envelope.of(testRunFinished));

        assertThat(bytes, bytes(equalTo(
            """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <testsuite name="Cucumber" time="5.0" tests="0" skipped="0" failures="0" errors="0" timestamp="1970-01-01T00:00:10Z">
                    </testsuite>
                    """)));
    }

}
