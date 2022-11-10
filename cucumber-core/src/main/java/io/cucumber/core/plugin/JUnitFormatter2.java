package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.TestCase;
import io.cucumber.messages.types.TestCaseFinished;
import io.cucumber.messages.types.TestCaseStarted;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestRunStarted;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Pattern;

import static io.cucumber.messages.TimeConversion.timestampToJavaInstant;
import static io.cucumber.messages.types.TestStepResultStatus.PASSED;
import static io.cucumber.messages.types.TestStepResultStatus.SKIPPED;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public final class JUnitFormatter2 implements ConcurrentEventListener {

    private final MessagesToJunitWriter writer;

    public JUnitFormatter2(OutputStream out) {
        this.writer = new MessagesToJunitWriter(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, this::write);
    }

    private void write(Envelope event) {
        try {
            writer.write(event);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // TODO: Plugins should implement the closable interface
        // and be closed by Cucumber
        if (event.getTestRunFinished().isPresent()) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static class MessagesToJunitWriter implements AutoCloseable {

        private final OutputStreamWriter out;
        private final XmlReportData data = new XmlReportData();

        public MessagesToJunitWriter(OutputStream out) {
            this.out = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        }

        public void write(Envelope envelope) throws IOException {
            data.collect(envelope);
            envelope.getTestRunFinished().ifPresent(this::handleTestRunFinished);
        }

        private void handleTestRunFinished(TestRunFinished testRunFinished) {
            try {
                new XmlReportWriter(data).writeXmlReport(out);
            } catch (XMLStreamException e) {
                throw new CucumberException("Error while transforming.", e);
            }
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }

    private static class XmlReportData {

        private static final long MILLIS_PER_SECOND = SECONDS.toMillis(1L);
        private final Comparator<TestStepResult> testStepResultComparator = nullsFirst(
            comparing(o -> o.getStatus().ordinal()));
        private Instant testRunStarted;
        private Instant testRunFinished;
        private final Deque<String> testCaseStartedIds = new ConcurrentLinkedDeque<>();
        private final Map<String, Instant> testCaseStartedIdToStartedInstant = new ConcurrentHashMap<>();
        private final Map<String, Instant> testCaseStartedIdToFinishedInstant = new ConcurrentHashMap<>();
        private final Map<String, TestStepResult> testCaseStartedIdToResult = new ConcurrentHashMap<>();
        private final Map<String, String> testCaseStartedIdToTestCaseId = new ConcurrentHashMap<>();
        private final Map<String, String> testCaseIdToPickleId = new ConcurrentHashMap<>();
        private final Map<String, String> pickleIdToPickleName = new ConcurrentHashMap<>();
        private final Map<String, String> pickleIdToScenarioAstNodeId = new ConcurrentHashMap<>();
        private final Map<String, String> scenarioAstNodeIdToFeatureName = new ConcurrentHashMap<>();

        void collect(Envelope envelope) {
            envelope.getTestRunStarted().ifPresent(this::testRunStarted);
            envelope.getTestRunFinished().ifPresent(this::testRunFinished);
            envelope.getTestCaseStarted().ifPresent(this::testCaseStarted);
            envelope.getTestCaseFinished().ifPresent(this::testCaseFinished);
            envelope.getTestStepFinished().ifPresent(this::testStepFinished);
            envelope.getGherkinDocument().ifPresent(this::source);
            envelope.getPickle().ifPresent(this::pickle);
            envelope.getTestCase().ifPresent(this::testCase);
        }

        void testRunStarted(TestRunStarted event) {
            this.testRunStarted = timestampToJavaInstant(event.getTimestamp());
        }

        void testRunFinished(TestRunFinished event) {
            this.testRunFinished = timestampToJavaInstant(event.getTimestamp());
        }

        void testCaseStarted(TestCaseStarted event) {
            this.testCaseStartedIds.add(event.getId());
            this.testCaseStartedIdToStartedInstant.put(event.getId(), timestampToJavaInstant(event.getTimestamp()));
            this.testCaseStartedIdToTestCaseId.put(event.getId(), event.getTestCaseId());
        }

        void testCaseFinished(TestCaseFinished event) {
            this.testCaseStartedIdToFinishedInstant.put(event.getTestCaseStartedId(),
                timestampToJavaInstant(event.getTimestamp()));
        }

        void testStepFinished(TestStepFinished event) {
            testCaseStartedIdToResult.compute(event.getTestCaseStartedId(),
                (__, previousStatus) -> mostSevereResult(previousStatus, event.getTestStepResult()));
        }

        void source(GherkinDocument event) {
            event.getFeature().ifPresent(feature -> {
                feature.getChildren().forEach(featureChild -> {
                    featureChild.getRule().ifPresent(rule -> {
                        rule.getChildren().forEach(ruleChild -> {
                            ruleChild.getScenario().ifPresent(scenario -> {
                                scenarioAstNodeIdToFeatureName.put(scenario.getId(), feature.getName());
                            });
                        });
                    });
                    featureChild.getScenario().ifPresent(scenario -> {
                        scenarioAstNodeIdToFeatureName.put(scenario.getId(), feature.getName());
                    });
                });
            });
        }

        void pickle(Pickle pickle) {
            pickleIdToPickleName.put(pickle.getId(), pickle.getName());
            // @formatter:off
            pickle.getAstNodeIds().stream()
                    .findFirst()
                    .ifPresent(id -> pickleIdToScenarioAstNodeId.put(pickle.getId(), id));
            // @formatter:on
        }

        void testCase(TestCase testCase) {
            testCaseIdToPickleId.put(testCase.getId(), testCase.getPickleId());
        }

        private TestStepResult mostSevereResult(TestStepResult a, TestStepResult b) {
            return testStepResultComparator.compare(a, b) >= 0 ? a : b;
        }

        double getSuiteDurationInSeconds() {
            return durationInSeconds(testRunStarted, testRunFinished);
        }

        double getDurationInSeconds(String testCaseStartedId) {
            return durationInSeconds(testCaseStartedIdToStartedInstant.get(testCaseStartedId),
                testCaseStartedIdToFinishedInstant.get(testCaseStartedId));
        }

        private static double durationInSeconds(Instant testRunStarted1, Instant testRunFinished1) {
            return Duration.between(testRunStarted1, testRunFinished1).toMillis() / (double) MILLIS_PER_SECOND;
        }

        Map<TestStepResultStatus, Long> getTestCaseStatusCounts() {
            // @formatter:off
            return testCaseStartedIdToResult.values().stream()
                    .map(TestStepResult::getStatus)
                    .collect(groupingBy(identity(), counting()));
            // @formatter:on
        }

        int getTestCaseCount() {
            return testCaseStartedIdToStartedInstant.size();
        }

        String getPickleName(String testCaseStartedId) {
            String testCaseId = testCaseStartedIdToTestCaseId.get(testCaseStartedId);
            String pickleId = testCaseIdToPickleId.get(testCaseId);
            return pickleIdToPickleName.get(pickleId);
        }

        public String getFeatureName(String testCaseStartedId) {
            String testCaseId = testCaseStartedIdToTestCaseId.get(testCaseStartedId);
            String pickleId = testCaseIdToPickleId.get(testCaseId);
            String astNodeId = pickleIdToScenarioAstNodeId.get(pickleId);
            return scenarioAstNodeIdToFeatureName.get(astNodeId);
        }

        public Deque<String> testCaseStartedIds() {
            return testCaseStartedIds;
        }

        private static final io.cucumber.messages.types.Duration ZERO_DURATION = new io.cucumber.messages.types.Duration(
            0L, 0L);
        private static final TestStepResult SCENARIO_WITH_NO_STEPS = new TestStepResult(ZERO_DURATION, null, PASSED); // By
                                                                                                                      // definition,
                                                                                                                      // but
                                                                                                                      // see
                                                                                                                      // https://github.com/cucumber/gherkin/issues/11
        public TestStepResult getTestCaseStatus(String testCaseStartedId) {
            return testCaseStartedIdToResult.getOrDefault(testCaseStartedId, SCENARIO_WITH_NO_STEPS);
        }

    }

    private static class XmlReportWriter {
        private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        private final XmlReportData data;

        public XmlReportWriter(XmlReportData data) {
            this.data = data;
        }

        private void writeXmlReport(Writer out) throws XMLStreamException {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(out);
            writer.writeStartDocument("UTF-8", "1.0");
            newLine(writer);
            writeTestsuite(data, writer);
            writer.writeEndDocument();
            writer.flush();
        }

        private void writeTestsuite(XmlReportData data, XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("testsuite");
            writeSuiteAttributes(writer);
            newLine(writer);

            for (String testCaseStartedId : data.testCaseStartedIds()) {
                writeTestcase(writer, testCaseStartedId);
            }

            writer.writeEndElement();
            newLine(writer);
        }

        private void writeSuiteAttributes(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeAttribute("name", "Cucumber");
            writer.writeAttribute("time", numberFormat.format(data.getSuiteDurationInSeconds()));

            Map<TestStepResultStatus, Long> counts = data.getTestCaseStatusCounts();

            writer.writeAttribute("tests", String.valueOf(data.getTestCaseCount()));
            writer.writeAttribute("skipped", counts.getOrDefault(SKIPPED, 0L).toString());
            writer.writeAttribute("failures", String.valueOf(countFailures(counts)));
            writer.writeAttribute("errors", "0");
        }

        private static long countFailures(Map<TestStepResultStatus, Long> counts) {
            EnumSet<TestStepResultStatus> notPassedNotSkipped = EnumSet.allOf(TestStepResultStatus.class);
            notPassedNotSkipped.remove(PASSED);
            notPassedNotSkipped.remove(SKIPPED);
            return notPassedNotSkipped.stream().mapToLong(s -> counts.getOrDefault(s, 0L)).sum();
        }

        private void writeTestcase(XMLStreamWriter writer, String id) throws XMLStreamException {
            writer.writeStartElement("testcase");

            writer.writeAttribute("classname", data.getFeatureName(id));
            writer.writeAttribute("name", data.getPickleName(id));
            writer.writeAttribute("time", numberFormat.format(data.getDurationInSeconds(id)));

            writeNonPassedElement(writer, id);

            writer.writeEndElement();
            newLine(writer);
        }

        private void writeNonPassedElement(XMLStreamWriter writer, String id) throws XMLStreamException {
            TestStepResult result = data.getTestCaseStatus(id);
            if (result.getStatus() == TestStepResultStatus.PASSED) {
                return;
            }

            String elementName = result.getStatus() == SKIPPED ? "skipped" : "failure";

            if (result.getMessage().isPresent()) {
                writer.writeStartElement(elementName);
                // writer.writeAttribute("message", ); // TODO: Add to message
                // protocol
                // writer.writeAttribute("type", ); // TODO: Add to message
                // protocol
                newLine(writer);
                // TODO: Write step line listing

                writeCDataSafely(writer, result.getMessage().get());
                writer.writeEndElement();
            } else {
                writer.writeEmptyElement(elementName);
            }
            newLine(writer);
        }
        private static final Pattern CDATA_TERMINATOR_SPLIT = Pattern.compile("(?<=]])(?=>)");
        private static void writeCDataSafely(XMLStreamWriter writer, String data) throws XMLStreamException {
            // https://stackoverflow.com/questions/223652/is-there-a-way-to-escape-a-cdata-end-token-in-xml
            for (String part : CDATA_TERMINATOR_SPLIT.split(data)) {
                writer.writeCData(part);
            }
        }

        private void newLine(XMLStreamWriter xmlWriter) throws XMLStreamException {
            xmlWriter.writeCharacters("\n");
        }

    }

}
