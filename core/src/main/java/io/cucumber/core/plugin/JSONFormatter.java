package io.cucumber.core.plugin;

import gherkin.ast.Background;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.Argument;
import io.cucumber.plugin.event.DocStringArgument;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.WriteEvent;
import io.cucumber.plugin.EventListener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Locale.ROOT;

public final class JSONFormatter implements EventListener {
    private static final String before = "before";
    private static final String after = "after";
    private String currentFeatureFile;
    private final List<Map<String, Object>> featureMaps = new ArrayList<>();
    private List<Map<String, Object>> currentElementsList;
    private Map<String, Object> currentElementMap;
    private Map<String, Object> currentTestCaseMap;
    private List<Map<String, Object>> currentStepsList;
    private Map<String, Object> currentStepOrHookMap;
    private final Map<String, Object> currentBeforeStepHookList = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final NiceAppendable out;
    private final TestSourcesModel testSources = new TestSourcesModel();

    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public JSONFormatter(Appendable out) {
        this.out = new NiceAppendable(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::handleTestSourceRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::handleTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
        publisher.registerHandlerFor(WriteEvent.class, this::handleWrite);
        publisher.registerHandlerFor(EmbedEvent.class, this::handleEmbed);
        publisher.registerHandlerFor(TestRunFinished.class, event -> finishReport());
    }

    private void handleTestSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.getUri(), event);
    }

    @SuppressWarnings("unchecked")
    private void handleTestCaseStarted(TestCaseStarted event) {
        if (currentFeatureFile == null || !currentFeatureFile.equals(event.getTestCase().getUri())) {
            currentFeatureFile = event.getTestCase().getUri();
            Map<String, Object> currentFeatureMap = createFeatureMap(event.getTestCase());
            featureMaps.add(currentFeatureMap);
            currentElementsList = (List<Map<String, Object>>) currentFeatureMap.get("elements");
        }
        currentTestCaseMap = createTestCase(event);
        if (testSources.hasBackground(currentFeatureFile, event.getTestCase().getLine())) {
            currentElementMap = createBackground(event.getTestCase());
            currentElementsList.add(currentElementMap);
        } else {
            currentElementMap = currentTestCaseMap;
        }
        currentElementsList.add(currentTestCaseMap);
        currentStepsList = (List<Map<String, Object>>) currentElementMap.get("steps");
    }

    @SuppressWarnings("unchecked")
    private void handleTestStepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            if (isFirstStepAfterBackground(testStep)) {
                currentElementMap = currentTestCaseMap;
                currentStepsList = (List<Map<String, Object>>) currentElementMap.get("steps");
            }
            currentStepOrHookMap = createTestStep(testStep);
            //add beforeSteps list to current step
            if (currentBeforeStepHookList.containsKey(before)) {
                currentStepOrHookMap.put(before, currentBeforeStepHookList.get(before));
                currentBeforeStepHookList.clear();
            }
            currentStepsList.add(currentStepOrHookMap);
        } else if (event.getTestStep() instanceof HookTestStep) {
            HookTestStep hookTestStep = (HookTestStep) event.getTestStep();
            currentStepOrHookMap = createHookStep(hookTestStep);
            addHookStepToTestCaseMap(currentStepOrHookMap, hookTestStep.getHookType());
        } else {
            throw new IllegalStateException();
        }
    }

    private void handleWrite(WriteEvent event) {
        addOutputToHookMap(event.getText());
    }

    private void handleEmbed(EmbedEvent event) {
        addEmbeddingToHookMap(event.getData(), event.getMimeType(), event.getName());
    }

    private void handleTestStepFinished(TestStepFinished event) {
        currentStepOrHookMap.put("match", createMatchMap(event.getTestStep(), event.getResult()));
        currentStepOrHookMap.put("result", createResultMap(event.getResult()));
    }

    private void finishReport() {
        gson.toJson(featureMaps, out);
        out.close();
    }

    private Map<String, Object> createFeatureMap(TestCase testCase) {
        Map<String, Object> featureMap = new HashMap<>();
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

    private Map<String, Object> createTestCase(TestCaseStarted event) {
        Map<String, Object> testCaseMap = new HashMap<>();

        testCaseMap.put("start_timestamp", getDateTimeFromTimeStamp(event.getInstant()));

        TestCase testCase = event.getTestCase();

        testCaseMap.put("name", testCase.getName());
        testCaseMap.put("line", testCase.getLine());
        testCaseMap.put("type", "scenario");
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
        if (astNode != null) {
            testCaseMap.put("id", TestSourcesModel.calculateId(astNode));
            ScenarioDefinition scenarioDefinition = TestSourcesModel.getScenarioDefinition(astNode);
            testCaseMap.put("keyword", scenarioDefinition.getKeyword());
            testCaseMap.put("description", scenarioDefinition.getDescription() != null ? scenarioDefinition.getDescription() : "");
        }
        testCaseMap.put("steps", new ArrayList<Map<String, Object>>());
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
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
        if (astNode == null) {
            return false;
        }
        return currentElementMap != currentTestCaseMap && !TestSourcesModel.isBackgroundStep(astNode);
    }

    private Map<String, Object> createTestStep(PickleStepTestStep testStep) {
        Map<String, Object> stepMap = new HashMap<>();
        stepMap.put("name", testStep.getStepText());
        stepMap.put("line", testStep.getStepLine());
        TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
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
        if (astNode != null) {
            Step step = (Step) astNode.node;
            stepMap.put("keyword", step.getKeyword());
        }

        return stepMap;
    }

    private Map<String, Object> createDocStringMap(DocStringArgument docString) {
        Map<String, Object> docStringMap = new HashMap<>();
        docStringMap.put("value", docString.getContent());
        docStringMap.put("line", docString.getLine());
        docStringMap.put("content_type", docString.getContentType());
        return docStringMap;
    }

    private List<Map<String, List<String>>> createDataTableList(DataTableArgument argument) {
        List<Map<String, List<String>>> rowList = new ArrayList<>();
        for (List<String> row : argument.cells()) {
            Map<String, List<String>> rowMap = new HashMap<>();
            rowMap.put("cells", new ArrayList<>(row));
            rowList.add(rowMap);
        }
        return rowList;
    }

    private Map<String, Object> createHookStep(HookTestStep hookTestStep) {
        return new HashMap<>();
    }

    private void addHookStepToTestCaseMap(Map<String, Object> currentStepOrHookMap, HookType hookType) {
        String hookName;
        if (hookType == HookType.AFTER || hookType == HookType.AFTER_STEP)
            hookName = after;
        else
            hookName = before;

        Map<String, Object> mapToAddTo;
        switch (hookType) {
            case BEFORE:
                mapToAddTo = currentTestCaseMap;
                break;
            case AFTER:
                mapToAddTo = currentTestCaseMap;
                break;
            case BEFORE_STEP:
                mapToAddTo = currentBeforeStepHookList;
                break;
            case AFTER_STEP:
                mapToAddTo = currentStepsList.get(currentStepsList.size() - 1);
                break;
            default:
                mapToAddTo = currentTestCaseMap;
        }

        if (!mapToAddTo.containsKey(hookName)) {
            mapToAddTo.put(hookName, new ArrayList<Map<String, Object>>());
        }
        ((List<Map<String, Object>>) mapToAddTo.get(hookName)).add(currentStepOrHookMap);
    }

    private void addOutputToHookMap(String text) {
        if (!currentStepOrHookMap.containsKey("output")) {
            currentStepOrHookMap.put("output", new ArrayList<String>());
        }
        ((List<String>) currentStepOrHookMap.get("output")).add(text);
    }

    private void addEmbeddingToHookMap(byte[] data, String mimeType, String name) {
        if (!currentStepOrHookMap.containsKey("embeddings")) {
            currentStepOrHookMap.put("embeddings", new ArrayList<Map<String, Object>>());
        }
        Map<String, Object> embedMap = createEmbeddingMap(data, mimeType, name);
        ((List<Map<String, Object>>) currentStepOrHookMap.get("embeddings")).add(embedMap);
    }

    private Map<String, Object> createEmbeddingMap(byte[] data, String mimeType, String name) {
        Map<String, Object> embedMap = new HashMap<>();
        embedMap.put("mime_type", mimeType);
        embedMap.put("data", Base64.getEncoder().encodeToString(data));
        if (name != null) {
            embedMap.put("name", name);
        }
        return embedMap;
    }

    private Map<String, Object> createMatchMap(TestStep step, Result result) {
        Map<String, Object> matchMap = new HashMap<>();
        if (step instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) step;
            if (!testStep.getDefinitionArgument().isEmpty()) {
                List<Map<String, Object>> argumentList = new ArrayList<>();
                for (Argument argument : testStep.getDefinitionArgument()) {
                    Map<String, Object> argumentMap = new HashMap<>();
                    if (argument.getValue() != null) {
                        argumentMap.put("val", argument.getValue());
                        argumentMap.put("offset", argument.getStart());
                    }
                    argumentList.add(argumentMap);
                }
                matchMap.put("arguments", argumentList);
            }
        }
        if (!result.getStatus().is(Status.UNDEFINED)) {
            matchMap.put("location", step.getCodeLocation());
        }
        return matchMap;
    }

    private Map<String, Object> createResultMap(Result result) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", result.getStatus().name().toLowerCase(ROOT));
        if (result.getError() != null) {
            resultMap.put("error_message", printStackTrace(result.getError()));
        }
        if (!result.getDuration().isZero()) {
            resultMap.put("duration", result.getDuration().toNanos());
        }
        return resultMap;
    }

    private String getDateTimeFromTimeStamp(Instant instant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .withZone(ZoneOffset.UTC);
        return formatter.format(instant);
    }

    private static String printStackTrace(Throwable error) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        error.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
