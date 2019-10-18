package io.cucumber.core.gherkin8.formatter;

import io.cucumber.gherkin.Gherkin;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestStep;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class ProtobufFormatter implements EventListener {
    private static final Map<Status, Messages.TestResult.Status> STATUS = new HashMap<Status, Messages.TestResult.Status>() {{
        put(Status.FAILED, Messages.TestResult.Status.FAILED);
        put(Status.PASSED, Messages.TestResult.Status.PASSED);
        put(Status.UNDEFINED, Messages.TestResult.Status.UNDEFINED);
        put(Status.PENDING, Messages.TestResult.Status.PENDING);
        put(Status.SKIPPED, Messages.TestResult.Status.SKIPPED);
        put(Status.AMBIGUOUS, Messages.TestResult.Status.AMBIGUOUS);
        put(Status.UNUSED, Messages.TestResult.Status.UNKNOWN);
    }};
    private final OutputStream out;
    private Map<String, String> pickleIdByUriAndLine = new HashMap<>();

    public ProtobufFormatter(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(io.cucumber.plugin.event.TestSourceRead.class, this::handleTestSourceRead);
        publisher.registerHandlerFor(io.cucumber.plugin.event.TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(io.cucumber.plugin.event.TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(io.cucumber.plugin.event.TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
    }

    private void handleTestSourceRead(io.cucumber.plugin.event.TestSourceRead t) {
        Gherkin.fromSources(singletonList(Gherkin.makeSourceEnvelope(t.getSource(), t.getUri().toString())), false, true, true).forEach(e -> {
            if (e.hasPickle()) {
                for (Messages.Location location : e.getPickle().getLocationsList()) {
                    String uriAndLine = uriAndLine(e.getPickle().getUri(), location.getLine());
                    pickleIdByUriAndLine.put(uriAndLine, e.getPickle().getId());
                }
            }
            write(e);
        });
    }

    private void handleTestCaseStarted(io.cucumber.plugin.event.TestCaseStarted t) {
        String pickleId = getPickleId(t.getTestCase());

        write(Messages.Envelope.newBuilder()
            .setTestCaseStarted(Messages.TestCaseStarted.newBuilder()
                .setPickleId(pickleId)
                .setTimestamp(Messages.Timestamp.newBuilder()
                    .setSeconds(t.getInstant().getEpochSecond())
                    .setNanos(t.getInstant().getNano())
                )).build());

    }

    private void handleTestCaseFinished(io.cucumber.plugin.event.TestCaseFinished t) {
        String pickleId = getPickleId(t.getTestCase());

        write(Messages.Envelope.newBuilder()
            .setTestCaseFinished(Messages.TestCaseFinished.newBuilder()
                .setPickleId(pickleId)
                .setTimestamp(Messages.Timestamp.newBuilder()
                    .setSeconds(t.getInstant().getEpochSecond())
                    .setNanos(t.getInstant().getNano())
                )
                .setTestResult(Messages.TestResult.newBuilder()
                    .setStatus(STATUS.get(t.getResult().getStatus()))
                )
            ).build());
    }

    private void handleTestStepFinished(io.cucumber.plugin.event.TestStepFinished t) {
        if (t.getTestStep() instanceof HookTestStep) {
            return;
        }
        String pickleId = getPickleId(t.getTestCase());
        List<TestStep> pickleTestSteps = t.getTestCase().getTestSteps()
            .stream().filter(s -> s instanceof PickleStepTestStep)
            .collect(Collectors.toList());

        int stepIndex = pickleTestSteps
            .indexOf(t.getTestStep());

        write(Messages.Envelope.newBuilder()
            .setTestStepFinished(Messages.TestStepFinished.newBuilder()
                .setPickleId(pickleId)
                .setIndex(stepIndex)
                .setTimestamp(Messages.Timestamp.newBuilder()
                    .setSeconds(t.getInstant().getEpochSecond())
                    .setNanos(t.getInstant().getNano())
                )
                .setTestResult(Messages.TestResult.newBuilder()
                    .setStatus(STATUS.get(t.getResult().getStatus()))
                    .setDuration(Messages.Duration.newBuilder()
                        .setSeconds(t.getResult().getDuration().getSeconds())
                        .setNanos(t.getResult().getDuration().getNano())
                    )
                )
            ).build());
    }

    private void handleTestRunFinished(TestRunFinished t) {
        try {
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String uriAndLine(String uri, int line) {
        return String.format("%s:%d", uri, line);
    }

    private String getPickleId(TestCase testCase) {
        String uriAndLine = uriAndLine(testCase.getUri().toString(), testCase.getLine());
        return pickleIdByUriAndLine.get(uriAndLine);
    }

    private void write(Messages.Envelope m) {
        try {
            m.writeDelimitedTo(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

