package cucumber.runtime.formatter;

import cucumber.api.HookType;
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
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.TokenMatcher;
import gherkin.ast.Background;
import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Node;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;
import gherkin.ast.TableRow;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.deps.net.iharder.Base64;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import gherkin.pickles.PickleTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONFormatter implements Formatter {
    private String currentFeatureFile;
    private List<Map<String, Object>> featureMaps = new ArrayList<Map<String, Object>>();
    private Map<String, Object> currentFeatureMap;
    private List<Map<String, Object>> currentElementsList;
    private Map<String, Object> currentElementMap;
    private Map<String, Object> currentTestCaseMap;
    private List<Map<String, Object>> currentStepsList;
    private Map<String, Object> currentStepOrHookMap;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final NiceAppendable out;
    private final Map<String, String> pathToSourceMap = new HashMap<String, String>();
    private final Map<String, GherkinDocument> pathToAstMap = new HashMap<String, GherkinDocument>();
    private final Map<String, Map<Integer, AstNode>> pathToNodeMap = new HashMap<String, Map<Integer, AstNode>>();

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
    private EventHandler<TestStepStarted> stepStartedhandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            handleTestStepStarted(event);
        }
    };
    private EventHandler<TestStepFinished> stepFinishedhandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            handleTestStepFinished(event);
        }
    };
    private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            finishReport();
        }
    };
    private EventHandler<WriteEvent> writeEventhandler = new EventHandler<WriteEvent>() {
        @Override
        public void receive(WriteEvent event) {
            handleWrite(event);
        }
    };
    private EventHandler<EmbedEvent> embedEventhandler = new EventHandler<EmbedEvent>() {
        @Override
        public void receive(EmbedEvent event) {
            handleEmbed(event);
        }
    };

    public JSONFormatter(Appendable out) {
        this.out = new NiceAppendable(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, caseStartedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedhandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedhandler);
        publisher.registerHandlerFor(WriteEvent.class, writeEventhandler);
        publisher.registerHandlerFor(EmbedEvent.class, embedEventhandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
    }

    private void handleTestSourceRead(TestSourceRead event) {
        pathToSourceMap.put(event.path, event.source);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        if (currentFeatureFile == null || !currentFeatureFile.equals(event.testCase.getPath())) {
            currentFeatureFile = event.testCase.getPath();
            currentFeatureMap = createFeatureMap(event.testCase);
            featureMaps.add(currentFeatureMap);
            currentElementsList = (List<Map<String, Object>>) currentFeatureMap.get("elements");
        }
        currentTestCaseMap = createTestCase(event.testCase);
        if (hasBackground(event.testCase)) {
            currentElementMap = createBackground(event.testCase);
            currentElementsList.add(currentElementMap);
        } else {
            currentElementMap = currentTestCaseMap;
        }
        currentElementsList.add(currentTestCaseMap);
        currentStepsList = (List<Map<String, Object>>) currentElementMap.get("steps");
    }

    private void handleTestStepStarted(TestStepStarted event) {
        if (!event.testStep.isHook()) {
            if (isFirstStepAfterBackground(event.testStep)) {
                currentElementMap = currentTestCaseMap;
                currentStepsList = (List<Map<String, Object>>) currentElementMap.get("steps");
            }
            currentStepOrHookMap = createTestStep(event.testStep);
            currentStepsList.add(currentStepOrHookMap);
        } else {
            currentStepOrHookMap = createHookStep(event.testStep);
            addHookStepToTestCaseMap(currentStepOrHookMap, event.testStep.getHookType());
        }
    }

    private void handleWrite(WriteEvent event) {
        addOutputToHookMap(event.text);
    }

    private void handleEmbed(EmbedEvent event) {
        addEmbeddingToHookMap(event.data, event.mimeType);
    }

    private void handleTestStepFinished(TestStepFinished event) {
        currentStepOrHookMap.put("match", createMatchMap(event.testStep, event.result));
        currentStepOrHookMap.put("result", createResultMap(event.result));
    }

    private void finishReport() {
        out.append(gson.toJson(featureMaps));
        out.close();
    }

    private Map<String, Object> createFeatureMap(TestCase testCase) {
        Map<String, Object> featureMap = new HashMap<String, Object>();
        featureMap.put("uri", testCase.getPath());
        featureMap.put("elements", new ArrayList<Map<String, Object>>());
        if (!pathToAstMap.containsKey(testCase.getPath())) {
            parseGherkinSource(testCase.getPath());
        }
        if (pathToAstMap.containsKey(testCase.getPath())) {
            Feature feature = pathToAstMap.get(testCase.getPath()).getFeature();
            featureMap.put("keyword", feature.getKeyword());
            featureMap.put("name", feature.getName());
            featureMap.put("description", feature.getDescription() != null ? feature.getDescription() : "");
            featureMap.put("line", feature.getLocation().getLine());
            featureMap.put("id", convertToId(feature.getName()));
        }
        return featureMap;
    }

    private void parseGherkinSource(String path) {
        if (!pathToSourceMap.containsKey(path)) {
            return;
        }
        Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
        TokenMatcher matcher = new TokenMatcher();
        try {
            GherkinDocument gherkinDocument = parser.parse(pathToSourceMap.get(path), matcher);
            pathToAstMap.put(path, gherkinDocument);
            Map<Integer, AstNode> nodeMap = new HashMap<Integer, AstNode>();
            AstNode currentParent = new AstNode(gherkinDocument.getFeature(), null);
            for (ScenarioDefinition child : gherkinDocument.getFeature().getChildren()) {
                processScenarioDefinition(nodeMap, child, currentParent);
            }
            pathToNodeMap.put(path, nodeMap);
        } catch (ParserException e) {
            // Ignore exceptions
        }
    }

    private void processScenarioDefinition(Map<Integer, AstNode> nodeMap, ScenarioDefinition child, AstNode currentParent) {
        AstNode childNode = new AstNode(child, currentParent);
        nodeMap.put(child.getLocation().getLine(), childNode);
        for (Step step : child.getSteps()) {
            nodeMap.put(step.getLocation().getLine(), new AstNode(step, childNode));
        }
        if (child instanceof ScenarioOutline) {
            processScenarioOutlineExamples(nodeMap, (ScenarioOutline)child, childNode);
        }
    }

    private void processScenarioOutlineExamples(Map<Integer, AstNode> nodeMap, ScenarioOutline scenarioOutline, AstNode childNode) {
        for (Examples examples : scenarioOutline.getExamples()) {
            AstNode examplesNode = new AstNode(examples, childNode);
            for (int i = 0; i < examples.getTableBody().size(); ++i) {
                TableRow examplesRow = examples.getTableBody().get(i);
                Node rowNode = new ExamplesRowWrapperNode(examplesRow, i);
                AstNode expandedScenarioNode = new AstNode(rowNode, examplesNode);
                nodeMap.put(examplesRow.getLocation().getLine(), expandedScenarioNode);
            }
        }
    }

    private String convertToId(String name) {
        return name.replaceAll("[\\s'_,!]", "-").toLowerCase();
    }

    private Map<String, Object> createTestCase(TestCase testCase) {
        Map<String, Object> testCaseMap = new HashMap<String, Object>();
        testCaseMap.put("name", testCase.getName());
        testCaseMap.put("line", testCase.getLine());
        testCaseMap.put("type", "scenario");
        if (pathToNodeMap.containsKey(currentFeatureFile)) {
            AstNode astNode = pathToNodeMap.get(currentFeatureFile).get(testCase.getLine());
            testCaseMap.put("id", calculateId(astNode));
            ScenarioDefinition scenarioDefinition = astNode.node instanceof ScenarioDefinition ? (ScenarioDefinition)astNode.node : (ScenarioDefinition)astNode.parent.parent.node;
            testCaseMap.put("keyword", scenarioDefinition.getKeyword());
            testCaseMap.put("description", scenarioDefinition.getDescription() != null ? scenarioDefinition.getDescription() : "");
        }
        testCaseMap.put("steps", new ArrayList<Map<String, Object>>());
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

    private String calculateId(AstNode astNode) {
        Node node = astNode.node;
        if (node instanceof ScenarioDefinition) {
            return calculateId(astNode.parent) + ";" + convertToId(((ScenarioDefinition)node).getName());
        }
        if (node instanceof ExamplesRowWrapperNode) {
            return calculateId(astNode.parent) + ";" + Integer.toString(((ExamplesRowWrapperNode)node).bodyRowIndex + 2);
        }
        if (node instanceof Examples) {
            return calculateId(astNode.parent) + ";" + convertToId(((Examples)node).getName());
        }
        if (node instanceof Feature) {
            return convertToId(((Feature)node).getName());
        }
        return "";
    }

    private boolean hasBackground(TestCase testCase) {
        if (pathToNodeMap.containsKey(currentFeatureFile)) {
            AstNode astNode = pathToNodeMap.get(currentFeatureFile).get(testCase.getLine());
            Feature feature = getFeatureForTestCase(astNode);
            if (feature.getChildren().get(0) instanceof Background) {
                return true;
            }
        }
        return false;
    }

    private Feature getFeatureForTestCase(AstNode astNode) {
        while (astNode.parent != null) {
            astNode = astNode.parent;
        }
        return (Feature)astNode.node;
    }

    private Map<String, Object> createBackground(TestCase testCase) {
        if (pathToNodeMap.containsKey(currentFeatureFile)) {
            AstNode astNode = pathToNodeMap.get(currentFeatureFile).get(testCase.getLine());
            Feature feature = getFeatureForTestCase(astNode);
            Background background = (Background)feature.getChildren().get(0);
            Map<String, Object> testCaseMap = new HashMap<String, Object>();
            testCaseMap.put("name", background.getName());
            testCaseMap.put("line", background.getLocation().getLine());
            testCaseMap.put("type", "background");
            testCaseMap.put("keyword", background.getKeyword());
            testCaseMap.put("description", background.getDescription() != null ? background.getDescription() : "");
            testCaseMap.put("steps", new ArrayList<Map<String, Object>>());
            return testCaseMap;
        }
        return null;
    }

    private boolean isFirstStepAfterBackground(TestStep testStep) {
        if (pathToNodeMap.containsKey(currentFeatureFile)) {
            AstNode astNode = pathToNodeMap.get(currentFeatureFile).get(testStep.getStepLine());
            if (currentElementMap != currentTestCaseMap && !(astNode.parent.node instanceof Background)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> createTestStep(TestStep testStep) {
        Map<String, Object> stepMap = new HashMap<String, Object>();
        stepMap.put("name", testStep.getStepText());
        stepMap.put("line", testStep.getStepLine());
        if (!testStep.getStepArgument().isEmpty()) {
            Argument argument = testStep.getStepArgument().get(0);
            if (argument instanceof PickleString) {
                stepMap.put("doc_string", createDocStringMap(argument));
            } else if (argument instanceof PickleTable) {
                stepMap.put("rows", createDataTableList(argument));
            }
        }
        if (pathToNodeMap.containsKey(currentFeatureFile)) {
            Step step = (Step) pathToNodeMap.get(currentFeatureFile).get(testStep.getStepLine()).node;
            stepMap.put("keyword", step.getKeyword());
        }

        return stepMap;
    }

    private Map<String, Object> createDocStringMap(Argument argument) {
        Map<String, Object> docStringMap = new HashMap<String, Object>();
        PickleString docString = ((PickleString)argument);
        docStringMap.put("value", docString.getContent());
        docStringMap.put("line", docString.getLocation().getLine());
        return docStringMap;
    }

    private List<Map<String, Object>> createDataTableList(Argument argument) {
        List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();
        for (PickleRow row : ((PickleTable)argument).getRows()) {
            Map<String, Object> rowMap = new HashMap<String, Object>();
            rowMap.put("cells", createCellList(row));
            rowList.add(rowMap);
        }
        return rowList;
    }

    private List<String> createCellList(PickleRow row) {
        List<String> cells = new ArrayList<String>();
        for (PickleCell cell : row.getCells()) {
            cells.add(cell.getValue());
        }
        return cells;
    }

    private Map<String, Object> createHookStep(TestStep testStep) {
        return new HashMap<String, Object>();
    }

    private void addHookStepToTestCaseMap(Map<String, Object> currentStepOrHookMap, HookType hookType) {
        if (!currentTestCaseMap.containsKey(hookType.toString())) {
            currentTestCaseMap.put(hookType.toString(), new ArrayList<Map<String, Object>>());
        }
        ((List<Map<String, Object>>)currentTestCaseMap.get(hookType.toString())).add(currentStepOrHookMap);
    }

    private void addOutputToHookMap(String text) {
        if (!currentStepOrHookMap.containsKey("output")) {
            currentStepOrHookMap.put("output", new ArrayList<String>());
        }
        ((List<String>)currentStepOrHookMap.get("output")).add(text);
    }

    private void addEmbeddingToHookMap(byte[] data, String mimeType) {
        if (!currentStepOrHookMap.containsKey("embedding")) {
            currentStepOrHookMap.put("embedding", new ArrayList<Map<String, Object>>());
        }
        Map<String, Object> embedMap = createEmbeddingMap(data, mimeType);
        ((List<Map<String, Object>>)currentStepOrHookMap.get("embedding")).add(embedMap);
    }

    private Map<String, Object> createEmbeddingMap(byte[] data, String mimeType) {
        Map<String, Object> embedMap = new HashMap<String, Object>();
        embedMap.put("mime_type", mimeType);
        embedMap.put("data", Base64.encodeBytes(data));
        return embedMap;
    }

    private Map<String, Object> createMatchMap(TestStep testStep, Result result) {
        Map<String, Object> matchMap = new HashMap<String, Object>();
        if (!testStep.getDefinitionArgument().isEmpty()) {
            List<Map<String, Object>> argumentList = new ArrayList<Map<String, Object>>();
            for (cucumber.runtime.Argument argument : testStep.getDefinitionArgument()) {
                Map<String, Object> argumentMap = new HashMap<String, Object>();
                argumentMap.put("val", argument.getVal());
                argumentMap.put("offset", argument.getOffset());
                argumentList.add(argumentMap);
            }
            matchMap.put("arguments", argumentList);
        }
        if (!result.getStatus().equals(Result.UNDEFINED)) {
            matchMap.put("location", testStep.getCodeLocation());
        }
        return matchMap;
    }

    private Map<String, Object> createResultMap(Result result) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("status", result.getStatus());
        if (result.getErrorMessage() != null) {
            resultMap.put("error_message", result.getErrorMessage());
        }
        if (result.getDuration() != null && result.getDuration() != 0) {
            resultMap.put("duration", result.getDuration());
        }
        return resultMap;
    }

    private class AstNode {
        public final Node node;
        public final AstNode parent;

        public AstNode(Node node, AstNode parent) {
            this.node = node;
            this.parent = parent;
        }
    }

    private class ExamplesRowWrapperNode extends Node {
        public final int bodyRowIndex;

        protected ExamplesRowWrapperNode(Node examplesRow, int bodyRowIndex) {
            super(examplesRow.getLocation());
            this.bodyRowIndex = bodyRowIndex;
        }
    }
}
