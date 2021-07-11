package io.cucumber.core.plugin;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Hook;
import io.cucumber.messages.types.ParameterType;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestRunStarted;
import io.cucumber.messages.types.Timestamp;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

class HtmlFormatterTest {

    @Test
    void writes_index_html() throws Throwable {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        HtmlFormatter formatter = new HtmlFormatter(bytes);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        TestRunStarted testRunStarted = new TestRunStarted();
        testRunStarted.setTimestamp(new Timestamp(10L, 0L));
        Envelope testRunStartedEnvelope = new Envelope();
        testRunStartedEnvelope.setTestRunStarted(testRunStarted);
        bus.send(testRunStartedEnvelope);

        TestRunFinished testRunFinished = new TestRunFinished();
        testRunFinished.setTimestamp(new Timestamp(15L, 0L));
        Envelope testRunFinishedEnvelope = new Envelope();
        testRunFinishedEnvelope.setTestRunFinished(testRunFinished);
        bus.send(testRunFinishedEnvelope);

        String html = new String(bytes.toByteArray(), UTF_8);
        assertThat(html, containsString("" +
                "window.CUCUMBER_MESSAGES = [" +
                "{\"testRunStarted\":{\"timestamp\":{\"seconds\":10,\"nanos\":0}}}," +
                "{\"testRunFinished\":{\"timestamp\":{\"seconds\":15,\"nanos\":0}}}" +
                "];\n"));
    }

    @Test
    void ignores_step_definitions() throws Throwable {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        HtmlFormatter formatter = new HtmlFormatter(bytes);
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        formatter.setEventPublisher(bus);

        TestRunStarted testRunStarted = new TestRunStarted();
        testRunStarted.setTimestamp(new Timestamp(10L, 0L));
        Envelope testRunStartedEnvelope = new Envelope();
        testRunStartedEnvelope.setTestRunStarted(testRunStarted);
        bus.send(testRunStartedEnvelope);

        StepDefinition stepDefinition = new StepDefinition();
        Envelope stepDefinitionEnvelope = new Envelope();
        stepDefinitionEnvelope.setStepDefinition(stepDefinition);
        bus.send(stepDefinitionEnvelope);

        Hook hook = new Hook();
        Envelope hookEnvelope = new Envelope();
        hookEnvelope.setHook(hook);
        bus.send(hookEnvelope);

        ParameterType parameterType = new ParameterType();
        Envelope parameterTypeEnvelope = new Envelope();
        parameterTypeEnvelope.setParameterType(parameterType);
        bus.send(parameterTypeEnvelope);

        TestRunFinished testRunFinished = new TestRunFinished();
        testRunFinished.setTimestamp(new Timestamp(15L, 0L));
        Envelope testRunFinishedEnvelope = new Envelope();
        testRunFinishedEnvelope.setTestRunFinished(testRunFinished);
        bus.send(testRunFinishedEnvelope);

        String html = new String(bytes.toByteArray(), UTF_8);
        assertThat(html, containsString("" +
                "window.CUCUMBER_MESSAGES = [" +
                "{\"testRunStarted\":{\"timestamp\":{\"seconds\":10,\"nanos\":0}}}," +
                "{\"testRunFinished\":{\"timestamp\":{\"seconds\":15,\"nanos\":0}}}" +
                "];\n"));
    }

}
