package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.TestStep;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestSourceRead;
import cucumber.api.event.TestStepFinished;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.StrictAware;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;
import cucumber.runtime.io.URLOutputStream;
import cucumber.runtime.io.UTF8OutputStreamWriter;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class JUnitFormatter implements Formatter, StrictAware {
    private static final String NUMBER_FORMAT_PATTERN = "0.######";
    private static final String TEST_SUITE_TAG = "testsuite";
    private static final String TEST_CASE_TAG = "testcase";

    private final TestSourcesModel testSources = new TestSourcesModel();
    private final Object mergeToMainDocumentSyncObject = new Object();
    private final Writer out;
    private final Document outputDocument;
    private final Element outputTestSuiteElement;

    private boolean strict;

    private ThreadLocal<CurrentDomElements> domElements = new ThreadLocal<CurrentDomElements>();
    private ThreadLocal<CurrentFeature> featureUnderTest = new ThreadLocal<CurrentFeature>();
    private ThreadLocal<DecimalFormat> numberFormat = new ThreadLocal<DecimalFormat>();

    private EventHandler<TestSourceRead> sourceReadHandler = new EventHandler<TestSourceRead>() {
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

    public JUnitFormatter(URL out) throws IOException {
        this.out = new UTF8OutputStreamWriter(new URLOutputStream(out));
        try {
            outputDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            outputTestSuiteElement = outputDocument.createElement(TEST_SUITE_TAG);
            outputDocument.appendChild(outputTestSuiteElement);
        }
        catch (ParserConfigurationException e) {
            throw new CucumberException("Error while processing unit report", e);
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, sourceReadHandler);
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
        CurrentDomElements currentDomElements = new CurrentDomElements();
        domElements.set(currentDomElements);

        CurrentFeature currentFeature = featureUnderTest.get();
        if (currentFeature == null || currentFeature.uri == null || !currentFeature.uri.equals(event.testCase.getUri())) {
            currentFeature = new CurrentFeature(event.testCase.getUri());
            featureUnderTest.set(currentFeature);
        } else {
            currentFeature.testStepResults.clear();
        }

        currentFeature.testCase = event.testCase;
        currentDomElements.testCaseElement = currentDomElements.document.createElement(TEST_CASE_TAG);
        currentDomElements.testCaseElement.setAttribute("classname", testSources.getFeatureName(currentFeature.uri));
        currentDomElements.testCaseElement.setAttribute("name", calculateElementName(currentFeature));
        currentDomElements.testSuiteElement.appendChild(currentDomElements.testCaseElement);
    }

    private String calculateElementName(final CurrentFeature currentFeature) {
        String testCaseName = currentFeature.testCase.getName();
        if (testCaseName.equals(currentFeature.previousTestCaseName)) {
            return Utils.getUniqueTestNameForScenarioExample(testCaseName, ++currentFeature.exampleNumber);
        } else {
            currentFeature.previousTestCaseName = testCaseName;
            currentFeature.exampleNumber = 1;
            return testCaseName;
        }
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (!event.testStep.isHook()) {
            CurrentFeature currentFeature = featureUnderTest.get();
            currentFeature.testStepResults.add(new TestStepResult(event.testStep, event.result));
        }
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        CurrentFeature currentFeature = featureUnderTest.get();
        if (currentFeature.testStepResults.isEmpty()) {
            handleEmptyTestCase(event.result);
        } else {
            addTestCaseElement(event.result);
        }
        
        synchronized (mergeToMainDocumentSyncObject) {
            final Element localTestCaseElement = domElements.get().testSuiteElement;
            final NodeList testCases = localTestCaseElement.getChildNodes();
            for (int i = 0; i < testCases.getLength(); i++) {
                final Node clonedNode = testCases.item(i).cloneNode(true);
                outputDocument.adoptNode(clonedNode);
                outputTestSuiteElement.appendChild(clonedNode);
            }
        }
    }

    private void handleEmptyTestCase(final Result result) {
        CurrentDomElements currentDomElements = domElements.get();
        currentDomElements.testCaseElement.setAttribute("time", calculateTotalDurationString(result));
        final String resultType = this.strict ? "failure" : "skipped";
        final Element child = createElementWithMessage(currentDomElements.document, new StringBuilder(), resultType, "The scenario has no steps");
        currentDomElements.testCaseElement.appendChild(child);
    }

    private String calculateTotalDurationString(final Result result) {
        DecimalFormat df = numberFormat.get();
        if (df == null) {
            df = getDecimalFormat();
            numberFormat.set(df);
        }
        return df.format(((double) result.getDuration()) / 1000000000);
    }

    private DecimalFormat getDecimalFormat() {
        final DecimalFormat df = new DecimalFormat();
        df.applyPattern(NUMBER_FORMAT_PATTERN);
        return df;
    }

    private Element createElementWithMessage(final Document doc, final StringBuilder sb, final String elementType, final String message) {
        Element child = createElement(doc, sb, elementType);
        child.setAttribute("message", message);
        return child;
    }

    private Element createElement(final Document doc, final StringBuilder sb, final String elementType) {
        Element child = doc.createElement(elementType);
        // the createCDATASection method seems to convert "\n" to "\r\n" on Windows, in case
        // data originally contains "\r\n" line separators the result becomes "\r\r\n", which
        // are displayed as double line breaks.
        // TODO Java 7 PR #1147: Inlined System.lineSeparator()
        String systemLineSeperator = System.getProperty("line.separator");
        child.appendChild(doc.createCDATASection(sb.toString().replace(systemLineSeperator, "\n")));
        return child;
    }

    private void addTestCaseElement(final Result result) {
        CurrentDomElements currentDomElements = domElements.get();
        currentDomElements.testCaseElement.setAttribute("time", calculateTotalDurationString(result));

        StringBuilder sb = new StringBuilder();
        addStepAndResultListing(sb, featureUnderTest.get().testStepResults);
        Element child;
        if (result.is(Result.Type.FAILED)) {
            addStackTrace(sb, result);
            child = createElementWithMessage(currentDomElements.document, sb, "failure", result.getErrorMessage());
        } else if (result.is(Result.Type.AMBIGUOUS)) {
            addStackTrace(sb, result);
            child = createElementWithMessage(currentDomElements.document, sb, "failure", result.getErrorMessage());
        } else if (result.is(Result.Type.PENDING) || result.is(Result.Type.UNDEFINED)) {
            if (this.strict) {
                child = createElementWithMessage(currentDomElements.document, sb, "failure", "The scenario has pending or undefined step(s)");
            }
            else {
                child = createElement(currentDomElements.document, sb, "skipped");
            }
        } else if (result.is(Result.Type.SKIPPED) && result.getError() != null) {
            addStackTrace(sb, result);
            child = createElementWithMessage(currentDomElements.document, sb, "skipped", result.getErrorMessage());
        } else {
            child = createElement(currentDomElements.document, sb, "system-out");
        }

        currentDomElements.testCaseElement.appendChild(child);
    }

    private void addStepAndResultListing(final StringBuilder sb, final List<TestStepResult> stepResults) {
        for (final TestStepResult stepResult : stepResults) {
            int length = sb.length();
            sb.append(getKeywordFromSource(stepResult.step.getStepLine()));
            sb.append(stepResult.step.getStepText());
            do {
                sb.append(".");
            } while (sb.length() - length < 76);
            sb.append(stepResult.result.getStatus().lowerCaseName());
            sb.append("\n");
        }
    }

    private void addStackTrace(final StringBuilder sb, final Result failed) {
        sb.append("\nStackTrace:\n");
        StringWriter sw = new StringWriter();
        failed.getError().printStackTrace(new PrintWriter(sw));
        sb.append(sw.toString());
    }

    private String getKeywordFromSource(int stepLine) {
        return testSources.getKeywordFromSource(featureUnderTest.get().uri, stepLine);
    }

    private void finishReport() {
        try {
            // set up a transformer
            outputTestSuiteElement.setAttribute("name", JUnitFormatter.class.getName());
            outputTestSuiteElement.setAttribute("failures", String.valueOf(outputTestSuiteElement.getElementsByTagName("failure").getLength()));
            outputTestSuiteElement.setAttribute("skipped", String.valueOf(outputTestSuiteElement.getElementsByTagName("skipped").getLength()));
            outputTestSuiteElement.setAttribute("time", sumTimes(outputTestSuiteElement.getElementsByTagName("testcase")));
            outputTestSuiteElement.setAttribute("tests", String.valueOf(outputTestSuiteElement.getChildNodes().getLength()));
            final TransformerFactory transFac = TransformerFactory.newInstance();
            final Transformer trans = transFac.newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            final StreamResult result = new StreamResult(out);
            final DOMSource source = new DOMSource(outputDocument);
            trans.transform(source, result);
            closeQuietly(out);
        } catch (TransformerException e) {
            throw new CucumberException("Error while transforming.", e);
        }
    }

    private String sumTimes(NodeList testCaseNodes) {
        double totalDurationSecondsForAllTimes = 0.0d;
        for( int i = 0; i < testCaseNodes.getLength(); i++ ) {
            try {
                double testCaseTime =
                    Double.parseDouble(testCaseNodes.item(i).getAttributes().getNamedItem("time").getNodeValue());
                totalDurationSecondsForAllTimes += testCaseTime;
            } catch ( NumberFormatException e ) {
                throw new CucumberException(e);
            } catch ( NullPointerException e ) {
                throw new CucumberException(e);
            }
        }
        return getDecimalFormat().format(totalDurationSecondsForAllTimes);
    }

    private void closeQuietly(final Closeable out) {
        try {
            out.close();
        } catch (IOException ignored) {
            // go gentle into that good night
        }
    }

    private class CurrentDomElements {
        private Document document;
        private Element testSuiteElement;
        private Element testCaseElement;

        CurrentDomElements() {
            try {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                testSuiteElement = document.createElement(TEST_SUITE_TAG);
                document.appendChild(testSuiteElement);
            }
            catch (ParserConfigurationException e) {
                throw new CucumberException("Error while processing unit report", e);
            }
        }
    }

    private class CurrentFeature {
        private final String uri;
        private String previousTestCaseName = "";
        private int exampleNumber = 1;
        private TestCase testCase;
        private final List<TestStepResult> testStepResults = new ArrayList<TestStepResult>();

        CurrentFeature(final String uri) {
            this.uri = uri;
        }
    }
    
    private class TestStepResult {
        private final TestStep step;
        private final Result result;

        TestStepResult(TestStep step, Result result) {
            this.step = step;
            this.result = result;
        }
    }
}
