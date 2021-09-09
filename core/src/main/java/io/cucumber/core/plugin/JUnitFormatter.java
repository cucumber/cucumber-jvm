package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceParsed;
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
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static io.cucumber.core.exception.ExceptionUtils.printStackTrace;
import static java.util.Locale.ROOT;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class JUnitFormatter implements EventListener {

    private static final long MILLIS_PER_SECOND = SECONDS.toMillis(1L);
    private final Writer writer;
    private final Document document;
    private final Element rootElement;
    private final Map<URI, Collection<Node>> parsedTestSources = new HashMap<>();
    private Element root;
    private TestCase testCase;
    private URI currentFeatureFile = null;
    private String previousTestCaseName;
    private int exampleNumber;
    private Instant started;

    public JUnitFormatter(OutputStream out) {
        this.writer = new UTF8OutputStreamWriter(out);
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

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);
        publisher.registerHandlerFor(TestSourceParsed.class, this::handleTestSourceParsed);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
    }

    private void handleTestRunStarted(TestRunStarted event) {
        this.started = event.getInstant();
    }

    private void handleTestSourceParsed(TestSourceParsed event) {
        parsedTestSources.put(event.getUri(), event.getNodes());
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

    private void handleTestCaseFinished(TestCaseFinished event) {
        if (testCase.steps.isEmpty()) {
            testCase.handleEmptyTestCase(document, root, event.getResult());
        } else {
            testCase.addTestCaseElement(document, root, event.getResult());
        }
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            testCase.steps.add((PickleStepTestStep) event.getTestStep());
            testCase.results.add(event.getResult());
        }
    }

    private void handleTestRunFinished(TestRunFinished event) {
        try {
            Instant finished = event.getInstant();
            // set up a transformer
            rootElement.setAttribute("name", JUnitFormatter.class.getName());
            rootElement.setAttribute("failures",
                String.valueOf(rootElement.getElementsByTagName("failure").getLength()));
            rootElement.setAttribute("skipped",
                String.valueOf(rootElement.getElementsByTagName("skipped").getLength()));
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

    private void increaseTestCount(Element element) {
        int value = 0;
        if (element.hasAttribute("tests")) {
            value = Integer.parseInt(element.getAttribute("tests"));
        }
        element.setAttribute("tests", String.valueOf(++value));
    }

    private static String calculateTotalDurationString(Duration result) {
        DecimalFormat numberFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        double duration = (double) result.toMillis() / MILLIS_PER_SECOND;
        return numberFormat.format(duration);
    }

    private void closeQuietly(Closeable out) {
        try {
            out.close();
        } catch (IOException ignored) {
            // go gentle into that good night
        }
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
            tc.setAttribute("classname", findRootNodeName(testCase));
            tc.setAttribute("name", calculateElementName(testCase));
        }

        private String findRootNodeName(io.cucumber.plugin.event.TestCase testCase) {
            Location location = testCase.getLocation();
            Predicate<Node> withLocation = candidate -> location.equals(candidate.getLocation());
            return parsedTestSources.get(testCase.getUri())
                    .stream()
                    .map(node -> node.findPathTo(withLocation))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .map(nodes -> nodes.get(0))
                    .flatMap(Node::getName)
                    .orElse("Unknown");
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
                Throwable error = result.getError();
                child = createFailure(doc, sb, "The scenario has pending or undefined step(s)",
                    error == null ? Exception.class : error.getClass());
            } else if (status.is(Status.SKIPPED) && result.getError() != null) {
                addStackTrace(sb, result);
                child = createSkipped(doc, sb, printStackTrace(result.getError()));
            } else {
                child = createElement(doc, sb, "system-out");
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
                sb.append(steps.get(i).getStep().getKeyword());
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

        private Element createFailure(Document doc, StringBuilder sb, String message, Class<? extends Throwable> type) {
            Element child = createElement(doc, sb, "failure");
            child.setAttribute("message", message);
            child.setAttribute("type", type.getName());
            return child;
        }

        private Element createSkipped(Document doc, StringBuilder sb, String message) {
            Element child = createElement(doc, sb, "skipped");
            child.setAttribute("message", message);
            return child;
        }

        private Element createElement(Document doc, StringBuilder sb, String elementType) {
            Element child = doc.createElement(elementType);
            // the createCDATASection method seems to convert "\n" to "\r\n" on
            // Windows, in case
            // data originally contains "\r\n" line separators the result
            // becomes "\r\r\n", which
            // are displayed as double line breaks.
            String normalizedLineEndings = sb.toString().replace(System.lineSeparator(), "\n");
            child.appendChild(doc.createCDATASection(normalizedLineEndings));
            return child;
        }

        void handleEmptyTestCase(Document doc, Element tc, Result result) {
            tc.setAttribute("time", calculateTotalDurationString(result.getDuration()));
            Element child = createFailure(doc, new StringBuilder(), "The scenario has no steps", Exception.class);
            tc.appendChild(child);
        }

    }

}
