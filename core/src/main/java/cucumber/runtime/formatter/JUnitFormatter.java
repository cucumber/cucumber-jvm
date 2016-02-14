package cucumber.runtime.formatter;

import cucumber.runtime.CucumberException;
import cucumber.runtime.io.URLOutputStream;
import cucumber.runtime.io.UTF8OutputStreamWriter;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

class JUnitFormatter implements Formatter, Reporter, StrictAware {
    private final Writer out;
    private final Document doc;
    private final Element rootElement;

    private TestCase testCase;
    private Element root;

    public JUnitFormatter(URL out) throws IOException {
        this.out = new UTF8OutputStreamWriter(new URLOutputStream(out));
        TestCase.treatSkippedAsFailure = false;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            rootElement = doc.createElement("testsuite");
            doc.appendChild(rootElement);
        } catch (ParserConfigurationException e) {
            throw new CucumberException("Error while processing unit report", e);
        }
    }

    @Override
    public void feature(Feature feature) {
        TestCase.feature = feature;
        TestCase.previousScenarioOutlineName = "";
        TestCase.exampleNumber = 1;
    }

    @Override
    public void background(Background background) {
        if (!isCurrentTestCaseCreatedNameless()) {
            testCase = new TestCase();
            root = testCase.createElement(doc);
        }
    }

    @Override
    public void scenario(Scenario scenario) {
        if (isCurrentTestCaseCreatedNameless()) {
            testCase.scenario = scenario;
        } else {
            testCase = new TestCase(scenario);
            root = testCase.createElement(doc);
        }
        testCase.writeElement(doc, root);
        rootElement.appendChild(root);

        increaseAttributeValue(rootElement, "tests");
    }

    private boolean isCurrentTestCaseCreatedNameless() {
        return testCase != null && testCase.scenario == null;
    }

    @Override
    public void step(Step step) {
        if (testCase != null) testCase.steps.add(step);
    }

    @Override
    public void done() {
        try {
            // set up a transformer
            rootElement.setAttribute("name", JUnitFormatter.class.getName());
            rootElement.setAttribute("failures", String.valueOf(rootElement.getElementsByTagName("failure").getLength()));
            rootElement.setAttribute("skipped", String.valueOf(rootElement.getElementsByTagName("skipped").getLength()));
            rootElement.setAttribute("time", sumTimes(rootElement.getElementsByTagName("testcase")));
            if (rootElement.getElementsByTagName("testcase").getLength() == 0) {
                addDummyTestCase(); // to avoid failed Jenkins jobs
            }
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(out);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
        } catch (TransformerException e) {
            throw new CucumberException("Error while transforming.", e);
        }
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        // NoOp
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        if (testCase != null && testCase.steps.isEmpty()) {
            testCase.handleEmptyTestCase(doc, root);
        }
    }

    private void addDummyTestCase() {
        Element dummy = doc.createElement("testcase");
        dummy.setAttribute("classname", "dummy");
        dummy.setAttribute("name", "dummy");
        rootElement.appendChild(dummy);
        Element skipped = doc.createElement("skipped");
        skipped.setAttribute("message", "No features found");
        dummy.appendChild(skipped);
    }

    @Override
    public void result(Result result) {
        testCase.results.add(result);
        testCase.updateElement(doc, root);
    }

    @Override
    public void before(Match match, Result result) {
        if (!isCurrentTestCaseCreatedNameless()) {
            testCase = new TestCase();
            root = testCase.createElement(doc);
        }
        handleHook(result);
    }

    @Override
    public void after(Match match, Result result) {
        handleHook(result);
    }

    private void handleHook(Result result) {
        testCase.hookResults.add(result);
        testCase.updateElement(doc, root);
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
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        testCase = null;
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void match(Match match) {
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
    }

    @Override
    public void write(String text) {
    }

    @Override
    public void uri(String uri) {
    }

    @Override
    public void close() {
    }

    @Override
    public void eof() {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }

    @Override
    public void setStrict(boolean strict) {
        TestCase.treatSkippedAsFailure = strict;
    }

    private static class TestCase {
        private static final DecimalFormat NUMBER_FORMAT = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);

        static {
            NUMBER_FORMAT.applyPattern("0.######");
        }

        private TestCase(Scenario scenario) {
            this.scenario = scenario;
        }

        private TestCase() {
        }

        Scenario scenario;
        static Feature feature;
        static String previousScenarioOutlineName;
        static int exampleNumber;
        static boolean treatSkippedAsFailure = false;
        final List<Step> steps = new ArrayList<Step>();
        final List<Result> results = new ArrayList<Result>();
        final List<Result> hookResults = new ArrayList<Result>();

        private Element createElement(Document doc) {
            return doc.createElement("testcase");
        }

        private void writeElement(Document doc, Element tc) {
            tc.setAttribute("classname", feature.getName());
            tc.setAttribute("name", calculateElementName(scenario));
        }

        private String calculateElementName(Scenario scenario) {
            String scenarioName = scenario.getName();
            if (scenario.getKeyword().equals("Scenario Outline") && scenarioName.equals(previousScenarioOutlineName)) {
                return scenarioName + (includesBlank(scenarioName) ? " " : "_") + ++exampleNumber;
            } else {
                previousScenarioOutlineName = scenario.getKeyword().equals("Scenario Outline") ? scenarioName : "";
                exampleNumber = 1;
                return scenarioName;
            }
        }

        private boolean includesBlank(String scenarioName) {
            return scenarioName.indexOf(' ') != -1;
        }

        public void updateElement(Document doc, Element tc) {
            tc.setAttribute("time", calculateTotalDurationString());

            StringBuilder sb = new StringBuilder();
            addStepAndResultListing(sb);
            Result skipped = null, failed = null;
            for (Result result : results) {
                if ("failed".equals(result.getStatus())) failed = result;
                if ("undefined".equals(result.getStatus()) || "pending".equals(result.getStatus())) skipped = result;
            }
            for (Result result : hookResults) {
                if (failed == null && "failed".equals(result.getStatus())) failed = result;
                if (skipped == null && "pending".equals(result.getStatus())) skipped = result;
            }
            Element child;
            if (failed != null) {
                addStackTrace(sb, failed);
                child = createElementWithMessage(doc, sb, "failure", failed.getErrorMessage());
            } else if (skipped != null) {
                if (treatSkippedAsFailure) {
                    child = createElementWithMessage(doc, sb, "failure", "The scenario has pending or undefined step(s)");
                }
                else {
                    child = createElement(doc, sb, "skipped");
                }
            } else {
                child = createElement(doc, sb, "system-out");
            }

            Node existingChild = tc.getFirstChild();
            if (existingChild == null) {
                tc.appendChild(child);
            } else {
                tc.replaceChild(child, existingChild);
            }
        }

        public void handleEmptyTestCase(Document doc, Element tc) {
            tc.setAttribute("time", calculateTotalDurationString());

            String resultType = treatSkippedAsFailure ? "failure" : "skipped";
            Element child = createElementWithMessage(doc, new StringBuilder(), resultType, "The scenario has no steps");

            tc.appendChild(child);
        }

        private String calculateTotalDurationString() {
            long totalDurationNanos = 0;
            for (Result r : results) {
                totalDurationNanos += r.getDuration() == null ? 0 : r.getDuration();
            }
            for (Result r : hookResults) {
                totalDurationNanos += r.getDuration() == null ? 0 : r.getDuration();
            }
            double totalDurationSeconds = ((double) totalDurationNanos) / 1000000000;
            return NUMBER_FORMAT.format(totalDurationSeconds);
        }

        private void addStepAndResultListing(StringBuilder sb) {
            for (int i = 0; i < steps.size(); i++) {
                int length = sb.length();
                String resultStatus = "not executed";
                if (i < results.size()) {
                    resultStatus = results.get(i).getStatus();
                }
                sb.append(steps.get(i).getKeyword());
                sb.append(steps.get(i).getName());
                do {
                  sb.append(".");
                } while (sb.length() - length < 76);
                sb.append(resultStatus);
                sb.append("\n");
            }
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
            child.appendChild(doc.createCDATASection(sb.toString()));
            return child;
        }

    }

}
