package cucumber.runtime.formatter;

import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.TestStep;
import cucumber.api.event.EmbedEvent;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestSourceRead;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.api.event.WriteEvent;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.NiceAppendable;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.URLOutputStream;
import gherkin.ast.Background;
import gherkin.ast.DataTable;
import gherkin.ast.DocString;
import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.Node;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import gherkin.ast.Tag;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import gherkin.pickles.PickleTag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HTMLFormatter implements Formatter {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String JS_FORMATTER_VAR = "formatter";
    private static final String JS_REPORT_FILENAME = "report.js";
    private static final String[] TEXT_ASSETS = new String[]{"/cucumber/formatter/formatter.js", "/cucumber/formatter/index.html", "/cucumber/formatter/jquery-1.8.2.min.js", "/cucumber/formatter/style.css"};
    private static final Map<String, String> MIME_TYPES_EXTENSIONS = new HashMap<String, String>() {
        {
            put("image/bmp", "bmp");
            put("image/gif", "gif");
            put("image/jpeg", "jpg");
            put("image/png", "png");
            put("image/svg+xml", "svg");
            put("video/ogg", "ogg");
        }
    };

    private final TestSourcesModel testSources = new TestSourcesModel();
    private final URL htmlReportDir;
    private NiceAppendable jsOut;

    private boolean firstFeature = true;
    private String currentFeatureFile;
    private Map<String, Object> currentTestCaseMap;
    private ScenarioOutline currentScenarioOutline;
    private Examples currentExamples;
    private int embeddedIndex;

    private EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
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
    private EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            handleTestStepStarted(event);
        }
    };
    private EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            handleTestStepFinished(event);
        }
    };
    private EventHandler<EmbedEvent> embedEventhandler = new EventHandler<EmbedEvent>() {
        @Override
        public void receive(EmbedEvent event) {
            handleEmbed(event);
        }
    };
    private EventHandler<WriteEvent> writeEventhandler = new EventHandler<WriteEvent>() {
        @Override
        public void receive(WriteEvent event) {
            handleWrite(event);
        }
    };
    private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            finishReport();
        }
    };

    public HTMLFormatter(URL htmlReportDir) {
        this.htmlReportDir = htmlReportDir;
    }

    public HTMLFormatter(URL htmlReportDir, NiceAppendable jsOut) {
        this(htmlReportDir);
        this.jsOut = jsOut;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, caseStartedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
        publisher.registerHandlerFor(EmbedEvent.class, embedEventhandler);
        publisher.registerHandlerFor(WriteEvent.class, writeEventhandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
    }

    private void handleTestSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.path, event);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        if (firstFeature) {
            jsOut().append("$(document).ready(function() {").append("var ")
                    .append(JS_FORMATTER_VAR).append(" = new CucumberHTML.DOMFormatter($('.cucumber-report'));");
            firstFeature = false;
        }
        handleStartOfFeature(event.testCase);
        handleScenarioOutline(event.testCase);
        currentTestCaseMap = createTestCase(event.testCase);
        if (testSources.hasBackground(currentFeatureFile, event.testCase.getLine())) {
            jsFunctionCall("background", createBackground(event.testCase));
        } else {
            jsFunctionCall("scenario", currentTestCaseMap);
            currentTestCaseMap = null;
        }
    }

    private void handleTestStepStarted(TestStepStarted event) {
        if (!event.testStep.isHook()) {
            if (isFirstStepAfterBackground(event.testStep)) {
                jsFunctionCall("scenario", currentTestCaseMap);
                currentTestCaseMap = null;
            }
            jsFunctionCall("step", createTestStep(event.testStep));
        }
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (!event.testStep.isHook()) {
            jsFunctionCall("match", createMatchMap(event.testStep, event.result));
            jsFunctionCall("result", createResultMap(event.result));
        } else {
            jsFunctionCall(event.testStep.getHookType().toString(), createResultMap(event.result));
        }
    }

    private void handleEmbed(EmbedEvent event) {
        String mimeType = event.mimeType;
        if(mimeType.startsWith("text/")) {
            // just pass straight to the formatter to output in the html
            jsFunctionCall("embedding", mimeType, new String(event.data));
        } else {
            // Creating a file instead of using data urls to not clutter the js file
            String extension = MIME_TYPES_EXTENSIONS.get(mimeType);
            if (extension != null) {
                StringBuilder fileName = new StringBuilder("embedded").append(embeddedIndex++).append(".").append(extension);
                writeBytesAndClose(event.data, reportFileOutputStream(fileName.toString()));
                jsFunctionCall("embedding", mimeType, fileName);
            }
        }
    }

    private void handleWrite(WriteEvent event) {
        jsFunctionCall("write", event.text);
    }

    private void finishReport() {
        if (!firstFeature) {
            jsOut().append("});");
            copyReportFiles();
        }
        jsOut().close();
    }

    private void handleStartOfFeature(TestCase testCase) {
        if (currentFeatureFile == null || !currentFeatureFile.equals(testCase.getPath())) {
            currentFeatureFile = testCase.getPath();
            jsFunctionCall("uri", currentFeatureFile);
            jsFunctionCall("feature", createFeature(testCase));
        }
    }

    private Map<String, Object> createFeature(TestCase testCase) {
        Map<String, Object> featureMap = new HashMap<String, Object>();
        Feature feature = testSources.getFeature(testCase.getPath());
        if (feature != null) {
            featureMap.put("keyword", feature.getKeyword());
            featureMap.put("name", feature.getName());
            featureMap.put("description", feature.getDescription() != null ? feature.getDescription() : "");
            if (!feature.getTags().isEmpty()) {
                featureMap.put("tags", createTagList(feature.getTags()));
            }
        }
        return featureMap;
    }

    private List<Map<String, Object>> createTagList(List<Tag> tags) {
        List<Map<String, Object>> tagList = new ArrayList<Map<String, Object>>();
        for (Tag tag : tags) {
            Map<String, Object> tagMap = new HashMap<String, Object>();
            tagMap.put("name", tag.getName());
            tagList.add(tagMap);
        }
        return tagList;
    }

    private void handleScenarioOutline(TestCase testCase) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
        if (TestSourcesModel.isScenarioOutlineScenario(astNode)) {
            ScenarioOutline scenarioOutline = (ScenarioOutline)TestSourcesModel.getScenarioDefinition(astNode);
            if (currentScenarioOutline == null || !currentScenarioOutline.equals(scenarioOutline)) {
                currentScenarioOutline = scenarioOutline;
                jsFunctionCall("scenarioOutline", createScenarioOutline(currentScenarioOutline));
                addOutlineStepsToReport(scenarioOutline);
            }
            Examples examples = (Examples)astNode.parent.node;
            if (currentExamples == null || !currentExamples.equals(examples)) {
                currentExamples = examples;
                jsFunctionCall("examples", createExamples(currentExamples));
            }
        } else {
            currentScenarioOutline = null;
            currentExamples = null;
        }
    }

    private Map<String, Object> createScenarioOutline(ScenarioOutline scenarioOutline) {
        Map<String, Object> scenarioOutlineMap = new HashMap<String, Object>();
        scenarioOutlineMap.put("name", scenarioOutline.getName());
        scenarioOutlineMap.put("keyword", scenarioOutline.getKeyword());
        scenarioOutlineMap.put("description", scenarioOutline.getDescription() != null ? scenarioOutline.getDescription() : "");
        if (!scenarioOutline.getTags().isEmpty()) {
            scenarioOutlineMap.put("tags", createTagList(scenarioOutline.getTags()));
        }
        return scenarioOutlineMap;
    }

    private void addOutlineStepsToReport(ScenarioOutline scenarioOutline) {
        for (Step step : scenarioOutline.getSteps()) {
            Map<String, Object> stepMap = new HashMap<String, Object>();
            stepMap.put("name", step.getText());
            stepMap.put("keyword", step.getKeyword());
            if (step.getArgument() != null) {
                Node argument = step.getArgument();
                if (argument instanceof DocString) {
                    stepMap.put("doc_string", createDocStringMap((DocString)argument));
                } else if (argument instanceof DataTable) {
                    stepMap.put("rows", createDataTableList((DataTable)argument));
                }
            }
            jsFunctionCall("step", stepMap);
        }
    }

    private Map<String, Object> createDocStringMap(DocString docString) {
        Map<String, Object> docStringMap = new HashMap<String, Object>();
        docStringMap.put("value", docString.getContent());
        return docStringMap;
    }

    private List<Map<String, Object>> createDataTableList(DataTable dataTable) {
        List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();
        for (TableRow row : dataTable.getRows()) {
            rowList.add(createRowMap(row));
        }
        return rowList;
    }

    private Map<String, Object> createRowMap(TableRow row) {
        Map<String, Object> rowMap = new HashMap<String, Object>();
        rowMap.put("cells", createCellList(row));
        return rowMap;
    }

    private List<String> createCellList(TableRow row) {
        List<String> cells = new ArrayList<String>();
        for (TableCell cell : row.getCells()) {
            cells.add(cell.getValue());
        }
        return cells;
    }

    private Map<String, Object> createExamples(Examples examples) {
        Map<String, Object> examplesMap = new HashMap<String, Object>();
        examplesMap.put("name", examples.getName());
        examplesMap.put("keyword", examples.getKeyword());
        examplesMap.put("description", examples.getDescription() != null ? examples.getDescription() : "");
        List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();
        rowList.add(createRowMap(examples.getTableHeader()));
        for (TableRow row : examples.getTableBody()) {
            rowList.add(createRowMap(row));
        }
        examplesMap.put("rows", rowList);
        if (!examples.getTags().isEmpty()) {
            examplesMap.put("tags", createTagList(examples.getTags()));
        }
        return examplesMap;
    }

    private Map<String, Object> createTestCase(TestCase testCase) {
        Map<String, Object> testCaseMap = new HashMap<String, Object>();
        testCaseMap.put("name", testCase.getName());
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
        if (astNode != null) {
            ScenarioDefinition scenarioDefinition = TestSourcesModel.getScenarioDefinition(astNode);
            testCaseMap.put("keyword", scenarioDefinition.getKeyword());
            testCaseMap.put("description", scenarioDefinition.getDescription() != null ? scenarioDefinition.getDescription() : "");
        }
        if (!testCase.getTags().isEmpty()) {
            List<Map<String, Object>> tagList = new ArrayList<Map<String, Object>>();
            for (PickleTag tag : testCase.getTags()) {
                Map<String, Object> tagMap = new HashMap<String, Object>();
                tagMap.put("name", tag.getName());
                tagList.add(tagMap);
            }
            testCaseMap.put("tags", tagList);
        }
        return testCaseMap;
    }

    private Map<String, Object> createBackground(TestCase testCase) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
        if (astNode != null) {
            Background background = TestSourcesModel.getBackgoundForTestCase(astNode);
            Map<String, Object> testCaseMap = new HashMap<String, Object>();
            testCaseMap.put("name", background.getName());
            testCaseMap.put("keyword", background.getKeyword());
            testCaseMap.put("description", background.getDescription() != null ? background.getDescription() : "");
            return testCaseMap;
        }
        return null;
    }

    private boolean isFirstStepAfterBackground(TestStep testStep) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
        if (astNode != null) {
            if (currentTestCaseMap != null && !TestSourcesModel.isBackgroundStep(astNode)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> createTestStep(TestStep testStep) {
        Map<String, Object> stepMap = new HashMap<String, Object>();
        stepMap.put("name", testStep.getStepText());
        if (!testStep.getStepArgument().isEmpty()) {
            Argument argument = testStep.getStepArgument().get(0);
            if (argument instanceof PickleString) {
                stepMap.put("doc_string", createDocStringMap((PickleString)argument));
            } else if (argument instanceof PickleTable) {
                stepMap.put("rows", createDataTableList((PickleTable)argument));
            }
        }
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
        if (astNode != null) {
            Step step = (Step) astNode.node;
            stepMap.put("keyword", step.getKeyword());
        }

        return stepMap;
    }

    private Map<String, Object> createDocStringMap(PickleString docString) {
        Map<String, Object> docStringMap = new HashMap<String, Object>();
        docStringMap.put("value", docString.getContent());
        return docStringMap;
    }

    private List<Map<String, Object>> createDataTableList(PickleTable dataTable) {
        List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();
        for (PickleRow row : dataTable.getRows()) {
            rowList.add(createRowMap(row));
        }
        return rowList;
    }

    private Map<String, Object> createRowMap(PickleRow row) {
        Map<String, Object> rowMap = new HashMap<String, Object>();
        rowMap.put("cells", createCellList(row));
        return rowMap;
    }

    private List<String> createCellList(PickleRow row) {
        List<String> cells = new ArrayList<String>();
        for (PickleCell cell : row.getCells()) {
            cells.add(cell.getValue());
        }
        return cells;
    }

    private Map<String, Object> createMatchMap(TestStep testStep, Result result) {
        Map<String, Object> matchMap = new HashMap<String, Object>();
        if (!result.is(Result.Type.UNDEFINED)) {
            matchMap.put("location", testStep.getCodeLocation());
        }
        return matchMap;
    }

    private Map<String, Object> createResultMap(Result result) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("status", result.getStatus().lowerCaseName());
        if (result.getErrorMessage() != null) {
            resultMap.put("error_message", result.getErrorMessage());
        }
        return resultMap;
    }

    private void jsFunctionCall(String functionName, Object... args) {
        NiceAppendable out = jsOut().append(JS_FORMATTER_VAR + ".").append(functionName).append("(");
        boolean comma = false;
        for (Object arg : args) {
            if (comma) {
                out.append(", ");
            }
            String stringArg = gson.toJson(arg);
            out.append(stringArg);
            comma = true;
        }
        out.append(");").println();
    }

    private void copyReportFiles() {
        if (htmlReportDir == null) {
            return;
        }
        for (String textAsset : TEXT_ASSETS) {
            InputStream textAssetStream = getClass().getResourceAsStream(textAsset);
            if (textAssetStream == null) {
                throw new CucumberException("Couldn't find " + textAsset + ". Is cucumber-html on your classpath? Make sure you have the right version.");
            }
            String baseName = new File(textAsset).getName();
            writeStreamAndClose(textAssetStream, reportFileOutputStream(baseName));
        }
    }

    private void writeStreamAndClose(InputStream in, OutputStream out) {
        byte[] buffer = new byte[16 * 1024];
        try {
            int len = in.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
            out.close();
        } catch (IOException e) {
            throw new CucumberException("Unable to write to report file item: ", e);
        }
    }

    private void writeBytesAndClose(byte[] buf, OutputStream out) {
        try {
            out.write(buf);
        } catch (IOException e) {
            throw new CucumberException("Unable to write to report file item: ", e);
        }
    }

    private NiceAppendable jsOut() {
        if (jsOut == null) {
            try {
                jsOut = new NiceAppendable(new OutputStreamWriter(reportFileOutputStream(JS_REPORT_FILENAME), "UTF-8"));
            } catch (IOException e) {
                throw new CucumberException(e);
            }
        }
        return jsOut;
    }

    private OutputStream reportFileOutputStream(String fileName) {
        try {
            return new URLOutputStream(new URL(htmlReportDir, fileName));
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

}