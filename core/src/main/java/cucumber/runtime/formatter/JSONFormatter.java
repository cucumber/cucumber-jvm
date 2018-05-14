package cucumber.runtime.formatter;

import cucumber.api.HookTestStep;
import cucumber.api.HookType;
import cucumber.api.Result;
import cucumber.api.TestStep;
import cucumber.api.TestCase;
import cucumber.api.PickleStepTestStep;
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
import gherkin.ast.Background;
import gherkin.ast.DocString;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class JSONFormatter implements Formatter {
    private final List<Map<String, Object>> featureMaps = Collections.synchronizedList(new LinkedList<Map<String, Object>>());
    private final ThreadLocal<CurrentFeature> featureUnderTest = new ThreadLocal<CurrentFeature>();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final NiceAppendable out;
    private final TestSourcesModel testSources = new TestSourcesModel();

    private final EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
        @Override
        public void receive(TestSourceRead event) {
            handleTestSourceRead(event);
        }
    };
    private final EventHandler<TestCaseStarted> caseStartedHandler = new EventHandler<TestCaseStarted>() {
        @Override
        public void receive(TestCaseStarted event) {
            handleTestCaseStarted(event);
        }
    };
    private final EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            handleTestStepStarted(event);
        }
    };
    private final EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            handleTestStepFinished(event);
        }
    };
    private final EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
        @Override
        public void receive(TestRunFinished event) {
            finishReport();
        }
    };
    private final EventHandler<WriteEvent> writeEventHandler = new EventHandler<WriteEvent>() {
        @Override
        public void receive(WriteEvent event) {
            handleWrite(event);
        }
    };
    private final EventHandler<EmbedEvent> embedEventHandler = new EventHandler<EmbedEvent>() {
        @Override
        public void receive(EmbedEvent event) {
            handleEmbed(event);
        }
    };

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public JSONFormatter(Appendable out) {
        this.out = new NiceAppendable(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, caseStartedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
        publisher.registerHandlerFor(WriteEvent.class, writeEventHandler);
        publisher.registerHandlerFor(EmbedEvent.class, embedEventHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
    }

    private void handleTestSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.uri, event);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        CurrentFeature currentFeature = featureUnderTest.get();
        if (currentFeature == null || !currentFeature.uri.equals(event.testCase.getUri())) {
            currentFeature = new CurrentFeature(event.testCase.getUri());
            featureUnderTest.set(currentFeature);
            Map<String, Object> currentFeatureMap = createFeatureMap(event.testCase);
            featureMaps.add(currentFeatureMap);
            currentFeature.elementsList = (List<Map<String, Object>>) currentFeatureMap.get("elements");
        }
        currentFeature.testCaseMap = createTestCase(event.testCase);
        if (testSources.hasBackground(currentFeature.uri, event.testCase.getLine())) {
            currentFeature.elementMap = createBackground(event.testCase);
            currentFeature.elementsList.add(currentFeature.elementMap);
        } else {
            currentFeature.elementMap = currentFeature.testCaseMap;
        }
        currentFeature.elementsList.add(currentFeature.testCaseMap);
        currentFeature.stepsList = (List<Map<String, Object>>) currentFeature.elementMap.get("steps");
    }

    private void handleTestStepStarted(TestStepStarted event) {
        if (event.testStep instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.testStep;
            if (isFirstStepAfterBackground(testStep)) {
                currentElementMap = currentTestCaseMap;
                currentStepsList = (List<Map<String, Object>>) currentElementMap.get("steps");
            }
            currentStepOrHookMap = createTestStep(testStep);
            //add beforeSteps list to current step
            if (currentBeforeStepHookList.containsKey(HookType.Before.toString())) {
                currentStepOrHookMap.put(HookType.Before.toString(), currentBeforeStepHookList.get(HookType.Before.toString()));
                currentBeforeStepHookList.clear();
            }
            currentStepsList.add(currentStepOrHookMap);
        } else if(event.testStep instanceof HookTestStep) {
            HookTestStep hookTestStep = (HookTestStep) event.testStep;
            currentStepOrHookMap = createHookStep(hookTestStep);
            addHookStepToTestCaseMap(currentStepOrHookMap, hookTestStep.getHookType());
        } else {
            throw new IllegalStateException();
        }
    }

    private void handleWrite(WriteEvent event) {
        addOutputToHookMap(event.text);
    }

    private void handleEmbed(EmbedEvent event) {
        addEmbeddingToHookMap(event.data, event.mimeType);
    }

    private void handleTestStepFinished(TestStepFinished event) {
        CurrentFeature currentFeature = featureUnderTest.get();
        currentFeature.stepOrHookMap.put("match", createMatchMap(event.testStep, event.result));
        currentFeature.stepOrHookMap.put("result", createResultMap(event.result));
    }

    private void finishReport() {
        out.append(gson.toJson(featureMaps));
        out.close();
    }

    private Map<String, Object> createFeatureMap(TestCase testCase) {
        Map<String, Object> featureMap = new HashMap<String, Object>();
        featureMap.put("uri", testCase.getUri());
        featureMap.put("elements", new ArrayList<Map<String, Object>>());
        Feature feature = testSources.getFeature(testCase.getUri());
        if (feature != null) {
            featureMap.put("keyword", feature.getKeyword());
            featureMap.put("name", feature.getName());
            featureMap.put("description", feature.getDescription() != null ? feature.getDescription() : "");
            featureMap.put("line", feature.getLocation().getLine());
            featureMap.put("id", TestSourcesModel.convertToId(feature.getName()));
            featureMap.put("tags", feature.getTags());

        }
        return featureMap;
    }

    private Map<String, Object> createTestCase(TestCase testCase) {
        CurrentFeature currentFeature = featureUnderTest.get();
        Map<String, Object> testCaseMap = new HashMap<String, Object>();
        testCaseMap.put("name", testCase.getName());
        testCaseMap.put("line", testCase.getLine());
        testCaseMap.put("type", "scenario");
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeature.uri, testCase.getLine());
        if (astNode != null) {
            testCaseMap.put("id", TestSourcesModel.calculateId(astNode));
            ScenarioDefinition scenarioDefinition = TestSourcesModel.getScenarioDefinition(astNode);
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

    private Map<String, Object> createBackground(TestCase testCase) {
        CurrentFeature currentFeature = featureUnderTest.get();
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeature.uri, testCase.getLine());
        if (astNode != null) {
            Background background = TestSourcesModel.getBackgroundForTestCase(astNode);
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

    private boolean isFirstStepAfterBackground(PickleStepTestStep testStep) {
        CurrentFeature currentFeature = featureUnderTest.get();
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeature.uri, testStep.getStepLine());
        if (astNode != null) {
            if (currentFeature.elementMap != currentFeature.testCaseMap && !TestSourcesModel.isBackgroundStep(astNode)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> createTestStep(PickleStepTestStep testStep) {
        CurrentFeature currentFeature = featureUnderTest.get();
        Map<String, Object> stepMap = new HashMap<String, Object>();
        stepMap.put("name", testStep.getStepText());
        stepMap.put("line", testStep.getStepLine());
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeature.uri, testStep.getStepLine());
        if (!testStep.getStepArgument().isEmpty()) {
            Argument argument = testStep.getStepArgument().get(0);
            if (argument instanceof PickleString) {
                stepMap.put("doc_string", createDocStringMap(argument, astNode));
            } else if (argument instanceof PickleTable) {
                stepMap.put("rows", createDataTableList(argument));
            }
        }
        if (astNode != null) {
            Step step = (Step) astNode.node;
            stepMap.put("keyword", step.getKeyword());
        }

        return stepMap;
    }

    private Map<String, Object> createDocStringMap(Argument argument, TestSourcesModel.AstNode astNode) {
        Map<String, Object> docStringMap = new HashMap<String, Object>();
        PickleString docString = ((PickleString)argument);
        docStringMap.put("value", docString.getContent());
        docStringMap.put("line", docString.getLocation().getLine());
        if (astNode != null) {
            docStringMap.put("content_type", ((DocString)((Step)astNode.node).getArgument()).getContentType());
        }
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

    private Map<String, Object> createHookStep(HookTestStep hookTestStep) {
        return new HashMap<String, Object>();
    }

    private void addHookStepToTestCaseMap(Map<String, Object> currentStepOrHookMap, HookType hookType) {
        String hookName;
        if (hookType.toString().contains("after"))
            hookName = "after";
        else
            hookName = "before";


        Map<String, Object> mapToAddTo;
        switch (hookType) {
            case Before:
                mapToAddTo = currentTestCaseMap;
                break;
            case After:
                mapToAddTo = currentTestCaseMap;
                break;
            case BeforeStep:
                mapToAddTo = currentBeforeStepHookList;
                break;
            case AfterStep:
                mapToAddTo = currentStepsList.get(currentStepsList.size() - 1);
                break;
             default:
                 mapToAddTo = currentTestCaseMap;
        }

        if (!mapToAddTo.containsKey(hookName)) {
            mapToAddTo.put(hookName, new ArrayList<Map<String, Object>>());
        }
        ((List<Map<String, Object>>)mapToAddTo.get(hookName)).add(currentStepOrHookMap);
    }

    private void addOutputToHookMap(String text) {
        CurrentFeature currentFeature = featureUnderTest.get();
        if (!currentFeature.stepOrHookMap.containsKey("output")) {
            currentFeature.stepOrHookMap.put("output", new ArrayList<String>());
        }
        ((List<String>)currentFeature.stepOrHookMap.get("output")).add(text);
    }

    private void addEmbeddingToHookMap(byte[] data, String mimeType) {
        CurrentFeature currentFeature = featureUnderTest.get();
        if (!currentFeature.stepOrHookMap.containsKey("embeddings")) {
            currentFeature.stepOrHookMap.put("embeddings", new ArrayList<Map<String, Object>>());
        }
        Map<String, Object> embedMap = createEmbeddingMap(data, mimeType);
        ((List<Map<String, Object>>)currentFeature.stepOrHookMap.get("embeddings")).add(embedMap);
    }

    private Map<String, Object> createEmbeddingMap(byte[] data, String mimeType) {
        Map<String, Object> embedMap = new HashMap<String, Object>();
        embedMap.put("mime_type", mimeType);
        embedMap.put("data", Base64.encodeBytes(data));
        return embedMap;
    }

    private Map<String, Object> createMatchMap(TestStep step, Result result) {
        Map<String, Object> matchMap = new HashMap<String, Object>();
        if(step instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) step;
            if (!testStep.getDefinitionArgument().isEmpty()) {
                List<Map<String, Object>> argumentList = new ArrayList<Map<String, Object>>();
                for (cucumber.api.Argument argument : testStep.getDefinitionArgument()) {
                    Map<String, Object> argumentMap = new HashMap<String, Object>();
                    argumentMap.put("val", argument.getVal());
                    argumentMap.put("offset", argument.getOffset());
                    argumentList.add(argumentMap);
                }
                matchMap.put("arguments", argumentList);
            }
        }
        if (!result.is(Result.Type.UNDEFINED)) {
            matchMap.put("location", step.getCodeLocation());
        }
        return matchMap;
    }

    private Map<String, Object> createResultMap(Result result) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("status", result.getStatus().lowerCaseName());
        if (result.getErrorMessage() != null) {
            resultMap.put("error_message", result.getErrorMessage());
        }
        if (result.getDuration() != null && result.getDuration() != 0) {
            resultMap.put("duration", result.getDuration());
        }
        return resultMap;
    }
    
    private class CurrentFeature {
        private final String uri;
        private List<Map<String, Object>> elementsList;
        private Map<String, Object> elementMap;
        private Map<String, Object> testCaseMap;
        private List<Map<String, Object>> stepsList;
        private Map<String, Object> stepOrHookMap;
        
        CurrentFeature(final String uri) {
            this.uri = uri;
        }
    }
}
