package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestRunStarted;
import io.cucumber.messages.types.Timestamp;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static io.cucumber.core.plugin.BytesEqualTo.isBytesEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;

class NoPublishFormatterTest {
    @Test
    public void should_print_banner() throws UnsupportedEncodingException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, false, StandardCharsets.UTF_8.name());
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

        NoPublishFormatter noPublishFormatter = new NoPublishFormatter(out);
        noPublishFormatter.setMonochrome(true);
        noPublishFormatter.setEventPublisher(bus);

        bus.send(Envelope.of(new TestRunStarted(new Timestamp(0L, 0L))));
        bus.send(Envelope.of(new TestRunFinished(null, true, new Timestamp(0L, 0L))));

        assertThat(bytes, isBytesEqualTo("" +
                "┌───────────────────────────────────────────────────────────────────────────────────┐\n" +
                "│ Share your Cucumber Report with your team at https://reports.cucumber.io          │\n" +
                "│ Activate publishing with one of the following:                                    │\n" +
                "│                                                                                   │\n" +
                "│ src/test/resources/cucumber.properties:          cucumber.publish.enabled=true    │\n" +
                "│ src/test/resources/junit-platform.properties:    cucumber.publish.enabled=true    │\n" +
                "│ Environment variable:                            CUCUMBER_PUBLISH_ENABLED=true    │\n" +
                "│ JUnit:                                           @CucumberOptions(publish = true) │\n" +
                "│                                                                                   │\n" +
                "│ More information at https://cucumber.io/docs/cucumber/environment-variables/      │\n" +
                "│                                                                                   │\n" +
                "│ Disable this message with one of the following:                                   │\n" +
                "│                                                                                   │\n" +
                "│ src/test/resources/cucumber.properties:          cucumber.publish.quiet=true      │\n" +
                "│ src/test/resources/junit-platform.properties:    cucumber.publish.quiet=true      │\n" +
                "└───────────────────────────────────────────────────────────────────────────────────┘\n"));
    }

}
