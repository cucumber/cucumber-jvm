package io.cucumber.core.plugin;

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
import io.cucumber.core.exception.CucumberException;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.DocStringArgument;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.WriteEvent;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;

public final class HTMLFormatter implements EventListener {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String JS_FORMATTER_VAR = "formatter";
    private static final String JS_REPORT_FILENAME = "report.js";
    private static final String[] TEXT_ASSETS = new String[]{
        "/io/cucumber/core/plugin/html/formatter.js",
        "/io/cucumber/core/plugin/html/index.html",
        "/io/cucumber/core/plugin/html/jquery-3.4.1.min.js",
        "/io/cucumber/core/plugin/html/style.css"
    };
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
    private final NiceAppendable jsOut;

    private boolean firstFeature = true;
    private URI currentFeatureFile;
    private Map<String, Object> currentTestCaseMap;
    private ScenarioOutline currentScenarioOutline;
    private Examples currentExamples;
    private int embeddedIndex;

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public HTMLFormatter(URL htmlReportDir) {
        this(htmlReportDir, createJsOut(htmlReportDir));
    }

    HTMLFormatter(URL htmlReportDir, NiceAppendable jsOut) {
        this.htmlReportDir = htmlReportDir;
        this.jsOut = jsOut;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::handleTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(EmbedEvent.class, this::handleEmbed);
        publisher.registerHandlerFor(WriteEvent.class, this::handleWrite);
        publisher.registerHandlerFor(TestRunFinished.class, event -> finishReport());
    }

