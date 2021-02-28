package io.cucumber.core.runner;

import io.cucumber.core.backend.Status;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.Attachment;
import io.cucumber.messages.Messages.Attachment.ContentEncoding;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.WriteEvent;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.max;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

class TestCaseState implements io.cucumber.core.backend.TestCaseState {

    private final List<Result> stepResults = new ArrayList<>();
    private final EventBus bus;
    private final TestCase testCase;
    private final UUID testExecutionId;

    private UUID currentTestStepId;

    TestCaseState(EventBus bus, UUID testExecutionId, TestCase testCase) {
        this.bus = requireNonNull(bus);
        this.testExecutionId = requireNonNull(testExecutionId);
        this.testCase = requireNonNull(testCase);
    }

    void add(Result result) {
        stepResults.add(result);
    }

    UUID getTestExecutionId() {
        return testExecutionId;
    }

    @Override
    public Collection<String> getSourceTagNames() {
        return testCase.getTags();
    }

    @Override
    public Status getStatus() {
        if (stepResults.isEmpty()) {
            return Status.PASSED;
        }

        Result mostSevereResult = max(stepResults, comparing(Result::getStatus));
        return Status.valueOf(mostSevereResult.getStatus().name());
    }

    @Override
    public boolean isFailed() {
        return getStatus() == Status.FAILED;
    }

    @Override
    public void attach(byte[] data, String mediaType, String name) {
        requireNonNull(data);
        requireNonNull(mediaType);

        requireActiveTestStep();
        bus.send(new EmbedEvent(bus.getInstant(), testCase, data, mediaType, name));
        Attachment.Builder attachment = createAttachment()
                .setBody(Base64.getEncoder().encodeToString(data))
                .setContentEncoding(ContentEncoding.BASE64)
                .setMediaType(mediaType);
        if (name != null) {
            attachment.setFileName(name);
        }
        bus.send(Messages.Envelope.newBuilder()
                .setAttachment(attachment)
                .build());
    }

    @Override
    public void attach(String data, String mediaType, String name) {
        requireNonNull(data);
        requireNonNull(mediaType);

        requireActiveTestStep();
        bus.send(new EmbedEvent(bus.getInstant(), testCase, data.getBytes(UTF_8), mediaType, name));
        Attachment.Builder attachment = createAttachment()
                .setBody(data)
                .setContentEncoding(ContentEncoding.IDENTITY)
                .setMediaType(mediaType);
        if (name != null) {
            attachment.setFileName(name);
        }
        bus.send(Messages.Envelope.newBuilder()
                .setAttachment(attachment)
                .build());
    }

    @Override
    public void log(String text) {
        requireActiveTestStep();
        bus.send(new WriteEvent(bus.getInstant(), testCase, text));
        Attachment.Builder attachment = createAttachment()
                .setBody(text)
                .setContentEncoding(ContentEncoding.IDENTITY)
                .setMediaType("text/x.cucumber.log+plain");
        bus.send(Messages.Envelope.newBuilder()
                .setAttachment(attachment)
                .build());
    }

    private Attachment.Builder createAttachment() {
        return Attachment.newBuilder()
                .setTestCaseStartedId(testExecutionId.toString())
                .setTestStepId(currentTestStepId.toString());
    }

    @Override
    public String getName() {
        return testCase.getName();
    }

    @Override
    public String getId() {
        return testCase.getUri() + ":" + getLine();
    }

    @Override
    public URI getUri() {
        return testCase.getUri();
    }

    @Override
    public Integer getLine() {
        return testCase.getLocation().getLine();
    }

    Throwable getError() {
        if (stepResults.isEmpty()) {
            return null;
        }

        return max(stepResults, comparing(Result::getStatus)).getError();
    }

    void setCurrentTestStepId(UUID currentTestStepId) {
        this.currentTestStepId = currentTestStepId;
    }

    void clearCurrentTestStepId() {
        this.currentTestStepId = null;
    }

    private void requireActiveTestStep() {
        if (currentTestStepId == null) {
            throw new IllegalStateException(
                "You can not use Scenario.log or Scenario.attach when a step is not being executed");
        }
    }

}
