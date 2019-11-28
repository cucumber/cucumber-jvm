package io.cucumber.plugin.protobuf;

import io.cucumber.messages.Messages;
import io.cucumber.messages.internal.com.google.protobuf.util.JsonFormat;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

// TODO: Move back to core
public final class ProtobufFormatter implements EventListener {
    //    private static final Map<Status, Messages.TestResult.Status> STATUS = new HashMap<Status, Messages.TestResult.Status>() {{
//        put(Status.FAILED, Messages.TestResult.Status.FAILED);
//        put(Status.PASSED, Messages.TestResult.Status.PASSED);
//        put(Status.UNDEFINED, Messages.TestResult.Status.UNDEFINED);
//        put(Status.PENDING, Messages.TestResult.Status.PENDING);
//        put(Status.SKIPPED, Messages.TestResult.Status.SKIPPED);
//        put(Status.AMBIGUOUS, Messages.TestResult.Status.AMBIGUOUS);
//        put(Status.UNUSED, Messages.TestResult.Status.UNKNOWN);
//    }};
    private final OutputStream outputStream;
    private final Writer writer;
    private final JsonFormat.Printer jsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace().includingDefaultValueFields();
    //    private final Map<TestCase, String> testCaseStartedIdByTestCase = new HashMap<>();
    private final ProtobufFormat format;

    public ProtobufFormatter(File file) throws FileNotFoundException {
        this.format = file.getPath().endsWith(".ndjson") ? ProtobufFormat.NDJSON : ProtobufFormat.PROTOBUF;
        this.outputStream = new FileOutputStream(file);
        this.writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(io.cucumber.messages.Messages.Envelope.class, this::writeMessage);
    }

    private void writeMessage(io.cucumber.messages.Messages.Envelope envelope) {
        write(envelope);
    }


    //
//    private void handleGherkinDocumentParsed(io.cucumber.plugin.event.GherkinDocumentParsed t) {
////        CucumberFeature feature = t.getFeature();
////        this.write(Messages.Envelope
////            .newBuilder()
////            .setGherkinDocument(
////                feature.getGherkinDocument()
////            ).build()
////        );
////        for (Gherkin8CucumberPickle cucumberPickle : feature.getPickles()) {
////            this.write(Messages.Envelope
////                .newBuilder()
////                .setPickle(
////                    cucumberPickle.getPickle()
////                ).build()
////            );
////        }
//    }
//
//    private void handleTestCaseDefined(TestCaseDefined t) {
//        write(Messages.Envelope.newBuilder()
//            .setTestCase(Messages.TestCase.newBuilder()
//                .setId(t.getTestCase().getId())
//                .setPickleId(t.getTestCase().getPickleId())
//                .addAllTestSteps(t.getTestCase().getTestSteps()
//                    .stream()
//                    .map(testStep -> {
//                            Messages.TestCase.TestStep.Builder testStepBuilder = Messages.TestCase.TestStep
//                                .newBuilder()
//                                .setId(testStep.getId());
//
//                            if (testStep instanceof HookTestStep) {
//                                testStepBuilder.setHookId(testStep.getId());
//                            } else if (testStep instanceof PickleStepTestStep) {
//                                testStepBuilder
//                                    .setPickleStepId(testStep.getPickleStepId())
//                                    .addAllStepMatchArguments(testStep.getStepMatchArguments());
//                            }
//                            return testStepBuilder.build();
//                        }
//                    )
//                    .collect(Collectors.toList())
//                )
//            )
//            .build());
//    }
//
//    private void handleTestCaseStarted(io.cucumber.plugin.event.TestCaseStarted t) {
//        testCaseStartedIdByTestCase.put(t.getTestCase(), t.getId());
//        write(Messages.Envelope.newBuilder()
//            .setTestCaseStarted(Messages.TestCaseStarted.newBuilder()
//                .setId(t.getId())
//                .setTestCaseId(t.getTestCase().getId())
//                .setTimestamp(Messages.Timestamp.newBuilder()
//                    .setSeconds(t.getInstant().getEpochSecond())
//                    .setNanos(t.getInstant().getNano())
//                )).build());
//
//    }
//
//    private void handleTestCaseFinished(io.cucumber.plugin.event.TestCaseFinished t) {
//        String testCaseStartedId = testCaseStartedIdByTestCase.get(t.getTestCase());
//
//        write(Messages.Envelope.newBuilder()
//            .setTestCaseFinished(Messages.TestCaseFinished.newBuilder()
//                .setTestCaseStartedId(testCaseStartedId)
//                .setTimestamp(Messages.Timestamp.newBuilder()
//                    .setSeconds(t.getInstant().getEpochSecond())
//                    .setNanos(t.getInstant().getNano())
//                )
//                .setTestResult(Messages.TestResult.newBuilder()
//                    .setStatus(STATUS.get(t.getResult().getStatus()))
//                )
//            ).build());
//    }
//
//    private void handleTestStepFinished(io.cucumber.plugin.event.TestStepFinished t) {
//        String testCaseStartedId = testCaseStartedIdByTestCase.get(t.getTestCase());
//        write(Messages.Envelope.newBuilder()
//            .setTestStepFinished(Messages.TestStepFinished.newBuilder()
//                .setTestCaseStartedId(testCaseStartedId)
//                .setTestStepId(t.getTestStep().getId())
//                .setTimestamp(Messages.Timestamp.newBuilder()
//                    .setSeconds(t.getInstant().getEpochSecond())
//                    .setNanos(t.getInstant().getNano())
//                )
//                .setTestResult(Messages.TestResult.newBuilder()
//                    .setStatus(STATUS.get(t.getResult().getStatus()))
//                    .setDuration(Messages.Duration.newBuilder()
//                        .setSeconds(t.getResult().getDuration().getSeconds())
//                        .setNanos(t.getResult().getDuration().getNano())
//                    )
//                )
//            ).build());
//    }
//
//    private void handleTestRunFinished(io.cucumber.plugin.event.TestRunFinished t) {
//        try {
//            outputStream.close();
//            writer.close();
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to close stream", e);
//        }
//    }

    private void write(Messages.Envelope m) {
        try {
            switch (format) {
                case PROTOBUF:
                    m.writeDelimitedTo(outputStream);
                    break;
                case NDJSON:
                    String json = jsonPrinter.print(m);
                    writer.write(json);
                    writer.write("\n");
                    writer.flush();
                    break;
                default:
                    throw new IllegalStateException("Unsupported format: " + format.name());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

