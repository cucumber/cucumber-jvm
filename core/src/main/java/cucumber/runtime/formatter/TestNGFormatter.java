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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
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

import static java.util.concurrent.TimeUnit.NANOSECONDS;

final class TestNGFormatter implements EventListener, StrictAware {

    private final Writer writer;
    private final Document document;
    private final Element results;
    private final Element suite;
    private final Element test;
    private final TestSourcesModel testSources = new TestSourcesModel();
    private Element clazz;
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
            handleTestCaseFinished();
        }
    };
    private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            finishReport();
        }
    };

    @SuppressWarnings("WeakerAccess") // Used by plugin factory
    public TestNGFormatter(URL url) throws IOException {
        this.writer = new UTF8OutputStreamWriter(new URLOutputStream(url));
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
            clazz = document.createElement("class");
            clazz.setAttribute("name", testSources.getFeature(event.testCase.getUri()).getName());
            test.appendChild(clazz);
        }
        root = document.createElement("test-method");
        clazz.appendChild(root);
        testCase = new TestCase(event.testCase);
        testCase.start(root);
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.testStep instanceof PickleStepTestStep) {
            testCase.steps.add((PickleStepTestStep) event.testStep);
            testCase.results.add(event.result);
        } else {
            testCase.hooks.add(event.result);
        }
    }

    private void handleTestCaseFinished() {
        testCase.finish(document, root);
    }

    private void finishReport() {
        try {
            results.setAttribute("total", String.valueOf(getElementsCountByAttribute(suite, "status", ".*")));
            results.setAttribute("passed", String.valueOf(getElementsCountByAttribute(suite, "status", "PASS")));
            results.setAttribute("failed", String.valueOf(getElementsCountByAttribute(suite, "status", "FAIL")));
            results.setAttribute("skipped", String.valueOf(getElementsCountByAttribute(suite, "status", "SKIP")));
            suite.setAttribute("name", TestNGFormatter.class.getName());
            suite.setAttribute("duration-ms", getTotalDuration(suite.getElementsByTagName("test-method")));
            test.setAttribute("name", TestNGFormatter.class.getName());
            test.setAttribute("duration-ms", getTotalDuration(suite.getElementsByTagName("test-method")));

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

    private int getElementsCountByAttribute(Node node, String attributeName, String attributeValue) {
        int count = 0;

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            count += getElementsCountByAttribute(node.getChildNodes().item(i), attributeName, attributeValue);
        }

        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            Node namedItem = attributes.getNamedItem(attributeName);
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
                String duration = testCaseNodes.item(i).getAttributes().getNamedItem("duration-ms").getNodeValue();
                totalDuration += Long.parseLong(duration);
            } catch (NumberFormatException | NullPointerException e) {
                throw new CucumberException(e);
            }
        }
        return String.valueOf(totalDuration);
    }

    final class TestCase {

        private final List<PickleStepTestStep> steps = new ArrayList<>();
        private final List<Result> results = new ArrayList<>();
        private final List<Result> hooks = new ArrayList<>();
        private final cucumber.api.TestCase testCase;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        TestCase(cucumber.api.TestCase testCase) {
            this.testCase = testCase;
        }

        private void start(Element element) {
            element.setAttribute("name", calculateElementName(testCase));
            element.setAttribute("started-at", dateFormat.format(new Date()));
        }

        private String calculateElementName(cucumber.api.TestCase testCase) {
            String testCaseName = testCase.getName();
            if (testCaseName.equals(previousTestCaseName)) {
                return testCaseName + "_" + ++exampleNumber;
            } else {
                previousTestCaseName = testCaseName;
                exampleNumber = 1;
                return testCaseName;
            }
        }

        void finish(Document doc, Element element) {
            element.setAttribute("duration-ms", calculateTotalDurationString());
            element.setAttribute("finished-at", dateFormat.format(new Date()));
            StringBuilder stringBuilder = new StringBuilder();
            addStepAndResultListing(stringBuilder);
            Result skipped = null;
            Result failed = null;
            for (Result result : results) {
                if (result.is(Result.Type.FAILED) || result.is(Result.Type.AMBIGUOUS)) {
                    failed = result;
                }
                if (result.is(Result.Type.UNDEFINED) || result.is(Result.Type.PENDING)) {
                    skipped = result;
                }
            }
            for (Result result : hooks) {
                if (failed == null && result.is(Result.Type.FAILED)) {
                    failed = result;
                }
            }
            if (failed != null) {
                element.setAttribute("status", "FAIL");
                StringWriter stringWriter = new StringWriter();
                failed.getError().printStackTrace(new PrintWriter(stringWriter));
                Element exception = createException(doc, failed.getError().getClass().getName(), stringBuilder.toString(), stringWriter.toString());
                element.appendChild(exception);
            } else if (skipped != null) {
                if (strict) {
                    element.setAttribute("status", "FAIL");
                    Element exception = createException(doc, "The scenario has pending or undefined step(s)", stringBuilder.toString(), "The scenario has pending or undefined step(s)");
                    element.appendChild(exception);
                } else {
                    element.setAttribute("status", "SKIP");
                }
            } else {
                element.setAttribute("status", "PASS");
            }
        }

        private String calculateTotalDurationString() {
            long totalDurationNanos = 0;
            for (Result r : results) {
                totalDurationNanos += r.getDuration();
            }
            for (Result r : hooks) {
                totalDurationNanos += r.getDuration();
            }
            return String.valueOf(NANOSECONDS.toMillis(totalDurationNanos));
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
