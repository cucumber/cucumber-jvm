package cucumber.runtime.formatter;

import cucumber.api.event.TestSourceRead;
import gherkin.AstBuilder;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
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

import java.util.HashMap;
import java.util.Map;

final class TestSourcesModel {
    private final Map<String, TestSourceRead> pathToReadEventMap = new HashMap<String, TestSourceRead>();
    private final Map<String, GherkinDocument> pathToAstMap = new HashMap<String, GherkinDocument>();
    private final Map<String, Map<Integer, AstNode>> pathToNodeMap = new HashMap<String, Map<Integer, AstNode>>();

    static Feature getFeatureForTestCase(AstNode astNode) {
        while (astNode.parent != null) {
            astNode = astNode.parent;
        }
        return (Feature) astNode.node;
    }

    static Background getBackgroundForTestCase(AstNode astNode) {
        Feature feature = getFeatureForTestCase(astNode);
        ScenarioDefinition backgound = feature.getChildren().get(0);
        if (backgound instanceof Background) {
            return (Background) backgound;
        } else {
            return null;
        }
    }

    static ScenarioDefinition getScenarioDefinition(AstNode astNode) {
        return astNode.node instanceof ScenarioDefinition ? (ScenarioDefinition) astNode.node : (ScenarioDefinition) astNode.parent.parent.node;
    }

    static boolean isScenarioOutlineScenario(AstNode astNode) {
        return !(astNode.node instanceof ScenarioDefinition);
    }

    static boolean isBackgroundStep(AstNode astNode) {
        return astNode.parent.node instanceof Background;
    }

    static String calculateId(AstNode astNode) {
        Node node = astNode.node;
        if (node instanceof ScenarioDefinition) {
            return calculateId(astNode.parent) + ";" + convertToId(((ScenarioDefinition) node).getName());
        }
        if (node instanceof ExamplesRowWrapperNode) {
            return calculateId(astNode.parent) + ";" + Integer.toString(((ExamplesRowWrapperNode) node).bodyRowIndex + 2);
        }
        if (node instanceof TableRow) {
            return calculateId(astNode.parent) + ";" + Integer.toString(1);
        }
        if (node instanceof Examples) {
            return calculateId(astNode.parent) + ";" + convertToId(((Examples) node).getName());
        }
        if (node instanceof Feature) {
            return convertToId(((Feature) node).getName());
        }
        return "";
    }

    static String convertToId(String name) {
        return name.replaceAll("[\\s'_,!]", "-").toLowerCase();
    }

    void addTestSourceReadEvent(String path, TestSourceRead event) {
        pathToReadEventMap.put(path, event);
    }

    Feature getFeature(String path) {
        if (!pathToAstMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToAstMap.containsKey(path)) {
            return pathToAstMap.get(path).getFeature();
        }
        return null;
    }

    ScenarioDefinition getScenarioDefinition(String path, int line) {
        return getScenarioDefinition(getAstNode(path, line));
    }

    AstNode getAstNode(String path, int line) {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToNodeMap.containsKey(path)) {
            return pathToNodeMap.get(path).get(line);
        }
        return null;
    }

    boolean hasBackground(String path, int line) {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToNodeMap.containsKey(path)) {
            AstNode astNode = pathToNodeMap.get(path).get(line);
            return getBackgroundForTestCase(astNode) != null;
        }
        return false;
    }

    String getKeywordFromSource(String uri, int stepLine) {
        Feature feature = getFeature(uri);
        if (feature != null) {
            TestSourceRead event = getTestSourceReadEvent(uri);
            String trimmedSourceLine = event.source.split("\n")[stepLine - 1].trim();
            GherkinDialect dialect = new GherkinDialectProvider(feature.getLanguage()).getDefaultDialect();
            for (String keyword : dialect.getStepKeywords()) {
                if (trimmedSourceLine.startsWith(keyword)) {
                    return keyword;
                }
            }
        }
        return "";
    }

    private TestSourceRead getTestSourceReadEvent(String uri) {
        if (pathToReadEventMap.containsKey(uri)) {
            return pathToReadEventMap.get(uri);
        }
        return null;
    }

    String getFeatureName(String uri) {
        Feature feature = getFeature(uri);
        if (feature != null) {
            return feature.getName();
        }
        return "";
    }

    private void parseGherkinSource(String path) {
        if (!pathToReadEventMap.containsKey(path)) {
            return;
        }
        Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
        TokenMatcher matcher = new TokenMatcher();
        try {
            GherkinDocument gherkinDocument = parser.parse(pathToReadEventMap.get(path).source, matcher);
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
            processScenarioOutlineExamples(nodeMap, (ScenarioOutline) child, childNode);
        }
    }

    private void processScenarioOutlineExamples(Map<Integer, AstNode> nodeMap, ScenarioOutline scenarioOutline, AstNode childNode) {
        for (Examples examples : scenarioOutline.getExamples()) {
            AstNode examplesNode = new AstNode(examples, childNode);
            TableRow headerRow = examples.getTableHeader();
            AstNode headerNode = new AstNode(headerRow, examplesNode);
            nodeMap.put(headerRow.getLocation().getLine(), headerNode);
            for (int i = 0; i < examples.getTableBody().size(); ++i) {
                TableRow examplesRow = examples.getTableBody().get(i);
                Node rowNode = new ExamplesRowWrapperNode(examplesRow, i);
                AstNode expandedScenarioNode = new AstNode(rowNode, examplesNode);
                nodeMap.put(examplesRow.getLocation().getLine(), expandedScenarioNode);
            }
        }
    }

    class ExamplesRowWrapperNode extends Node {
        final int bodyRowIndex;

        ExamplesRowWrapperNode(Node examplesRow, int bodyRowIndex) {
            super(examplesRow.getLocation());
            this.bodyRowIndex = bodyRowIndex;
        }
    }

    class AstNode {
        final Node node;
        final AstNode parent;

        AstNode(Node node, AstNode parent) {
            this.node = node;
            this.parent = parent;
        }
    }
}
