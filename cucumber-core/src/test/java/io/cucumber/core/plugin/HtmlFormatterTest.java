package io.cucumber.core.plugin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
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
        List<LinkedHashMap<String, Object>> actual = rectifiedRepresentation(getWindowCucumberMessagesFromBytes(bytes));
        List<LinkedHashMap<String, Object>> expected = rectifiedRepresentation("" +
                "window.CUCUMBER_MESSAGES = [" +
                "{\"testRunStarted\":{\"timestamp\":{\"seconds\":10,\"nanos\":0}}}," +
                "{\"testRunFinished\":{\"success\":true,\"timestamp\":{\"seconds\":15,\"nanos\":0}}}" +
                "];\n");

        assertThat(actual, equalTo(expected));
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
        List<LinkedHashMap<String, Object>> expected = rectifiedRepresentation("" +
                "window.CUCUMBER_MESSAGES = [" +
                "{\"testRunStarted\":{\"timestamp\":{\"seconds\":10,\"nanos\":0}}}," +
                "{\"testRunFinished\":{\"success\":true,\"timestamp\":{\"seconds\":15,\"nanos\":0}}}" +
                "];\n");
        List<LinkedHashMap<String, Object>> actual = rectifiedRepresentation(getWindowCucumberMessagesFromBytes(bytes));
        assertThat(actual, equalTo(expected));
    }

    private String getWindowCucumberMessagesFromBytes(ByteArrayOutputStream bytes) {
        String result = bytes.toString(StandardCharsets.UTF_8);
        Pattern scriptTag = Pattern.compile("<script>\n(.*?)</script>", Pattern.DOTALL);
        Matcher contentMatcher = scriptTag.matcher(result);
        String output = null;
        while (contentMatcher.find()) {
            if (contentMatcher.group(1).contains("window.CUCUMBER_MESSAGES = ")) {
                output = contentMatcher.group(1);
            }
        }
        if (output == null) {
            Assertions.fail();
        }
        return output;
    }

    private List<LinkedHashMap<String, Object>> rectifiedRepresentation(String inputContainingListOfObjects) {
        Pattern pattern = Pattern.compile("\\[.*?]");
        Matcher matcher = pattern.matcher(inputContainingListOfObjects);
        String parsedInput = null;
        while (matcher.find()) {
            parsedInput = matcher.group();
        }
        if (parsedInput == null) {
            Assertions.fail();
            return null;
        }
        List<LinkedHashMap<String, Object>> output = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            output = objectMapper.readValue(parsedInput, new TypeReference<List<LinkedHashMap<String, Object>>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
        return output;
    }

}
