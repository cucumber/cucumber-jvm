package io.cucumber.core.runner;

import io.cucumber.core.backend.Status;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.messages.Messages;
import io.cucumber.messages.internal.com.google.protobuf.ByteString;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.WriteEvent;

import java.net.URI;
import java.util.ArrayList;
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

    @Deprecated
    @Override
    public void embed(byte[] data, String mediaType) {
        embed(data, mediaType, null);
    }

    @Override
    public void embed(byte[] data, String mediaType, String name) {
        bus.send(new EmbedEvent(bus.getInstant(), testCase, data, mediaType, name));

        // TODO: Remove this weird exception from the cck
        if (mediaType.equals("text/plain")) {
            bus.send(Messages.Envelope.newBuilder()
                .setAttachment(
                    Messages.Attachment.newBuilder()
                        .setTestCaseStartedId(testExecutionId.toString())
                        .setTestStepId(currentTestStepId.toString())
                        .setText(new String(data, UTF_8))
                        //TODO: Add file name to message protocol
                        .setMediaType(mediaType)
                        .build()
                )
                .build()
            );
        } else {
            bus.send(Messages.Envelope.newBuilder()
                .setAttachment(
                    Messages.Attachment.newBuilder()
                        .setTestCaseStartedId(testExecutionId.toString())
                        .setTestStepId(currentTestStepId.toString())
                        .setBinary(ByteString.copyFrom(data))
                        //TODO: Add file name to message protocol
                        .setMediaType(mediaType)
                        .build()
                )
                .build()
            );
        }
    }

    @Override
    public void write(String text) {
        bus.send(new WriteEvent(bus.getInstant(), testCase, text));
        bus.send(Messages.Envelope.newBuilder()
            .setAttachment(
                Messages.Attachment.newBuilder()
                    .setTestCaseStartedId(testExecutionId.toString())
                    .setTestStepId(currentTestStepId.toString())
                    .setText(text)
                    .setMediaType("text/x.cucumber.log+plain")
                    .build()
            )
            .build()
        );
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
        return testCase.getLine();
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

}
