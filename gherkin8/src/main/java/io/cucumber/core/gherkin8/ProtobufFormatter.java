package io.cucumber.core.gherkin8;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.plugin.ProtobufFormat;
import io.cucumber.cucumberexpressions.CucumberExpression;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.gherkin.IdGenerator;
import io.cucumber.messages.Messages;
import io.cucumber.messages.internal.com.google.protobuf.util.JsonFormat;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.StepDefinedEvent;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseDefined;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final OutputStream outputStream;
    private final Writer writer;
    private final ProtobufFormat format;
    private final JsonFormat.Printer jsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace().includingDefaultValueFields();
    private final IdGenerator idGenerator = new IdGenerator.UUID();
    private final Map<TestCase, String> testCaseStartedIdByTestCase = new HashMap<>();
    private final ExpressionFactory expressionFactory = new ExpressionFactory(new ParameterTypeRegistry(Locale.ENGLISH));

    public ProtobufFormatter(OutputStream outputStream, ProtobufFormat format) {
        this.outputStream = outputStream;
        this.format = format;
        this.writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
//        publisher.registerHandlerFor(io.cucumber.plugin.event.StepDefinedEvent.class, this::handleStepDefinedEvent);
//        publisher.registerHandlerFor(io.cucumber.plugin.event.TestSourceRead.class, this::handleTestSourceRead);
        publisher.registerHandlerFor(io.cucumber.plugin.event.GherkinDocumentParsed.class, this::handleGherkinDocumentParsed);
        publisher.registerHandlerFor(io.cucumber.plugin.event.TestCaseDefined.class, this::handleTestCaseDefined);
        publisher.registerHandlerFor(io.cucumber.plugin.event.TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(io.cucumber.plugin.event.TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(io.cucumber.plugin.event.TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(io.cucumber.plugin.event.TestRunFinished.class, this::handleTestRunFinished);
    }

    private void handleGherkinDocumentParsed(io.cucumber.plugin.event.GherkinDocumentParsed t) {
        Gherkin8CucumberFeature feature = (Gherkin8CucumberFeature) t.getFeature();
        this.write(Messages.Envelope
            .newBuilder()
            .setGherkinDocument(
                feature.getGherkinDocument()
            ).build()
        );
        for (Gherkin8CucumberPickle cucumberPickle : feature.getPickles()) {
            this.write(Messages.Envelope
                .newBuilder()
                .setPickle(
                    cucumberPickle.getPickle()
                ).build()
            );
        }
    }

    private void handleStepDefinedEvent(StepDefinedEvent t) {
        Expression expression = expressionFactory.createExpression(t.getStepDefinition().getPattern());
        Messages.StepDefinitionPatternType stepDefinitionPatternType = expression instanceof CucumberExpression ? Messages.StepDefinitionPatternType.CUCUMBER_EXPRESSION : Messages.StepDefinitionPatternType.REGULAR_EXPRESSION;
        write(Messages.Envelope.newBuilder()
            .setStepDefinitionConfig(Messages.StepDefinitionConfig.newBuilder()
                .setPattern(Messages.StepDefinitionPattern.newBuilder()
                    .setSource(t.getStepDefinition().getPattern())
                    .setType(stepDefinitionPatternType)
                ))
            .build());
    }

    private void handleTestCaseDefined(TestCaseDefined t) {
        write(Messages.Envelope.newBuilder()
            .setTestCase(Messages.TestCase.newBuilder()
                .setId(t.getTestCase().getId())
                .setPickleId(t.getTestCase().getPickleId())
                .addAllTestSteps(t.getTestCase().getTestSteps()
                    .stream()
                    .map(testStep -> {
                            Messages.TestCase.TestStep.Builder testStepBuilder = Messages.TestCase.TestStep
                                .newBuilder()
                                .setId(testStep.getId());

                            if (testStep instanceof HookTestStep) {
                                testStepBuilder.setHookId(testStep.getId());
                            } else if (testStep instanceof PickleStepTestStep) {
                                testStepBuilder
                                    .setPickleStepId(testStep.getPickleStepId())
                                    .addAllStepMatchArguments(testStep.getStepMatchArguments());
                            }
                            return testStepBuilder.build();
                        }
                    )
                    .collect(Collectors.toList())
                )
            )
            .build());
    }

    private void handleTestCaseStarted(io.cucumber.plugin.event.TestCaseStarted t) {
        testCaseStartedIdByTestCase.put(t.getTestCase(), t.getId());
        write(Messages.Envelope.newBuilder()
            .setTestCaseStarted(Messages.TestCaseStarted.newBuilder()
                .setId(t.getId())
                .setTestCaseId(t.getTestCase().getId())
                .setTimestamp(Messages.Timestamp.newBuilder()
                    .setSeconds(t.getInstant().getEpochSecond())
                    .setNanos(t.getInstant().getNano())
                )).build());

    }

    private void handleTestCaseFinished(io.cucumber.plugin.event.TestCaseFinished t) {
        String testCaseStartedId = testCaseStartedIdByTestCase.get(t.getTestCase());

        write(Messages.Envelope.newBuilder()
            .setTestCaseFinished(Messages.TestCaseFinished.newBuilder()
                .setTestCaseStartedId(testCaseStartedId)
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
        String testCaseStartedId = testCaseStartedIdByTestCase.get(t.getTestCase());
        write(Messages.Envelope.newBuilder()
            .setTestStepFinished(Messages.TestStepFinished.newBuilder()
                .setTestCaseStartedId(testCaseStartedId)
                .setTestStepId(t.getTestStep().getId())
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

    private void handleTestRunFinished(io.cucumber.plugin.event.TestRunFinished t) {
        try {
            outputStream.close();
            writer.close();
        } catch (IOException e) {
            throw new CucumberException("Failed to close stream", e);
        }
    }

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
                    throw new CucumberException("Unsupported format: " + format.name());
            }
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }
}

