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
import org.w3c.dom.NamedNodeMap;

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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static io.cucumber.core.exception.ExceptionUtils.printStackTrace;
import static java.time.Duration.ZERO;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.Locale.ROOT;

public final class TestNGFormatter implements EventListener {

    private final Writer writer;
    private final Document document;
    private final Element results;
    private final Element suite;
    private final Element test;
    private final Map<URI, Collection<Node>> parsedTestSources = new HashMap<>();
    private Element clazz;
    private Element root;
    private TestCase testCase;
    private URI currentFeatureFile = null;
    private String previousTestCaseName;
    private int exampleNumber;
    private Instant started;

    public TestNGFormatter(OutputStream out) {
        this.writer = new UTF8OutputStreamWriter(out);
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            results = document.createElement("testng-results");
            suite = document.createElement("suite");
            test = document.createElement("test");
            suite.appendChild(test);
            results.appendChild(suite);
            document.appendChild(results);
        } catch (ParserConfigurationException e) {
            throw new CucumberException("Error initializing DocumentBuilder.", e);
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceParsed.class, this::handleTestSourceParsed);
        publisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);
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
            clazz = document.createElement("class");
            clazz.setAttribute("name", findRootNodeName(event.getTestCase()));
            test.appendChild(clazz);
        }
        root = document.createElement("test-method");
        clazz.appendChild(root);
        testCase = new TestCase(event.getTestCase());
        testCase.start(root, event.getInstant());
    }

    private String findRootNodeName(io.cucumber.plugin.event.TestCase testCase) {
        Location location = testCase.getLocation();
        Predicate<Node> withLocation = candidate -> candidate.getLocation().equals(location);
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

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            testCase.steps.add((PickleStepTestStep) event.getTestStep());
            testCase.results.add(event.getResult());
        } else {
            testCase.hooks.add(event.getResult());
        }
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        testCase.finish(document, root, event.getInstant());
    }

    private void handleTestRunFinished(TestRunFinished event) {
        try {
            Instant finished = event.getInstant();
            Duration duration = Duration.between(started, finished);
            results.setAttribute("total", String.valueOf(getElementsCountByAttribute(suite, "status", ".*")));
            results.setAttribute("passed", String.valueOf(getElementsCountByAttribute(suite, "status", "PASS")));
            results.setAttribute("failed", String.valueOf(getElementsCountByAttribute(suite, "status", "FAIL")));
            results.setAttribute("skipped", String.valueOf(getElementsCountByAttribute(suite, "status", "SKIP")));
            suite.setAttribute("name", TestNGFormatter.class.getName());
            suite.setAttribute("duration-ms", String.valueOf(duration.toMillis()));
            test.setAttribute("name", TestNGFormatter.class.getName());
            test.setAttribute("duration-ms", String.valueOf(duration.toMillis()));

            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult streamResult = new StreamResult(writer);
            DOMSource domSource = new DOMSource(document);
            transformer.transform(domSource, streamResult);
            closeQuietly(writer);
        } catch (TransformerException e) {
            throw new CucumberException("Error transforming report.", e);
        }
    }

    private void closeQuietly(Closeable out) {
        try {
            out.close();
        } catch (IOException ignored) {
            // go gentle into that good night
        }
    }

    private int getElementsCountByAttribute(org.w3c.dom.Node node, String attributeName, String attributeValue) {
        int count = 0;

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            count += getElementsCountByAttribute(node.getChildNodes().item(i), attributeName, attributeValue);
        }

        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            org.w3c.dom.Node namedItem = attributes.getNamedItem(attributeName);
            if (namedItem != null && namedItem.getNodeValue().matches(attributeValue)) {
                count++;
            }
        }

        return count;
    }

    final class TestCase {

        private final List<PickleStepTestStep> steps = new ArrayList<>();
        private final List<Result> results = new ArrayList<>();
        private final List<Result> hooks = new ArrayList<>();
        private final io.cucumber.plugin.event.TestCase testCase;

        TestCase(io.cucumber.plugin.event.TestCase testCase) {
            this.testCase = testCase;
        }

        private void start(Element element, Instant instant) {
            element.setAttribute("name", calculateElementName(testCase));
            element.setAttribute("started-at", ISO_INSTANT.format(instant));
        }

        private String calculateElementName(io.cucumber.plugin.event.TestCase testCase) {
            String testCaseName = testCase.getName();
            if (testCaseName.equals(previousTestCaseName)) {
                return testCaseName + "_" + ++exampleNumber;
            } else {
                previousTestCaseName = testCaseName;
                exampleNumber = 1;
                return testCaseName;
            }
        }

        void finish(Document doc, Element element, Instant instant) {
            element.setAttribute("duration-ms", calculateTotalDurationString());
            element.setAttribute("finished-at", ISO_INSTANT.format(instant));
            StringBuilder stringBuilder = new StringBuilder();
            addStepAndResultListing(stringBuilder);
            Result skipped = null;
            Result failed = null;
            for (Result result : results) {
                if (result.getStatus().is(Status.FAILED) || result.getStatus().is(Status.AMBIGUOUS)) {
                    failed = result;
                }
                if (result.getStatus().is(Status.UNDEFINED) || result.getStatus().is(Status.PENDING)) {
                    skipped = result;
                }
            }
            for (Result result : hooks) {
                if (failed == null && result.getStatus().is(Status.FAILED)) {
                    failed = result;
                }
            }
            if (failed != null) {
                element.setAttribute("status", "FAIL");
                String stacktrace = printStackTrace(failed.getError());
                Element exception = createException(doc, failed.getError().getClass().getName(),
                    stringBuilder.toString(), stacktrace);
                element.appendChild(exception);
            } else if (skipped != null) {
                element.setAttribute("status", "FAIL");
                Element exception = createException(doc, "The scenario has pending or undefined step(s)",
                    stringBuilder.toString(), "The scenario has pending or undefined step(s)");
                element.appendChild(exception);
            } else {
                element.setAttribute("status", "PASS");
            }
        }

        private String calculateTotalDurationString() {
            Duration totalDuration = ZERO;
            for (Result r : results) {
                totalDuration = totalDuration.plus(r.getDuration());
            }
            for (Result r : hooks) {
                totalDuration = totalDuration.plus(r.getDuration());
            }
            return String.valueOf(totalDuration.toMillis());
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

        private Element createException(Document doc, String clazz, String message, String stacktrace) {
            Element exceptionElement = doc.createElement("exception");
            exceptionElement.setAttribute("class", clazz);

            if (message != null) {
                Element messageElement = doc.createElement("message");
                messageElement.appendChild(doc.createCDATASection(message));
                exceptionElement.appendChild(messageElement);
            }

            Element stacktraceElement = doc.createElement("full-stacktrace");
            stacktraceElement.appendChild(doc.createCDATASection(stacktrace));
            exceptionElement.appendChild(stacktraceElement);

            return exceptionElement;
        }

    }

}
