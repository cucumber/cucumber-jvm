package io.cucumber.core.plugin;

import io.cucumber.gherkin.Gherkin;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Background;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.RuleChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow;
import io.cucumber.messages.internal.com.google.protobuf.GeneratedMessageV3;
import io.cucumber.messages.internal.com.google.protobuf.Message;
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

    static Scenario getScenarioDefinition(AstNode astNode) {
        AstNode candidate = astNode;
        while (candidate != null && !(candidate.node instanceof Scenario)) {
            candidate = candidate.parent;
        }
        return candidate == null ? null : (Scenario) candidate.node;
    }

    static boolean isBackgroundStep(AstNode astNode) {
        return astNode.parent.node instanceof Background;
    }

    static String calculateId(AstNode astNode) {
        GeneratedMessageV3 node = astNode.node;
        if (node instanceof Scenario) {
            return calculateId(astNode.parent) + ";" + convertToId(((Scenario) node).getName());
        }
        if (node instanceof ExamplesRowWrapperNode) {
            return calculateId(astNode.parent) + ";" + (((ExamplesRowWrapperNode) node).bodyRowIndex + 2);
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

    private void parseGherkinSource(URI path) {
        if (!pathToReadEventMap.containsKey(path)) {
            return;
        }
        String source = pathToReadEventMap.get(path).getSource();

        List<Messages.Envelope> sources = singletonList(
            makeSourceEnvelope(source, path.toString()));

        List<Messages.Envelope> envelopes = Gherkin.fromSources(
            sources,
            true,
            true,
            true,
            () -> String.valueOf(UUID.randomUUID())).collect(toList());

        GherkinDocument gherkinDocument = envelopes.stream()
                .filter(Messages.Envelope::hasGherkinDocument)
                .map(Messages.Envelope::getGherkinDocument)
                .findFirst()
                .orElse(null);

        pathToAstMap.put(path, gherkinDocument);
        Map<Integer, AstNode> nodeMap = new HashMap<>();
        AstNode currentParent = new AstNode(gherkinDocument.getFeature(), null);
        for (FeatureChild child : gherkinDocument.getFeature().getChildrenList()) {
            processFeatureDefinition(nodeMap, child, currentParent);
        }
        pathToNodeMap.put(path, nodeMap);

    }

    private void processFeatureDefinition(Map<Integer, AstNode> nodeMap, FeatureChild child, AstNode currentParent) {
        if (child.hasBackground()) {
            processBackgroundDefinition(nodeMap, child.getBackground(), currentParent);
        } else if (child.hasScenario()) {
            processScenarioDefinition(nodeMap, child.getScenario(), currentParent);
        } else if (child.hasRule()) {
            AstNode childNode = new AstNode(child.getRule(), currentParent);
            nodeMap.put(child.getRule().getLocation().getLine(), childNode);
            for (RuleChild ruleChild : child.getRule().getChildrenList()) {
                processRuleDefinition(nodeMap, ruleChild, childNode);
            }
        }
    }

    private void processBackgroundDefinition(
            Map<Integer, AstNode> nodeMap, Background background, AstNode currentParent
    ) {
        AstNode childNode = new AstNode(background, currentParent);
        nodeMap.put(background.getLocation().getLine(), childNode);
        for (Step step : background.getStepsList()) {
            nodeMap.put(step.getLocation().getLine(), new AstNode(step, childNode));
        }
    }

    private void processScenarioDefinition(Map<Integer, AstNode> nodeMap, Scenario child, AstNode currentParent) {
        AstNode childNode = new AstNode(child, currentParent);
        nodeMap.put(child.getLocation().getLine(), childNode);
        for (Step step : child.getStepsList()) {
            nodeMap.put(step.getLocation().getLine(), new AstNode(step, childNode));
        }
        if (child.getExamplesCount() > 0) {
            processScenarioOutlineExamples(nodeMap, child, childNode);
        }
    }

    private void processRuleDefinition(Map<Integer, AstNode> nodeMap, RuleChild child, AstNode currentParent) {
        if (child.hasBackground()) {
            processBackgroundDefinition(nodeMap, child.getBackground(), currentParent);
        } else if (child.hasScenario()) {
            processScenarioDefinition(nodeMap, child.getScenario(), currentParent);
        }
    }

    private void processScenarioOutlineExamples(
            Map<Integer, AstNode> nodeMap, Scenario scenarioOutline, AstNode parent
    ) {
        for (Examples examples : scenarioOutline.getExamplesList()) {
            AstNode examplesNode = new AstNode(examples, parent);
            TableRow headerRow = examples.getTableHeader();
            AstNode headerNode = new AstNode(headerRow, examplesNode);
            nodeMap.put(headerRow.getLocation().getLine(), headerNode);
            for (int i = 0; i < examples.getTableBodyCount(); ++i) {
                TableRow examplesRow = examples.getTableBody(i);
                GeneratedMessageV3 rowNode = new ExamplesRowWrapperNode(examplesRow, i);
                AstNode expandedScenarioNode = new AstNode(rowNode, examplesNode);
                nodeMap.put(examplesRow.getLocation().getLine(), expandedScenarioNode);
            }
        }
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

    static Background getBackgroundForTestCase(AstNode astNode) {
        Feature feature = getFeatureForTestCase(astNode);
        return feature.getChildrenList()
                .stream()
                .filter(FeatureChild::hasBackground)
                .map(FeatureChild::getBackground)
                .findFirst()
                .orElse(null);
    }

    private static Feature getFeatureForTestCase(AstNode astNode) {
        while (astNode.parent != null) {
            astNode = astNode.parent;
        }
        return (Feature) astNode.node;
    }

    static class ExamplesRowWrapperNode extends GeneratedMessageV3 {

        final int bodyRowIndex;

        ExamplesRowWrapperNode(GeneratedMessageV3 examplesRow, int bodyRowIndex) {
            this.bodyRowIndex = bodyRowIndex;
        }

        @Override
        protected FieldAccessorTable internalGetFieldAccessorTable() {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        protected Message.Builder newBuilderForType(BuilderParent builderParent) {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public Message.Builder newBuilderForType() {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public Message.Builder toBuilder() {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public Message getDefaultInstanceForType() {
            throw new UnsupportedOperationException("not implemented");
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
