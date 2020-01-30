package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.StrictAware;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStepFinished;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.util.Locale.ROOT;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class JUnitFormatter implements EventListener, StrictAware {

    private static final long MILLIS_PER_SECOND = SECONDS.toMillis(1L);
    private final Writer writer;
    private final Document document;
    private final Element rootElement;
    private Element root;
    private TestCase testCase;
    private boolean strict = false;
    private URI currentFeatureFile = null;
    private String previousTestCaseName;
    private int exampleNumber;
    private Instant started;
    private final Map<URI, String> featuresNames = new HashMap<>();
    private final FeatureParser parser = new FeatureParser(UUID::randomUUID);

    @SuppressWarnings("WeakerAccess") // Used by plugin factory
    public JUnitFormatter(URL writer) throws IOException {
        this.writer = new UTF8OutputStreamWriter(new URLOutputStream(writer));
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            rootElement = document.createElement("testsuite");
            document.appendChild(rootElement);
        } catch (ParserConfigurationException e) {
            throw new CucumberException("Error while processing unit report", e);
        }
    }

    private static String getUniqueTestNameForScenarioExample(String testCaseName, int exampleNumber) {
        return testCaseName + (testCaseName.contains(" ") ? " " : "_") + exampleNumber;
    }

    private static String calculateTotalDurationString(Duration result) {
        DecimalFormat numberFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        double duration = (double) result.toMillis() / MILLIS_PER_SECOND;
        return numberFormat.format(duration);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);
        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
    }

    private void handleTestRunStarted(TestRunStarted event) {
        this.started = event.getInstant();
    }

    @Override
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    private void handleTestSourceRead(TestSourceRead event) {
        TestSourceReadResource source = new TestSourceReadResource(event);
        parser.parseResource(source).ifPresent(feature ->
            featuresNames.put(feature.getUri(), feature.getName())
        );
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        if (currentFeatureFile == null || !currentFeatureFile.equals(event.getTestCase().getUri())) {
            currentFeatureFile = event.getTestCase().getUri();
            previousTestCaseName = "";
            exampleNumber = 1;
        }
        testCase = new TestCase(event.getTestCase());
        root = testCase.createElement(document);
        testCase.writeElement(root);
        rootElement.appendChild(root);

        increaseTestCount(rootElement);
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            testCase.steps.add((PickleStepTestStep) event.getTestStep());
            testCase.results.add(event.getResult());
        }
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        if (testCase.steps.isEmpty()) {
            testCase.handleEmptyTestCase(document, root, event.getResult());
        } else {
            testCase.addTestCaseElement(document, root, event.getResult());
        }
    }

    private void handleTestRunFinished(TestRunFinished event) {
        try {
            Instant finished = event.getInstant();
            // set up a transformer
            rootElement.setAttribute("name", JUnitFormatter.class.getName());
            rootElement.setAttribute("failures", String.valueOf(rootElement.getElementsByTagName("failure").getLength()));
            rootElement.setAttribute("skipped", String.valueOf(rootElement.getElementsByTagName("skipped").getLength()));
            rootElement.setAttribute("errors", "0");
            rootElement.setAttribute("time", calculateTotalDurationString(Duration.between(started, finished)));

            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(writer);
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            closeQuietly(writer);
        } catch (TransformerException e) {
            throw new CucumberException("Error while transforming.", e);
        }
    }

    private void closeQuietly(Closeable out) {
        try {
            out.close();
        } catch (IOException ignored) {
            // go gentle into that good night
        }
    }

    private void increaseTestCount(Element element) {
        int value = 0;
        if (element.hasAttribute("tests")) {
            value = Integer.parseInt(element.getAttribute("tests"));
        }
        element.setAttribute("tests", String.valueOf(++value));
    }

    final class TestCase {

        private final List<PickleStepTestStep> steps = new ArrayList<>();
        private final List<Result> results = new ArrayList<>();
        private final io.cucumber.plugin.event.TestCase testCase;

        TestCase(io.cucumber.plugin.event.TestCase testCase) {
            this.testCase = testCase;
        }

        Element createElement(Document doc) {
            return doc.createElement("testcase");
        }

        void writeElement(Element tc) {
            tc.setAttribute("classname", featuresNames.get(currentFeatureFile));
            tc.setAttribute("name", calculateElementName(testCase));
        }

        private String calculateElementName(io.cucumber.plugin.event.TestCase testCase) {
            String testCaseName = testCase.getName();
            if (testCaseName.equals(previousTestCaseName)) {
                return getUniqueTestNameForScenarioExample(testCaseName, ++exampleNumber);
            } else {
                previousTestCaseName = testCase.getName();
                exampleNumber = 1;
                return testCaseName;
            }
        }

        void addTestCaseElement(Document doc, Element tc, Result result) {
            tc.setAttribute("time", calculateTotalDurationString(result.getDuration()));

            StringBuilder sb = new StringBuilder();
            addStepAndResultListing(sb);
            Element child;
            Status status = result.getStatus();
            if (status.is(Status.FAILED) || status.is(Status.AMBIGUOUS)) {
                addStackTrace(sb, result);
                child = createFailure(doc, sb, result.getError().getMessage(), result.getError().getClass());
            } else if (status.is(Status.PENDING) || status.is(Status.UNDEFINED)) {
                if (strict) {
                    Throwable error = result.getError();
                    child = createFailure(doc, sb, "The scenario has pending or undefined step(s)", error == null ? Exception.class : error.getClass());
                } else {
                    child = createElement(doc, sb, "skipped");
                }
            } else if (status.is(Status.SKIPPED) && result.getError() != null) {
                addStackTrace(sb, result);
                child = createSkipped(doc, sb, printStackTrace(result.getError()));
            } else {
                child = createElement(doc, sb, "system-out");
            }

            tc.appendChild(child);
        }

        void handleEmptyTestCase(Document doc, Element tc, Result result) {
            tc.setAttribute("time", calculateTotalDurationString(result.getDuration()));

            Element child;
            if (strict) {
                child = createFailure(doc, new StringBuilder(), "The scenario has no steps", Exception.class);
            } else {
                child = createSkipped(doc, new StringBuilder(), "The scenario has no steps");
            }

            tc.appendChild(child);
        }

        private void addStepAndResultListing(StringBuilder sb) {
            for (int i = 0; i < steps.size(); i++) {
                int length = sb.length();
                String resultStatus = "not executed";
                if (i < results.size()) {
                    resultStatus = results.get(i).getStatus().name().toLowerCase(ROOT);
                }
                sb.append(steps.get(i).getStep().getKeyWord());
                sb.append(steps.get(i).getStepText());
                do {
                    sb.append(".");
                } while (sb.length() - length < 76);
                sb.append(resultStatus);
                sb.append("\n");
            }
        }

        private void addStackTrace(StringBuilder sb, Result failed) {
            sb.append("\nStackTrace:\n");
            sb.append(printStackTrace(failed.getError()));
        }

        private String printStackTrace(Throwable error) {
            StringWriter stringWriter = new StringWriter();
            error.printStackTrace(new PrintWriter(stringWriter));
            return stringWriter.toString();
        }

        private Element createSkipped(Document doc, StringBuilder sb, String message) {
            Element child = createElement(doc, sb, "skipped");
            child.setAttribute("message", message);
            return child;
        }

        private Element createFailure(Document doc, StringBuilder sb, String message, Class<? extends Throwable> type) {
            Element child = createElement(doc, sb, "failure");
            child.setAttribute("message", message);
            child.setAttribute("type", type.getName());
            return child;
        }

        private Element createElement(Document doc, StringBuilder sb, String elementType) {
            Element child = doc.createElement(elementType);
            // the createCDATASection method seems to convert "\n" to "\r\n" on Windows, in case
            // data originally contains "\r\n" line separators the result becomes "\r\r\n", which
            // are displayed as double line breaks.
            String normalizedLineEndings = sb.toString().replace(System.lineSeparator(), "\n");
            child.appendChild(doc.createCDATASection(normalizedLineEndings));
            return child;
        }
    }

}
