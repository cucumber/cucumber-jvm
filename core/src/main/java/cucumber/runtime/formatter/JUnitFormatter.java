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

class JUnitFormatter implements Formatter, Reporter {
    private final Writer out;
    private final Document doc;
    private final Element rootElement;

    private TestCase testCase;
    private Element root;

    public JUnitFormatter(URL out) throws IOException {
        this.out = new UTF8OutputStreamWriter(new URLOutputStream(out));
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
    }

    @Override
    public void background(Background background) {
        testCase = new TestCase();
        root = testCase.createElement(doc);
    }

    @Override
    public void scenario(Scenario scenario) {
        if (testCase != null && testCase.scenario == null) {
            testCase.scenario = scenario;
        } else {
            testCase = new TestCase(scenario);
            root = testCase.createElement(doc);
        }
        testCase.writeElement(doc, root);
        rootElement.appendChild(root);

        increaseAttributeValue(rootElement, "tests");
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
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(out);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
        } catch (TransformerException e) {
            new CucumberException("Error while transforming.", e);
        }
    }

    @Override
    public void result(Result result) {
        testCase.results.add(result);

        testCase.updateElement(doc, root);
    }

    @Override
    public void before(Match match, Result result) {
        handleHook(result);
    }

    @Override
    public void after(Match match, Result result) {
        handleHook(result);
    }

    private void handleHook(Result result) {
        if (result.getStatus().equals(Result.FAILED)) {
            testCase.results.add(result);
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
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }

    @Override
    public void examples(Examples examples) {
        TestCase.examples = examples.getRows().size() - 1;
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
        static int examples = 0;
        final List<Step> steps = new ArrayList<Step>();
        final List<Result> results = new ArrayList<Result>();

        private Element createElement(Document doc) {
            return doc.createElement("testcase");
        }

        private void writeElement(Document doc, Element tc) {
            tc.setAttribute("classname", feature.getName());
            tc.setAttribute("name", examples > 0 ? scenario.getName() + "_" + examples-- : scenario.getName());
        }

        public void updateElement(Document doc, Element tc) {
            long totalDurationNanos = 0;
            for (Result r : results) {
                totalDurationNanos += r.getDuration() == null ? 0 : r.getDuration();
            }

            double totalDurationSeconds = ((double) totalDurationNanos) / 1000000000;
            String time = NUMBER_FORMAT.format(totalDurationSeconds);
            tc.setAttribute("time", time);

            StringBuilder sb = new StringBuilder();
            Result skipped = null, failed = null;
            for (int i = 0; i < steps.size(); i++) {
                int length = sb.length();
                Result result = results.get(i);
                if ("failed".equals(result.getStatus())) failed = result;
                if ("undefined".equals(result.getStatus()) || "pending".equals(result.getStatus())) skipped = result;
                sb.append(steps.get(i).getKeyword());
                sb.append(steps.get(i).getName());
                for (int j = 0; sb.length() - length + j < 140; j++) sb.append(".");
                sb.append(result.getStatus());
                sb.append("\n");
            }
            Element child;
            if (failed != null) {
                sb.append("\nStackTrace:\n");
                StringWriter sw = new StringWriter();
                failed.getError().printStackTrace(new PrintWriter(sw));
                sb.append(sw.toString());
                child = doc.createElement("failure");
                child.setAttribute("message", failed.getErrorMessage());
                child.appendChild(doc.createCDATASection(sb.toString()));
            } else if (skipped != null) {
                child = doc.createElement("skipped");
                child.appendChild(doc.createCDATASection(sb.toString()));
            } else {
                child = doc.createElement("system-out");
                child.appendChild(doc.createCDATASection(sb.toString()));
            }

            Node existingChild = tc.getFirstChild();
            if (existingChild == null) {
                tc.appendChild(child);
            } else {
                tc.replaceChild(child, existingChild);
            }
        }

    }

}
