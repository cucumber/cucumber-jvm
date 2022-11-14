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
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        MessagesToJunitWriter(OutputStream out) {
            this.out = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        }

        void write(Envelope envelope) throws IOException {
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
        private final Map<String, String> pickleIdToPickleUri = new ConcurrentHashMap<>();

        private final Map<String, String> uriToFeatureName = new ConcurrentHashMap<>();

        void collect(Envelope envelope) {
            envelope.getTestRunStarted().ifPresent(this::testRunStarted);
            envelope.getTestRunFinished().ifPresent(this::testRunFinished);
            envelope.getTestCaseStarted().ifPresent(this::testCaseStarted);
            envelope.getTestCaseFinished().ifPresent(this::testCaseFinished);
            envelope.getTestStepFinished().ifPresent(this::testStepFinished);
            envelope.getPickle().ifPresent(this::pickle);
            envelope.getGherkinDocument().ifPresent(this::gherkinDocument);
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

        void pickle(Pickle pickle) {
            pickleIdToPickleName.put(pickle.getId(), pickle.getName());
            pickleIdToPickleUri.put(pickle.getId(), pickle.getUri());
        }

        void gherkinDocument(GherkinDocument event) {
            event.getUri()
                    .ifPresent(uri -> event.getFeature()
                            .ifPresent(feature -> uriToFeatureName.put(uri, feature.getName())));
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

        double getSuiteDurationInSeconds(String uri) {
            // @formatter:off
            Optional<Instant> firstStartedInstant = testCaseStartedIds.stream()
                    .filter(testCaseStartedIdOfFeature(uri))
                    .map(testCaseStartedIdToStartedInstant::get)
                    .sorted()
                    .findFirst();
            // @formatter:on
            // @formatter:off
            Optional<Instant> lastFinishedInstant = testCaseStartedIds.stream()
                    .filter(testCaseStartedIdOfFeature(uri))
                    .map(testCaseStartedIdToFinishedInstant::get)
                    .sorted()
                    .reduce((first, second) -> second);
            // @formatter:on
            // @formatter:off
            return firstStartedInstant
                    .flatMap(started -> lastFinishedInstant.map(finished -> durationInSeconds(started, finished)))
                    .orElse(0.0);
            // @formatter:on
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

        Map<TestStepResultStatus, Long> getTestCaseStatusCounts(String uri) {
            // @formatter:off
            return testCaseStartedIds.stream()
                    .filter(testCaseStartedIdOfFeature(uri))
                    .map(testCaseStartedIdToResult::get)
                    .filter(Objects::nonNull)
                    .map(TestStepResult::getStatus)
                    .collect(groupingBy(identity(), counting()));
            // @formatter:on
        }

        long getTestCaseCount() {
            return testCaseStartedIds.size();
        }

        long getTestCaseCount(String uri) {
            // @formatter:off
            return testCaseStartedIds.stream()
                    .filter(testCaseStartedIdOfFeature(uri))
                    .count();
            // @formatter:on
        }

        String getPickleName(String testCaseStartedId) {
            String testCaseId = testCaseStartedIdToTestCaseId.get(testCaseStartedId);
            String pickleId = testCaseIdToPickleId.get(testCaseId);
            return pickleIdToPickleName.get(pickleId);
        }

        String getFeatureName(String uri) {
            return uriToFeatureName.get(uri);
        }

        Iterable<String> testCaseStartedIds(String uri) {
            return testCaseStartedIds.stream()
                    .filter(testCaseStartedIdOfFeature(uri))
                    .collect(Collectors.toList());
        }

        private Predicate<String> testCaseStartedIdOfFeature(String uri) {
            return id -> {
                String testCaseId = testCaseStartedIdToTestCaseId.get(id);
                String pickleId = testCaseIdToPickleId.get(testCaseId);
                String pickleUri = pickleIdToPickleUri.get(pickleId);
                return uri.equals(pickleUri);
            };
        }

        private static final io.cucumber.messages.types.Duration ZERO_DURATION = new io.cucumber.messages.types.Duration(
                0L, 0L);

        // By definition, but see https://github.com/cucumber/gherkin/issues/11
        private static final TestStepResult SCENARIO_WITH_NO_STEPS = new TestStepResult(ZERO_DURATION, null, PASSED);

        TestStepResult getTestCaseStatus(String testCaseStartedId) {
            return testCaseStartedIdToResult.getOrDefault(testCaseStartedId, SCENARIO_WITH_NO_STEPS);
        }

        Set<String> getFeatureUris() {
            return new LinkedHashSet<>(pickleIdToPickleUri.values());
        }
    }

    private static class XmlReportWriter {
        private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        private final XmlReportData data;

        XmlReportWriter(XmlReportData data) {
            this.data = data;
        }

        private void writeXmlReport(Writer out) throws XMLStreamException {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(out);
            writer.writeStartDocument("UTF-8", "1.0");
            newLine(writer);
            writeTestSuites(writer);
            writer.writeEndDocument();
            writer.flush();
        }

        private void writeTestSuites(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("testsuites");
            writeSuitesAttributes(writer);
            newLine(writer);

            for (String uri : data.getFeatureUris()) {
                writeTestsuite(writer, uri);
            }

            writer.writeEndElement();
            newLine(writer);
        }

        private void writeSuitesAttributes(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeAttribute("name", "Cucumber");
            writer.writeAttribute("time", numberFormat.format(data.getSuiteDurationInSeconds()));

            Map<TestStepResultStatus, Long> counts = data.getTestCaseStatusCounts();

            writer.writeAttribute("tests", String.valueOf(data.getTestCaseCount()));
            writer.writeAttribute("failures", String.valueOf(countFailures(counts)));
            writer.writeAttribute("errors", "0");
        }

        private void writeTestsuite(XMLStreamWriter writer, String uri) throws XMLStreamException {
            writer.writeStartElement("testsuite");
            writeSuiteAttributes(writer, uri);
            newLine(writer);

            for (String testCaseStartedId : data.testCaseStartedIds(uri)) {
                writeTestcase(writer, uri, testCaseStartedId);
            }

            writer.writeEndElement();
            newLine(writer);
        }

        private void writeSuiteAttributes(XMLStreamWriter writer, String uri) throws XMLStreamException {
            writer.writeAttribute("name", data.getFeatureName(uri));
            writer.writeAttribute("time", numberFormat.format(data.getSuiteDurationInSeconds(uri)));

            Map<TestStepResultStatus, Long> counts = data.getTestCaseStatusCounts(uri);

            writer.writeAttribute("tests", String.valueOf(data.getTestCaseCount(uri)));
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

        private void writeTestcase(XMLStreamWriter writer, String uri, String id) throws XMLStreamException {
            writer.writeStartElement("testcase");

            writer.writeAttribute("classname", data.getFeatureName(uri));
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
