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

        TestRunStarted testRunStarted = new TestRunStarted(new Timestamp(10L, 0L));
        bus.send(Envelope.of(testRunStarted));

        TestRunFinished testRunFinished = new TestRunFinished(null, true, new Timestamp(15L, 0L), null);
        bus.send(Envelope.of(testRunFinished));

        assertEquals("[" +
                "{\"testRunStarted\":{\"timestamp\":{\"nanos\":0,\"seconds\":10}}}," +
                "{\"testRunFinished\":{\"success\":true,\"timestamp\":{\"nanos\":0,\"seconds\":15}}}" +
                "]",
            extractCucumberMessages(bytes), STRICT);
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
