package cucumber.runtime.formatter;

import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestSourceRead;
import cucumber.api.event.TestStepFinished;
import cucumber.api.formatter.StrictAware;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;
import cucumber.runtime.io.URLOutputStream;
import cucumber.runtime.io.UTF8OutputStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class JUnitFormatter implements EventListener, StrictAware {
    private final Writer out;
    private final Document doc;
    private final Element rootElement;

    private TestCase testCase;
    private Element root;

    private EventHandler<TestSourceRead> sourceReadHandler= new EventHandler<TestSourceRead>() {
        @Override
        public void receive(TestSourceRead event) {
            handleTestSourceRead(event);
        }
    };
    private EventHandler<TestCaseStarted> caseStartedHandler= new EventHandler<TestCaseStarted>() {
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
        TestCase.treatConditionallySkippedAsFailure = false;
        TestCase.currentFeatureFile = null;
        TestCase.previousTestCaseName = "";
        TestCase.exampleNumber = 1;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            rootElement = doc.createElement("testsuite");
            doc.appendChild(rootElement);
        } catch (ParserConfigurationException e) {
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

    private void handleTestSourceRead(TestSourceRead event) {
        TestCase.testSources.addTestSourceReadEvent(event.uri, event);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        if (TestCase.currentFeatureFile == null || !TestCase.currentFeatureFile.equals(event.testCase.getUri())) {
            TestCase.currentFeatureFile = event.testCase.getUri();
            TestCase.previousTestCaseName = "";
            TestCase.exampleNumber = 1;
        }
        testCase = new TestCase(event.testCase);
        root = testCase.createElement(doc);
        testCase.writeElement(doc, root);
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
            testCase.handleEmptyTestCase(doc, root, event.result);
        } else {
            testCase.addTestCaseElement(doc, root, event.result);
        }
    }

    private void finishReport() {
        try {
            // set up a transformer
            rootElement.setAttribute("name", JUnitFormatter.class.getName());
            rootElement.setAttribute("failures", String.valueOf(rootElement.getElementsByTagName("failure").getLength()));
            rootElement.setAttribute("skipped", String.valueOf(rootElement.getElementsByTagName("skipped").getLength()));
            rootElement.setAttribute("time", sumTimes(rootElement.getElementsByTagName("testcase")));
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(out);
            DOMSource source = new DOMSource(doc);
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

    @Override
    public void setStrict(boolean strict) {
        TestCase.treatConditionallySkippedAsFailure = strict;
    }

    private static class TestCase {
        private static final DecimalFormat NUMBER_FORMAT = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        private static final TestSourcesModel testSources = new TestSourcesModel();

        static {
            NUMBER_FORMAT.applyPattern("0.######");
        }

        private TestCase(cucumber.api.TestCase testCase) {
            this.testCase = testCase;
        }

        static String currentFeatureFile;
        static String previousTestCaseName;
        static int exampleNumber;
        static boolean treatConditionallySkippedAsFailure = false;
        final List<PickleStepTestStep> steps = new ArrayList<PickleStepTestStep>();
        final List<Result> results = new ArrayList<Result>();
        private final cucumber.api.TestCase testCase;

        private Element createElement(Document doc) {
            return doc.createElement("testcase");
        }

        private void writeElement(Document doc, Element tc) {
            tc.setAttribute("classname", testSources.getFeatureName(currentFeatureFile));
            tc.setAttribute("name", calculateElementName(testCase));
        }

        private String calculateElementName(cucumber.api.TestCase testCase) {
            String testCaseName = testCase.getName();
            if (testCaseName.equals(previousTestCaseName)) {
                return Utils.getUniqueTestNameForScenarioExample(testCaseName, ++exampleNumber);
            } else {
                previousTestCaseName = testCase.getName();
                exampleNumber = 1;
                return testCaseName;
            }
        }

        public void addTestCaseElement(Document doc, Element tc, Result result) {
            tc.setAttribute("time", calculateTotalDurationString(result));

            StringBuilder sb = new StringBuilder();
            addStepAndResultListing(sb);
            Element child;
            if (result.is(Result.Type.FAILED)) {
                addStackTrace(sb, result);
                child = createElementWithMessage(doc, sb, "failure", result.getErrorMessage());
            } else if (result.is(Result.Type.AMBIGUOUS)) {
                addStackTrace(sb, result);
                child = createElementWithMessage(doc, sb, "failure", result.getErrorMessage());
            } else if (result.is(Result.Type.PENDING) || result.is(Result.Type.UNDEFINED)) {
                if (treatConditionallySkippedAsFailure) {
                    child = createElementWithMessage(doc, sb, "failure", "The scenario has pending or undefined step(s)");
                }
                else {
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

        public void handleEmptyTestCase(Document doc, Element tc, Result result) {
            tc.setAttribute("time", calculateTotalDurationString(result));

            String resultType = treatConditionallySkippedAsFailure ? "failure" : "skipped";
            Element child = createElementWithMessage(doc, new StringBuilder(), resultType, "The scenario has no steps");

            tc.appendChild(child);
        }

        private String calculateTotalDurationString(Result result) {
            return NUMBER_FORMAT.format(((double) result.getDuration()) / 1000000000);
        }

        private void addStepAndResultListing(StringBuilder sb) {
            for (int i = 0; i < steps.size(); i++) {
                int length = sb.length();
                String resultStatus = "not executed";
                if (i < results.size()) {
                    resultStatus = results.get(i).getStatus().lowerCaseName();
                }
                sb.append(getKeywordFromSource(steps.get(i).getStepLine()) + steps.get(i).getStepText());
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
            // TODO Java 7 PR #1147: Inlined System.lineSeparator()
            String systemLineSeperator = System.getProperty("line.separator");
            child.appendChild(doc.createCDATASection(sb.toString().replace(systemLineSeperator, "\n")));
            return child;
        }

    }

    private static void closeQuietly(Closeable out) {
        try {
            out.close();
        } catch (IOException ignored) {
            // go gentle into that good night
        }
    }
}