    private void handleTestSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.getUri(), event);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        if (firstFeature) {
            jsOut.append("$(document).ready(function() {").append("var ")
                .append(JS_FORMATTER_VAR).append(" = new CucumberHTML.DOMFormatter($('.cucumber-report'));");
            firstFeature = false;
        }
        handleStartOfFeature(event.getTestCase());
        handleScenarioOutline(event.getTestCase());
        currentTestCaseMap = createTestCase(event.getTestCase());
        if (testSources.hasBackground(currentFeatureFile, event.getTestCase().getLine())) {
            jsFunctionCall("background", createBackground(event.getTestCase()));
        } else {
            jsFunctionCall("scenario", currentTestCaseMap);
            currentTestCaseMap = null;
        }
    }

    private void handleTestStepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            if (isFirstStepAfterBackground(testStep)) {
                jsFunctionCall("scenario", currentTestCaseMap);
                currentTestCaseMap = null;
            }
            jsFunctionCall("step", createTestStep(testStep));
            jsFunctionCall("match", createMatchMap((PickleStepTestStep) event.getTestStep()));
        }
    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            jsFunctionCall("result", createResultMap(event.getResult()));
        } else if (event.getTestStep() instanceof HookTestStep) {
            HookTestStep hookTestStep = (HookTestStep) event.getTestStep();
            jsFunctionCall(getFunctionName(hookTestStep), createResultMap(event.getResult()));
        } else {
            throw new IllegalStateException();
        }
    }

    private String getFunctionName(HookTestStep hookTestStep) {
        HookType hookType = hookTestStep.getHookType();
        switch (hookType) {
            case BEFORE:
                return "before";
            case AFTER:
                return "after";
            case BEFORE_STEP:
                return "beforestep";
            case AFTER_STEP:
                return "afterstep";
            default:
                throw new IllegalArgumentException(hookType.name());
        }
    }

    private void handleEmbed(EmbedEvent event) {
        String mediaType = event.getMediaType();
        if (mediaType.startsWith("text/")) {
            // just pass straight to the plugin to output in the html
            jsFunctionCall("embedding", mediaType, new String(event.getData()), event.getName());
        } else {
            // Creating a file instead of using data urls to not clutter the js file
            String extension = MIME_TYPES_EXTENSIONS.get(mediaType);
            if (extension != null) {
                StringBuilder fileName = new StringBuilder("embedded").append(embeddedIndex++).append(".").append(extension);
                writeBytesToURL(event.getData(), toUrl(fileName.toString()));
                jsFunctionCall("embedding", mediaType, fileName, event.getName());
            }
        }
    }

    private void handleWrite(WriteEvent event) {
        jsFunctionCall("write", event.getText());
    }

    private void finishReport() {
        if (!firstFeature) {
            jsOut.append("});");
            copyReportFiles();
        }
        jsOut.close();
    }

    private void handleStartOfFeature(TestCase testCase) {
        if (currentFeatureFile == null || !currentFeatureFile.equals(testCase.getUri())) {
            currentFeatureFile = testCase.getUri();
            jsFunctionCall("uri", TestSourcesModel.relativize(currentFeatureFile));
            jsFunctionCall("feature", createFeature(testCase));
        }
    }

    private Map<String, Object> createFeature(TestCase testCase) {
        Map<String, Object> featureMap = new HashMap<>();
        Feature feature = testSources.getFeature(testCase.getUri());
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
        List<Map<String, Object>> tagList = new ArrayList<>();
        for (Tag tag : tags) {
            Map<String, Object> tagMap = new HashMap<>();
            tagMap.put("name", tag.getName());
            tagList.add(tagMap);
        }
        return tagList;
    }

    private void handleScenarioOutline(TestCase testCase) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
        if (TestSourcesModel.isScenarioOutlineScenario(astNode)) {
            ScenarioOutline scenarioOutline = (ScenarioOutline) TestSourcesModel.getScenarioDefinition(astNode);
            if (currentScenarioOutline == null || !currentScenarioOutline.equals(scenarioOutline)) {
                currentScenarioOutline = scenarioOutline;
                jsFunctionCall("scenarioOutline", createScenarioOutline(currentScenarioOutline));
                addOutlineStepsToReport(scenarioOutline);
            }
            Examples examples = (Examples) astNode.parent.node;
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
        Map<String, Object> scenarioOutlineMap = new HashMap<>();
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
            Map<String, Object> stepMap = new HashMap<>();
            stepMap.put("name", step.getText());
            stepMap.put("keyword", step.getKeyword());
            if (step.getArgument() != null) {
                Node argument = step.getArgument();
                if (argument instanceof DocString) {
                    stepMap.put("doc_string", createDocStringMap((DocString) argument));
                } else if (argument instanceof DataTable) {
                    stepMap.put("rows", createDataTableList((DataTable) argument));
                }
            }
            jsFunctionCall("step", stepMap);
        }
    }

    private Map<String, Object> createDocStringMap(DocString docString) {
        Map<String, Object> docStringMap = new HashMap<>();
        docStringMap.put("value", docString.getContent());
        return docStringMap;
    }

    private List<Map<String, Object>> createDataTableList(DataTable dataTable) {
        List<Map<String, Object>> rowList = new ArrayList<>();
        for (TableRow row : dataTable.getRows()) {
            rowList.add(createRowMap(row));
        }
        return rowList;
    }

    private Map<String, Object> createRowMap(TableRow row) {
        Map<String, Object> rowMap = new HashMap<>();
        rowMap.put("cells", createCellList(row));
        return rowMap;
    }

    private List<String> createCellList(TableRow row) {
        List<String> cells = new ArrayList<>();
        for (TableCell cell : row.getCells()) {
            cells.add(cell.getValue());
        }
        return cells;
    }

    private Map<String, Object> createExamples(Examples examples) {
        Map<String, Object> examplesMap = new HashMap<>();
        examplesMap.put("name", examples.getName());
        examplesMap.put("keyword", examples.getKeyword());
        examplesMap.put("description", examples.getDescription() != null ? examples.getDescription() : "");
        List<Map<String, Object>> rowList = new ArrayList<>();
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
        Map<String, Object> testCaseMap = new HashMap<>();
        testCaseMap.put("name", testCase.getName());
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
        if (astNode != null) {
            ScenarioDefinition scenarioDefinition = TestSourcesModel.getScenarioDefinition(astNode);
            testCaseMap.put("keyword", scenarioDefinition.getKeyword());
            testCaseMap.put("description", scenarioDefinition.getDescription() != null ? scenarioDefinition.getDescription() : "");
        }
        if (!testCase.getTags().isEmpty()) {
            List<Map<String, Object>> tagList = new ArrayList<>();
            for (String tag : testCase.getTags()) {
                Map<String, Object> tagMap = new HashMap<>();
                tagMap.put("name", tag);
                tagList.add(tagMap);
            }
            testCaseMap.put("tags", tagList);
        }
        return testCaseMap;
    }

    private Map<String, Object> createBackground(TestCase testCase) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
        if (astNode != null) {
            Background background = TestSourcesModel.getBackgroundForTestCase(astNode);
            Map<String, Object> testCaseMap = new HashMap<>();
            testCaseMap.put("name", background.getName());
            testCaseMap.put("keyword", background.getKeyword());
            testCaseMap.put("description", background.getDescription() != null ? background.getDescription() : "");
            return testCaseMap;
        }
        return null;
    }

    private boolean isFirstStepAfterBackground(PickleStepTestStep testStep) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
        if (astNode != null) {
            return currentTestCaseMap != null && !TestSourcesModel.isBackgroundStep(astNode);
        }
        return false;
    }

    private Map<String, Object> createTestStep(PickleStepTestStep testStep) {
        Map<String, Object> stepMap = new HashMap<>();
        stepMap.put("name", testStep.getStepText());
        StepArgument argument = testStep.getStepArgument();
        if (argument != null) {
            if (argument instanceof DocStringArgument) {
                DocStringArgument docStringArgument = (DocStringArgument) argument;
                stepMap.put("doc_string", createDocStringMap(docStringArgument));
            } else if (argument instanceof DataTableArgument) {
                DataTableArgument dataTableArgument = (DataTableArgument) argument;
                stepMap.put("rows", createDataTableList(dataTableArgument));
            }
        }
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
        if (astNode != null) {
            Step step = (Step) astNode.node;
            stepMap.put("keyword", step.getKeyword());
        }

        return stepMap;
    }

    private Map<String, Object> createDocStringMap(DocStringArgument docString) {
        Map<String, Object> docStringMap = new HashMap<>();
        docStringMap.put("value", docString.getContent());
        return docStringMap;
    }

    private List<Map<String, Object>> createDataTableList(DataTableArgument dataTable) {
        List<Map<String, Object>> rowList = new ArrayList<>();
        for (List<String> row : dataTable.cells()) {
            rowList.add(createRowMap(row));
        }
        return rowList;
    }

    private Map<String, Object> createRowMap(List<String> row) {
        Map<String, Object> rowMap = new HashMap<>();
        rowMap.put("cells", row);
        return rowMap;
    }

    private Map<String, Object> createMatchMap(PickleStepTestStep testStep) {
        Map<String, Object> matchMap = new HashMap<>();
        String location = testStep.getCodeLocation();
        if (location != null) {
            matchMap.put("location", location);
        }
        return matchMap;
    }

    private Map<String, Object> createResultMap(Result result) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", result.getStatus().name().toLowerCase(ROOT));
        if (result.getError() != null) {
            resultMap.put("error_message", printStackTrace(result.getError()));
        }
        return resultMap;
    }

    private void jsFunctionCall(String functionName, Object... args) {
        NiceAppendable out = jsOut.append(JS_FORMATTER_VAR + ".").append(functionName).append("(");
        boolean comma = false;
        for (Object arg : args) {
            if (comma) {
                out.append(", ");
            }
            gson.toJson(arg, out);
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
            String fileName = new File(textAsset).getName();
            writeStreamToURL(textAssetStream, toUrl(fileName));
        }
    }

    private static String printStackTrace(Throwable error) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        error.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private URL toUrl(String fileName) {
        try {
            return new URL(htmlReportDir, fileName);
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    private static void writeStreamToURL(InputStream in, URL url) {
        OutputStream out = createReportFileOutputStream(url);

        byte[] buffer = new byte[16 * 1024];
        try {
            int len = in.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
        } catch (IOException e) {
            throw new CucumberException("Unable to write to report file item: ", e);
        } finally {
            closeQuietly(out);
        }
    }

    private static void writeBytesToURL(byte[] buf, URL url) throws CucumberException {
        OutputStream out = createReportFileOutputStream(url);
        try {
            out.write(buf);
        } catch (IOException e) {
            throw new CucumberException("Unable to write to report file item: ", e);
        } finally {
            closeQuietly(out);
        }
    }

    private static NiceAppendable createJsOut(URL htmlReportDir) {
        try {
            return new NiceAppendable(new OutputStreamWriter(createReportFileOutputStream(new URL(htmlReportDir, JS_REPORT_FILENAME)), UTF_8));
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    private static OutputStream createReportFileOutputStream(URL url) {
        try {
            return new URLOutputStream(url);
        } catch (IOException e) {
            throw new CucumberException(e);
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
