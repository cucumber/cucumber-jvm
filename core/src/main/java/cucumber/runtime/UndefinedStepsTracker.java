package cucumber.runtime;

import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.TestStep;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.SnippetsSuggestedEvent;
import cucumber.api.event.TestSourceRead;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UndefinedStepsTracker implements EventListener {
    private final List<String> snippets = new ArrayList<String>();
    private final IGherkinDialectProvider dialectProvider = new GherkinDialectProvider();
    private final Map<String, String> pathToSourceMap = new HashMap<String, String>();
    private final Map<String, FeatureStepMap> pathToStepMap = new HashMap<String, FeatureStepMap>();
    private boolean hasUndefinedSteps = false;
    private String currentUri;

    private EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
        @Override
        public void receive(TestSourceRead event) {
            pathToSourceMap.put(event.path, event.source);
        }
    };
    private EventHandler<SnippetsSuggestedEvent> snippetsSuggestedHandler = new EventHandler<SnippetsSuggestedEvent>() {
        @Override
        public void receive(SnippetsSuggestedEvent event) {
            handleSnippetsSuggested(event.uri, event.stepLocations, event.snippets);
        }
    };

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(SnippetsSuggestedEvent.class, snippetsSuggestedHandler);
    }

    public boolean hasUndefinedSteps() {
        return hasUndefinedSteps;
    }

    public List<String> getSnippets() {
        return snippets;
    }

    void handleSnippetsSuggested(String uri, List<PickleLocation> stepLocations, List<String> snippets) {
        hasUndefinedSteps = true;
        String keyword = givenWhenThenKeyword(uri, stepLocations);
        for (String rawSnippet : snippets) {
            String snippet = rawSnippet.replace("**KEYWORD**", keyword);
            if (!this.snippets.contains(snippet)) {
                this.snippets.add(snippet);
            }
        }
    }

    private String givenWhenThenKeyword(String uri, List<PickleLocation> stepLocations) {
        String keyword = null;
        if (!stepLocations.isEmpty()) {
            if (pathToSourceMap.containsKey(uri)) {
                keyword = getKeywordFromSource(uri, stepLocations);
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
        List<String> keywords = new ArrayList<String>();
        keywords.addAll(dialect.getGivenKeywords());
        keywords.addAll(dialect.getWhenKeywords());
        keywords.addAll(dialect.getThenKeywords());
        return keywords;
    }

    private String getFirstGivenKeyword(GherkinDialect i18n) {
        for (String keyword : i18n.getGivenKeywords()) {
            if (!keyword.equals("* ")) {
                return convertToCodeKeyword(keyword);
            }
        }
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
