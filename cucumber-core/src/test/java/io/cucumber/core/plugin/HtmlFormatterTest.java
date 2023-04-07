package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Hook;
import io.cucumber.messages.types.ParameterType;
import io.cucumber.messages.types.SourceReference;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.StepDefinitionPattern;
import io.cucumber.messages.types.StepDefinitionPatternType;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestRunStarted;
import io.cucumber.messages.types.Timestamp;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.util.Collections;
import java.util.UUID;

import static io.cucumber.core.plugin.Bytes.bytes;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

class HtmlFormatterTest {

    @Test
    void writes_index_html() throws Throwable {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        HtmlFormatter formatter = new HtmlFormatter(bytes);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        TestRunStarted testRunStarted = new TestRunStarted(new Timestamp(10L, 0L));
        bus.send(Envelope.of(testRunStarted));

        TestRunFinished testRunFinished = new TestRunFinished(null, true, new Timestamp(15L, 0L), null);
        bus.send(Envelope.of(testRunFinished));

        assertThat(bytes, bytes(containsString("" +
                "window.CUCUMBER_MESSAGES = [" +
                "{\"testRunStarted\":{\"timestamp\":{\"seconds\":10,\"nanos\":0}}}," +
                "{\"testRunFinished\":{\"success\":true,\"timestamp\":{\"seconds\":15,\"nanos\":0}}}" +
                "];\n")));
    }

    @Test
    void ignores_step_definitions() throws Throwable {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        HtmlFormatter formatter = new HtmlFormatter(bytes);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        TestRunStarted testRunStarted = new TestRunStarted(new Timestamp(10L, 0L));
        bus.send(Envelope.of(testRunStarted));

        StepDefinition stepDefinition = new StepDefinition(
            "",
            new StepDefinitionPattern("", StepDefinitionPatternType.CUCUMBER_EXPRESSION),
            SourceReference.of("https://example.com"));
        bus.send(Envelope.of(stepDefinition));

        Hook hook = new Hook("",
            null,
            SourceReference.of("https://example.com"),
            null);
        bus.send(Envelope.of(hook));

        // public ParameterType(String name, List<String> regularExpressions,
        // Boolean preferForRegularExpressionMatch, Boolean useForSnippets,
        // String id) {
        ParameterType parameterType = new ParameterType(
            "",
            Collections.emptyList(),
            true,
            false,
            "",
            null);
        bus.send(Envelope.of(parameterType));

        TestRunFinished testRunFinished = new TestRunFinished(
            null,
            true,
            new Timestamp(15L, 0L),
            null);
        bus.send(Envelope.of(testRunFinished));

        assertThat(bytes, bytes(containsString("" +
                "window.CUCUMBER_MESSAGES = [" +
                "{\"testRunStarted\":{\"timestamp\":{\"seconds\":10,\"nanos\":0}}}," +
                "{\"testRunFinished\":{\"success\":true,\"timestamp\":{\"seconds\":15,\"nanos\":0}}}" +
                "];\n")));
    }

}
