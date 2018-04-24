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
import cucumber.runtime.io.URLOutputStream;
import cucumber.runtime.io.UTF8OutputStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class TestNGFormatter implements Formatter, StrictAware {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    private final TestSourcesModel testSources = new TestSourcesModel();
    private final Object mergeToMainDocumentSyncObject = new Object();
    private final XPath xPath = XPathFactory.newInstance().newXPath();
    private final Writer writer;
    private final Document outputDocument;
    private final Element outputNgResultsElement;
    private final Element outputSuiteElement;
    private final Element outputTestElement;

    private boolean strict;

    private ThreadLocal<CurrentDomElements> domElements = new ThreadLocal<CurrentDomElements>() {
        @Override
        protected CurrentDomElements initialValue() {
            return new CurrentDomElements();
        }
    };
    private ThreadLocal<CurrentFeature> featureUnderTest = new ThreadLocal<CurrentFeature>();
    private ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>();

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
            handleTestCaseFinished();
        }
    };
    private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            finishReport();
        }
    };

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public TestNGFormatter(URL url) throws IOException {
        this.writer = new UTF8OutputStreamWriter(new URLOutputStream(url));
        final CurrentDomElements outputDomElements = new CurrentDomElements();
        this.outputDocument = outputDomElements.document;
        this.outputNgResultsElement = outputDomElements.ngResultsElement;
        this.outputSuiteElement = outputDomElements.suiteElement;
        this.outputTestElement = outputDomElements.testElement;
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
        final CurrentDomElements currentDomElements = domElements.get();
        CurrentFeature currentFeature = featureUnderTest.get();
        if (currentFeature == null || currentFeature.uri == null || !currentFeature.uri.equals(event.testCase.getUri())) {
            currentFeature = new CurrentFeature(event.testCase.getUri());
            featureUnderTest.set(currentFeature);
            currentDomElements.classElement = currentDomElements.document.createElement("class");
            currentDomElements.classElement.setAttribute("name", testSources.getFeature(event.testCase.getUri()).getName());
            currentDomElements.testElement.appendChild(currentDomElements.classElement);
        }
        else {
            currentFeature.testStepResults.clear();
            currentFeature.hooks.clear();
        }
        
        currentDomElements.testMethodElement = currentDomElements.document.createElement("test-method");
        currentDomElements.testMethodElement.setAttribute("name", calculateElementName(event.testCase, currentFeature));
        currentDomElements.testMethodElement.setAttribute("started-at", getDateTime());
        currentDomElements.classElement.appendChild(currentDomElements.testMethodElement);
    }

    private String calculateElementName(final TestCase testCase, final CurrentFeature currentFeature) {
        String testCaseName = testCase.getName();
        if (testCaseName.equals(currentFeature.previousTestCaseName)) {
            return testCaseName + "_" + ++currentFeature.exampleNumber;
        } else {
            currentFeature.previousTestCaseName = testCaseName;
            currentFeature.exampleNumber = 1;
            return testCaseName;
        }
    }
    
    private String getDateTime() {
        SimpleDateFormat df = dateFormat.get();
        if (df == null) {
            df = getNewDateFormat();
            dateFormat.set(df);
        }
        return df.format(new Date());
    }
    
    private SimpleDateFormat getNewDateFormat() {
        return new SimpleDateFormat(DATE_FORMAT_PATTERN);
    }

    private void handleTestStepFinished(TestStepFinished event) {
        final CurrentFeature currentFeature = featureUnderTest.get();
        if (!event.testStep.isHook()) {
            currentFeature.testStepResults.add(new TestStepResult(event.testStep, event.result));
        } else {
            currentFeature.hooks.add(event.result);
        }
    }

    private void handleTestCaseFinished() {
        final CurrentDomElements currentDomElements = domElements.get();
        final CurrentFeature currentFeature = featureUnderTest.get();
        writeResultToLocalDocument(currentDomElements, currentFeature);
        
        synchronized (mergeToMainDocumentSyncObject) {
            final Element localClassElement = currentDomElements.classElement;
            final String featureName = localClassElement.getAttributeNode("name").getValue();
            Node targetClassElement = findSingleNodeByAttributeValue(outputTestElement, "name", featureName);
            if (targetClassElement == null) {
                targetClassElement = localClassElement.cloneNode(false);
                outputDocument.adoptNode(targetClassElement);
                outputTestElement.appendChild(targetClassElement);
            }
            final NodeList testCases = localClassElement.getChildNodes();
            for (int i = 0; i < testCases.getLength(); i++) {
                final Node node = testCases.item(i);
                localClassElement.removeChild(node);
                outputDocument.adoptNode(node);
                targetClassElement.appendChild(node);
            }
        }
    }
    
    private Node findSingleNodeByAttributeValue(final Element element, final String name, final String value) {
        final NodeList matchingNodes = getNodeListByAttributeValue(element, name, value);
        if (matchingNodes == null || matchingNodes.getLength() == 0) {
            return null;
        }
        if (matchingNodes.getLength() > 1) {
            throw new CucumberException("More than 1 node found matching tagName [" + name + "]");
        }
        return matchingNodes.item(0);
    }

    private NodeList getNodeListByAttributeValue(final Element element, final String name, final String value) {
        try {
            final String exp = "*[@" + name + "='" + value + "']";
            final XPathExpression xExpress = xPath.compile(exp);
            return (NodeList) xExpress.evaluate(element, XPathConstants.NODESET);
        }
        catch(final XPathExpressionException e) {
            throw new CucumberException(e);
        }
    }

    private void writeResultToLocalDocument(final CurrentDomElements currentDomElements, final CurrentFeature currentFeature) {
        currentDomElements.testMethodElement.setAttribute("duration-ms", calculateTotalDurationString(currentFeature));
        currentDomElements.testMethodElement.setAttribute("finished-at", getDateTime());

        final StringBuilder stringBuilder = buildStepAndResultListing(currentFeature);
        Result skipped = null;
        Result failed = null;
        for (final TestStepResult stepResult : currentFeature.testStepResults) {
            if (stepResult.result.is(Result.Type.FAILED) || stepResult.result.is(Result.Type.AMBIGUOUS)) {
                failed = stepResult.result;
            }
            if (stepResult.result.is(Result.Type.UNDEFINED) || stepResult.result.is(Result.Type.PENDING)) {
                skipped = stepResult.result;
            }
        }
        for (final Result result : currentFeature.hooks) {
            if (failed == null && result.is(Result.Type.FAILED)) {
                failed = result;
            }
        }
        if (failed != null) {
            currentDomElements.testMethodElement.setAttribute("status", "FAIL");
            final StringWriter stringWriter = new StringWriter();
            failed.getError().printStackTrace(new PrintWriter(stringWriter));
            final Element exception = createException(currentDomElements.document, failed.getError().getClass().getName(), stringBuilder.toString(), stringWriter.toString());
            currentDomElements.testMethodElement.appendChild(exception);
        } else if (skipped != null) {
            if (this.strict) {
                currentDomElements.testMethodElement.setAttribute("status", "FAIL");
                Element exception = createException(currentDomElements.document, "The scenario has pending or undefined step(s)", stringBuilder.toString(), "The scenario has pending or undefined step(s)");
                currentDomElements.testMethodElement.appendChild(exception);
            } else {
                currentDomElements.testMethodElement.setAttribute("status", "SKIP");
            }
        } else {
            currentDomElements.testMethodElement.setAttribute("status", "PASS");
        }
    }

    private String calculateTotalDurationString(final CurrentFeature currentFeature) {
        long totalDurationNanos = 0;
        for (final TestStepResult r : currentFeature.testStepResults) {
            totalDurationNanos += r.result.getDuration() == null ? 0 : r.result.getDuration();
        }
        for (final Result r : currentFeature.hooks) {
            totalDurationNanos += r.getDuration() == null ? 0 : r.getDuration();
        }
        return String.valueOf(totalDurationNanos / 1000000);
    }

    private StringBuilder buildStepAndResultListing(final CurrentFeature currentFeature) {
        final StringBuilder sb = new StringBuilder();
        for (final TestStepResult stepResult : currentFeature.testStepResults) {
            int length = sb.length();
            sb.append(testSources.getKeywordFromSource(currentFeature.uri, stepResult.step.getStepLine()));
            sb.append(stepResult.step.getStepText());
            do {
                sb.append(".");
            } while (sb.length() - length < 76);
            sb.append(stepResult.result.getStatus().lowerCaseName());
            sb.append("\n");
        }
        return sb;
    }

    private Element createException(final Document doc, final String clazz, final String message, final String stacktrace) {
        final Element exceptionElement = doc.createElement("exception");
        exceptionElement.setAttribute("class", clazz);

        if (message != null) {
            final Element messageElement = doc.createElement("message");
            messageElement.appendChild(doc.createCDATASection(message));
            exceptionElement.appendChild(messageElement);
        }

        final Element stacktraceElement = doc.createElement("full-stacktrace");
        stacktraceElement.appendChild(doc.createCDATASection(stacktrace));
        exceptionElement.appendChild(stacktraceElement);

        return exceptionElement;
    }
    
    private void finishReport() {
        try {
            outputNgResultsElement.setAttribute("total", String.valueOf(getElementsCountByAttribute(outputSuiteElement, "status", ".*")));
            outputNgResultsElement.setAttribute("passed", String.valueOf(getElementsCountByAttribute(outputSuiteElement, "status", "PASS")));
            outputNgResultsElement.setAttribute("failed", String.valueOf(getElementsCountByAttribute(outputSuiteElement, "status", "FAIL")));
            outputNgResultsElement.setAttribute("skipped", String.valueOf(getElementsCountByAttribute(outputSuiteElement, "status", "SKIP")));
            outputSuiteElement.setAttribute("name", TestNGFormatter.class.getName());
            outputSuiteElement.setAttribute("duration-ms", getTotalDuration(outputSuiteElement.getElementsByTagName("test-method")));
            outputTestElement.setAttribute("name", TestNGFormatter.class.getName());
            outputTestElement.setAttribute("duration-ms", getTotalDuration(outputSuiteElement.getElementsByTagName("test-method")));

            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            final StreamResult streamResult = new StreamResult(writer);
            final DOMSource domSource = new DOMSource(outputDocument);
            transformer.transform(domSource, streamResult);
            closeQuietly(writer);
        } catch (TransformerException e) {
            throw new CucumberException("Error transforming report.", e);
        }
    }

    private int getElementsCountByAttribute(Node node, String attributeName, String attributeValue) {
        int count = 0;

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            count += getElementsCountByAttribute(node.getChildNodes().item(i), attributeName, attributeValue);
        }

        final NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            final Node namedItem = attributes.getNamedItem(attributeName);
            if (namedItem != null && namedItem.getNodeValue().matches(attributeValue)) {
                count++;
            }
        }

        return count;
    }

    private String getTotalDuration(NodeList testCaseNodes) {
        long totalDuration = 0;
        for (int i = 0; i < testCaseNodes.getLength(); i++) {
            try {
                final String duration = testCaseNodes.item(i).getAttributes().getNamedItem("duration-ms").getNodeValue();
                totalDuration += Long.parseLong(duration);
            } catch (NumberFormatException e) {
                throw new CucumberException(e);
            } catch (NullPointerException e) {
                throw new CucumberException(e);
            }
        }
        return String.valueOf(totalDuration);
    }

    private void closeQuietly(final Closeable out) {
        try {
            out.close();
        } catch (IOException ignored) {
            // go gentle into that good night
        }
    }

    private class CurrentDomElements {

        private final Document document;
        private final Element ngResultsElement;
        private final Element suiteElement;
        private final Element testElement;
        private Element classElement;
        private Element testMethodElement;

        CurrentDomElements() {
            try {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                ngResultsElement = document.createElement("testng-results");
                suiteElement = document.createElement("suite");
                testElement = document.createElement("test");
                suiteElement.appendChild(testElement);
                ngResultsElement.appendChild(suiteElement);
                document.appendChild(ngResultsElement);
            }
            catch (ParserConfigurationException e) {
                throw new CucumberException("Error initializing DocumentBuilder.", e);
            }
        }
    }

    private class CurrentFeature {
        private final String uri;
        private String previousTestCaseName = "";
        private int exampleNumber = 1;
        private final List<TestStepResult> testStepResults = new ArrayList<TestStepResult>();
        private final List<Result> hooks = new ArrayList<Result>();

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
