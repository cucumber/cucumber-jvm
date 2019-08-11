package io.cucumber.core.plugin;

import io.cucumber.core.event.PickleStepTestStep;
import io.cucumber.core.event.EventHandler;
import io.cucumber.core.event.EventPublisher;
import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestCaseFinished;
import io.cucumber.core.event.TestCaseStarted;
import io.cucumber.core.event.TestRunFinished;
import io.cucumber.core.event.TestSourceRead;
import io.cucumber.core.event.TestStepFinished;
import io.cucumber.core.exception.CucumberException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static java.util.Locale.ROOT;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class JUnitFormatter implements EventListener, StrictAware {

    private static final long NANOS_PER_SECONDS = SECONDS.toNanos(1L);
    private final Writer writer;
    private final Document document;
    private final Element rootElement;
    private final TestSourcesModel testSources = new TestSourcesModel();
    private Element root;
    private TestCase testCase;
    private boolean strict = false;
    private String currentFeatureFile = null;
    private String previousTestCaseName;
    private int exampleNumber;

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

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(TestRunFinished.class, event -> finishReport());
    }

    @Override
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    private void handleTestSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.getUri(), event);
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

        increaseAttributeValue(rootElement, "tests");
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

    private void finishReport() {
        try {
            // set up a transformer
            rootElement.setAttribute("name", JUnitFormatter.class.getName());
            rootElement.setAttribute("failures", String.valueOf(rootElement.getElementsByTagName("failure").getLength()));
            rootElement.setAttribute("skipped", String.valueOf(rootElement.getElementsByTagName("skipped").getLength()));
            rootElement.setAttribute("time", getTotalDuration(rootElement.getElementsByTagName("testcase")));

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

    private String getTotalDuration(NodeList testCaseNodes) {
        double totalDurationSecondsForAllTimes = 0.0d;
        for (int i = 0; i < testCaseNodes.getLength(); i++) {
            try {
                double testCaseTime =
                    Double.parseDouble(testCaseNodes.item(i).getAttributes().getNamedItem("time").getNodeValue());
                totalDurationSecondsForAllTimes += testCaseTime;
            } catch (NumberFormatException | NullPointerException e) {
                throw new CucumberException(e);
            }
        }
        DecimalFormat nfmt = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        nfmt.applyPattern("0.######");
        return nfmt.format(totalDurationSecondsForAllTimes);
    }

    private void increaseAttributeValue(Element element, String attribute) {
        int value = 0;
        if (element.hasAttribute(attribute)) {
            value = Integer.parseInt(element.getAttribute(attribute));
        }
        element.setAttribute(attribute, String.valueOf(++value));
    }

    final class TestCase {

        private final List<PickleStepTestStep> steps = new ArrayList<>();
        private final List<Result> results = new ArrayList<>();
        private final io.cucumber.core.event.TestCase testCase;

        TestCase(io.cucumber.core.event.TestCase testCase) {
            this.testCase = testCase;
        }

        Element createElement(Document doc) {
            return doc.createElement("testcase");
        }

        void writeElement(Element tc) {
            tc.setAttribute("classname", testSources.getFeatureName(currentFeatureFile));
            tc.setAttribute("name", calculateElementName(testCase));
        }

        private String calculateElementName(io.cucumber.core.event.TestCase testCase) {
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
            tc.setAttribute("time", calculateTotalDurationString(result));

            StringBuilder sb = new StringBuilder();
            addStepAndResultListing(sb);
            Element child;
            Status status = result.getStatus();
            if (status.is(Status.FAILED) || status.is(Status.AMBIGUOUS)) {
                addStackTrace(sb, result);
                child = createElementWithMessage(doc, sb, "failure", printStackTrace(result.getError()));
            } else if (status.is(Status.PENDING) || status.is(Status.UNDEFINED)) {
                if (strict) {
                    child = createElementWithMessage(doc, sb, "failure", "The scenario has pending or undefined step(s)");
                } else {
                    child = createElement(doc, sb, "skipped");
                }
            } else if (status.is(Status.SKIPPED) && result.getError() != null) {
                addStackTrace(sb, result);
                child = createElementWithMessage(doc, sb, "skipped", printStackTrace(result.getError()));
            } else {
                child = createElement(doc, sb, "system-out");
            }

            tc.appendChild(child);
        }

        void handleEmptyTestCase(Document doc, Element tc, Result result) {
            tc.setAttribute("time", calculateTotalDurationString(result));

            String resultType = strict ? "failure" : "skipped";
            Element child = createElementWithMessage(doc, new StringBuilder(), resultType, "The scenario has no steps");

            tc.appendChild(child);
        }

        private String calculateTotalDurationString(Result result) {
            DecimalFormat numberFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
            numberFormat.applyPattern("0.######");
            return numberFormat.format(((double) result.getDuration().toNanos() / NANOS_PER_SECONDS));
        }

        private void addStepAndResultListing(StringBuilder sb) {
            for (int i = 0; i < steps.size(); i++) {
                int length = sb.length();
                String resultStatus = "not executed";
                if (i < results.size()) {
                    resultStatus = results.get(i).getStatus().name().toLowerCase(ROOT);
                }
                sb.append(getKeywordFromSource(steps.get(i).getStepLine()));
                sb.append(steps.get(i).getStepText());
                do {
                    sb.append(".");
                } while (sb.length() - length < 76);
                sb.append(resultStatus);
                sb.append("\n");
            }
        }

        private String getKeywordFromSource(int stepLine) {
            return testSources.getKeywordFromSource(currentFeatureFile, stepLine);
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

        private Element createElementWithMessage(Document doc, StringBuilder sb, String elementType, String message) {
            Element child = createElement(doc, sb, elementType);
            child.setAttribute("message", message);
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
