package io.cucumber.core.plugin;

import io.cucumber.core.gherkin.ScenarioDefinition;
import io.cucumber.gherkin.Gherkin;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.*;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples;
import io.cucumber.messages.internal.com.google.protobuf.GeneratedMessageV3;
import io.cucumber.plugin.event.TestSourceRead;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.cucumber.gherkin.Gherkin.makeSourceEnvelope;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

final class TestSourcesModel {
    private final Map<URI, TestSourceRead> pathToReadEventMap = new HashMap<>();
    private final Map<URI, GherkinDocument> pathToAstMap = new HashMap<>();
    private final Map<URI, Map<Integer, AstNode>> pathToNodeMap = new HashMap<>();

    private static Feature getFeatureForTestCase(AstNode astNode) {
        while (astNode.parent != null) {
            astNode = astNode.parent;
        }
        return (Feature) astNode.node;
    }

    static Background getBackgroundForTestCase(AstNode astNode) {
        Feature feature = getFeatureForTestCase(astNode);
        return feature.getChildrenList()
            .stream()
            .filter(FeatureChild::hasBackground)
            .map(FeatureChild::getBackground)
            .findFirst()
            .orElse(null);
    }

    static Scenario getScenarioDefinition(AstNode astNode) {
        return astNode.node instanceof Scenario ? (Scenario) astNode.node : (Scenario) astNode.parent.parent.node;
    }

    static boolean isBackgroundStep(AstNode astNode) {
        return astNode.parent.node instanceof Background;
    }

    static String calculateId(AstNode astNode) {
        GeneratedMessageV3 node = astNode.node;
        if (node instanceof Scenario) {
            return calculateId(astNode.parent) + ";" + convertToId(((Scenario) node).getName());
        }
        if (node instanceof TableRow) {
            return calculateId(astNode.parent) + ";" + 1;
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

    static URI relativize(URI uri) {
        if (!"file".equals(uri.getScheme())) {
            return uri;
        }
        if (!uri.isAbsolute()) {
            return uri;
        }

        try {
            URI root = new File("").toURI();
            URI relative = root.relativize(uri);
            // Scheme is lost by relativize
            return new URI("file", relative.getSchemeSpecificPart(), relative.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    void addTestSourceReadEvent(URI path, TestSourceRead event) {
        pathToReadEventMap.put(path, event);
    }

    Feature getFeature(URI path) {
        if (!pathToAstMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToAstMap.containsKey(path)) {
            return pathToAstMap.get(path).getFeature();
        }
        return null;
    }

    AstNode getAstNode(URI path, int line) {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToNodeMap.containsKey(path)) {
            return pathToNodeMap.get(path).get(line);
        }
        return null;
    }

    boolean hasBackground(URI path, int line) {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToNodeMap.containsKey(path)) {
            AstNode astNode = pathToNodeMap.get(path).get(line);
            return getBackgroundForTestCase(astNode) != null;
        }
        return false;
    }

    private void parseGherkinSource(URI path) {
        if (!pathToReadEventMap.containsKey(path)) {
            return;
        }
        String source = pathToReadEventMap.get(path).getSource();

        List<Messages.Envelope> sources = singletonList(
            makeSourceEnvelope(source, path.toString())
        );

        List<Messages.Envelope> envelopes = Gherkin.fromSources(
            sources,
            true,
            true,
            true,
            () -> String.valueOf(UUID.randomUUID())
        ).collect(toList());

        GherkinDocument gherkinDocument = envelopes.stream()
            .filter(Messages.Envelope::hasGherkinDocument)
            .map(Messages.Envelope::getGherkinDocument)
            .findFirst()
            .orElse(null);

        pathToAstMap.put(path, gherkinDocument);
        Map<Integer, AstNode> nodeMap = new HashMap<>();
        AstNode currentParent = new AstNode(gherkinDocument.getFeature(), null);
        for (FeatureChild child : gherkinDocument.getFeature().getChildrenList()) {
            processScenarioDefinition(nodeMap, child, currentParent);
        }
        pathToNodeMap.put(path, nodeMap);

    }

    private void processScenarioDefinition(Map<Integer, AstNode> nodeMap, FeatureChild child, AstNode currentParent) {
        if (child.hasBackground()) {
            AstNode childNode = new AstNode(child.getBackground(), currentParent);
            nodeMap.put(child.getBackground().getLocation().getLine(), childNode);
        } else if (child.hasScenario()) {
            AstNode childNode = new AstNode(child.getScenario(), currentParent);
            nodeMap.put(child.getScenario().getLocation().getLine(), childNode);
            for (Step step : child.getScenario().getStepsList()) {
                nodeMap.put(step.getLocation().getLine(), new AstNode(step, childNode));
            }
            if (child.getScenario().getExamplesCount() > 0) {
                processScenarioOutlineExamples(nodeMap, child.getScenario(), childNode);
            }
        }

    }

    private void processScenarioOutlineExamples(Map<Integer, AstNode> nodeMap, Scenario scenarioOutline, AstNode parent) {
        for (Examples examples : scenarioOutline.getExamplesList()) {
            AstNode examplesNode = new AstNode(examples, parent);
            TableRow headerRow = examples.getTableHeader();
            AstNode headerNode = new AstNode(headerRow, examplesNode);
            nodeMap.put(headerRow.getLocation().getLine(), headerNode);
            for (int i = 0; i < examples.getTableBodyCount(); ++i) {
                TableRow examplesRow = examples.getTableBody(i);
                AstNode expandedScenarioNode = new AstNode(examplesRow, examplesNode);
                nodeMap.put(examplesRow.getLocation().getLine(), expandedScenarioNode);
            }
        }
    }

    static class AstNode {
        final GeneratedMessageV3 node;
        final AstNode parent;

        AstNode(GeneratedMessageV3 node, AstNode parent) {
            this.node = node;
            this.parent = parent;
        }
    }
}
