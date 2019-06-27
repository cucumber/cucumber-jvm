package io.cucumber.core.plugin;

import io.cucumber.core.api.event.PickleStepTestStep;
import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.plugin.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.event.TestCaseFinished;
import io.cucumber.core.api.event.TestCaseStarted;
import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.api.event.TestSourceRead;
import io.cucumber.core.api.event.TestStepFinished;
import io.cucumber.core.api.plugin.StrictAware;
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

    private EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
        @Override
        public void receive(TestSourceRead event) {
            handleTestSourceRead(event);
        }
    };
    private EventHandler<TestCaseStarted> caseStartedHandler = new EventHandler<TestCaseStarted>() {
        @Override
        public void receive(TestCaseStarted event) {
            handleTestCaseStarted(event);
        }
    };
    private EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            handleTestStepFinished(event);
        }
    };
    private EventHandler<TestCaseFinished> caseFinishedHandler = new EventHandler<TestCaseFinished>() {
        @Override
        public void receive(TestCaseFinished event) {
            handleTestCaseFinished(event);
        }
    };
    private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            finishReport();
        }
    };

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
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, caseStartedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, caseFinishedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
    }

    @Override
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    private void handleTestSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.uri, event);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        if (currentFeatureFile == null || !currentFeatureFile.equals(event.testCase.getUri())) {
            currentFeatureFile = event.testCase.getUri();
            previousTestCaseName = "";
            exampleNumber = 1;
        }
        testCase = new TestCase(event.testCase);
        root = testCase.createElement(document);
        testCase.writeElement(root);
        rootElement.appendChild(root);

        increaseAttributeValue(rootElement, "tests");
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.testStep instanceof PickleStepTestStep) {
            testCase.steps.add((PickleStepTestStep) event.testStep);
            testCase.results.add(event.result);
        }
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        if (testCase.steps.isEmpty()) {
            testCase.handleEmptyTestCase(document, root, event.result);
        } else {
            testCase.addTestCaseElement(document, root, event.result);
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
        private final io.cucumber.core.api.event.TestCase testCase;

        TestCase(io.cucumber.core.api.event.TestCase testCase) {
            this.testCase = testCase;
        }

        Element createElement(Document doc) {
            return doc.createElement("testcase");
        }

        void writeElement(Element tc) {
            tc.setAttribute("classname", testSources.getFeatureName(currentFeatureFile));
            tc.setAttribute("name", calculateElementName(testCase));
        }

        private String calculateElementName(io.cucumber.core.api.event.TestCase testCase) {
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
            if (result.is(Result.Type.FAILED) || result.is(Result.Type.AMBIGUOUS)) {
                addStackTrace(sb, result);
                child = createElementWithMessage(doc, sb, "failure", result.getErrorMessage());
            } else if (result.is(Result.Type.PENDING) || result.is(Result.Type.UNDEFINED)) {
                if (strict) {
                    child = createElementWithMessage(doc, sb, "failure", "The scenario has pending or undefined step(s)");
                } else {
                    child = createElement(doc, sb, "skipped");
                }
            } else if (result.is(Result.Type.SKIPPED) && result.getError() != null) {
                addStackTrace(sb, result);
                child = createElementWithMessage(doc, sb, "skipped", result.getErrorMessage());
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
                    resultStatus = results.get(i).getStatus().lowerCaseName();
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
            StringWriter sw = new StringWriter();
            failed.getError().printStackTrace(new PrintWriter(sw));
            sb.append(sw.toString());
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
