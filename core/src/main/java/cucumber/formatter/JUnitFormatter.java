package cucumber.formatter;

import cucumber.runtime.CucumberException;
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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class JUnitFormatter implements Formatter, Reporter {
    private final File out;
    private final Document doc;
    private final Element rootElement;

    private TestCase testCase;

    public JUnitFormatter(File out) {
        this.out = out;
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
    }

    @Override
    public void scenario(Scenario scenario) {
        if (testCase != null) {
            testCase.scenario = scenario;
        } else {
            testCase = new TestCase(scenario);
        }

        increaseAttributeValue(rootElement, "tests");
    }

    @Override
    public void step(Step step) {
        if (testCase != null) testCase.steps.add(step);
    }

    @Override
    public void done() {
        try {
            //set up a transformer
            rootElement.setAttribute("failed", String.valueOf(rootElement.getElementsByTagName("failure").getLength()));
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

        if (testCase.scenario != null && testCase.results.size() == testCase.steps.size()) {
            rootElement.appendChild(testCase.writeTo(doc));
            testCase = null;
        }
    }

    @Override
    public void before(Match match, Result result) {
        handleHook(match, result);
    }

    @Override
    public void after(Match match, Result result) {
        handleHook(match, result);
    }

    private void handleHook(Match match, Result result) {
        if (result.getStatus().equals(Result.FAILED)) {
            testCase.results.add(result);
        }

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
    public void embedding(String mimeType, InputStream data) {
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
        private TestCase(Scenario scenario) {
            this.scenario = scenario;
        }

        private TestCase() {
        }

        Scenario scenario;
        static Feature feature;
        static int examples = 0;
        List<Step> steps = new ArrayList<Step>();
        List<Result> results = new ArrayList<Result>();

        private Element writeTo(Document doc) {
            Element tc = doc.createElement("testcase");
            tc.setAttribute("classname", feature.getName());
            tc.setAttribute("name", examples > 0 ? scenario.getName() + "_" + examples-- : scenario.getName());
            long time = 0;
            for (Result r : results) {
                long durationMillis = r.getDuration() == null ? 0 : r.getDuration() / 1000000; // duration is reported in nanos
                time += durationMillis;
            }
            tc.setAttribute("time", String.valueOf(time));

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
            tc.appendChild(child);
            return tc;
        }
    }

}
