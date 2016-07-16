package cucumber.runtime;

import cucumber.api.Result;
import cucumber.api.TestStep;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestSourceRead;
import cucumber.api.event.TestStepFinished;
import gherkin.AstBuilder;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.IGherkinDialectProvider;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.TokenMatcher;
import gherkin.ast.Background;
import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UndefinedStepsTracker implements EventListener {
    private final List<String> snippets = new ArrayList<String>();
    private final IGherkinDialectProvider dialectProvider = new GherkinDialectProvider();
    private final Map<String, String> pathToSourceMap = new HashMap<String, String>();
    private final Map<String, FeatureStepMap> pathToStepMap = new HashMap<String, FeatureStepMap>();
    private boolean hasUndefinedSteps = false;

    private EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
        @Override
        public void receive(TestSourceRead event) {
            pathToSourceMap.put(event.path, event.source);
        }
    };
    private EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {
        @Override
        public void receive(TestStepFinished event) {
            handleTestStepFinished(event.testStep, event.result);
        }
    };

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
    }

    public boolean hasUndefinedSteps() {
        return hasUndefinedSteps;
    }

    public List<String> getSnippets() {
        return snippets;
    }

    void handleTestStepFinished(TestStep step, Result result) {
        if (Result.UNDEFINED.equals(result.getStatus())) {
            hasUndefinedSteps = true;
            String keyword = givenWhenThenKeyword(step.getPickleStep());
            for (String rawSnippet : result.getSnippets()) {
                String snippet = rawSnippet.replace("**KEYWORD**", keyword);
                if (!snippets.contains(snippet)) {
                    snippets.add(snippet);
                }
            }
        }
    }

    private String givenWhenThenKeyword(PickleStep step) {
        String keyword = null;
        if (!step.getLocations().isEmpty()) {
            List<PickleLocation> stepLocations = step.getLocations();
            String path = stepLocations.get(0).getPath();
            if (pathToSourceMap.containsKey(path)) {
                keyword = getKeywordFromSource(path, stepLocations);
            }
        }
        return keyword != null ? keyword : getFirstGivenKeyword(dialectProvider.getDefaultDialect());
    }

    private String getKeywordFromSource(String path, List<PickleLocation> stepLocations) {
        if (!pathToStepMap.containsKey(path)) {
            createFeatureStepMap(path);
        }
        if (!pathToStepMap.containsKey(path)) {
            return null;
        }
        GherkinDialect featureDialect = pathToStepMap.get(path).dialect;
        List<String> givenThenWhenKeywords = getGivenWhenThenKeywords(featureDialect);
        Map<Integer, StepNode> stepMap = pathToStepMap.get(path).stepMap;
        for (PickleLocation stepLocation : stepLocations) {
            if (!stepMap.containsKey(stepLocation.getLine())) {
                continue;
            }
            for (StepNode stepNode = stepMap.get(stepLocation.getLine()); stepNode != null; stepNode = stepNode.previous) {
                for (String keyword : givenThenWhenKeywords) {
                    if (!keyword.equals("* ") && keyword == stepNode.step.getKeyword()) {
                        return convertToCodeKeyword(keyword);
                    }
                }
            }
        }
        return getFirstGivenKeyword(featureDialect);
    }

    private void createFeatureStepMap(String path) {
        if (!pathToSourceMap.containsKey(path)) {
            return;
        }
        Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
        TokenMatcher matcher = new TokenMatcher();
        try {
            GherkinDocument gherkinDocument = parser.parse(pathToSourceMap.get(path), matcher);
            Map<Integer, StepNode> stepMap = new HashMap<Integer, StepNode>();
            StepNode initialPreviousNode = null;
            for (ScenarioDefinition child : gherkinDocument.getFeature().getChildren()) {
                StepNode lastStepNode = processScenarioDefinition(stepMap, initialPreviousNode, child);
                if (child instanceof Background) {
                    initialPreviousNode = lastStepNode;
                }
            }
            pathToStepMap.put(path, new FeatureStepMap(new GherkinDialectProvider(gherkinDocument.getFeature().getLanguage()).getDefaultDialect(), stepMap));
        } catch (ParserException e) {
            // Ignore exceptions
        }
    }

    private StepNode processScenarioDefinition(Map<Integer, StepNode> stepMap, StepNode initialPreviousNode, ScenarioDefinition child) {
        StepNode previousNode = initialPreviousNode;
        for (Step step : child.getSteps()) {
            StepNode stepNode = new StepNode(step, previousNode);
            stepMap.put(step.getLocation().getLine(), stepNode);
            previousNode = stepNode;
        }
        return previousNode;
    }

    private List<String> getGivenWhenThenKeywords(GherkinDialect dialect) {
        Map<String, List<String>> keywordsMap;
        try { // TODO: Fix when Gherkin provide a getter for the keywords.
            Field f;
            f = dialect.getClass().getDeclaredField("keywords");
            f.setAccessible(true);
            keywordsMap = (Map<String, List<String>>) f.get(dialect);
        } catch (Exception e) {
            return Collections.<String>emptyList();
        }
        List<String> keywords = new ArrayList<String>();
        keywords.addAll(keywordsMap.get("given"));
        keywords.addAll(keywordsMap.get("when"));
        keywords.addAll(keywordsMap.get("then"));
        return keywords;
    }

    private String getFirstGivenKeyword(GherkinDialect i18n) {
        try { // TODO: Fix when Gherkin provide a getter for the keywords.
            Field f;
            f = i18n.getClass().getDeclaredField("keywords");
            f.setAccessible(true);
            Map<String, List<String>> keywordsMap = (Map<String, List<String>>) f.get(i18n);
            for (String keyword : keywordsMap.get("given")) {
                if (!keyword.equals("* ")) {
                    return convertToCodeKeyword(keyword);
                }
            }
        } catch (Exception e) {}
        return null;
    }

    private String convertToCodeKeyword(String keyword) {
        return keyword.replaceAll("[\\s',!]", "");
    }

    private class FeatureStepMap {
        public final GherkinDialect dialect;
        public final Map<Integer, StepNode> stepMap;

        public FeatureStepMap(GherkinDialect dialect, Map<Integer, StepNode> stepMap) {
            this.dialect = dialect;
            this.stepMap = stepMap;
        }
    }

    private class StepNode {
        public final Step step;
        public final StepNode previous;

        public StepNode(Step step, StepNode previous) {
            this.step = step;
            this.previous = previous;
        }
    }
}
