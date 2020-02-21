package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.plugin.event.Node.ScenarioOutline;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

class FeatureDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final Feature feature;

    private FeatureDescriptor(UniqueId uniqueId, String name, TestSource source, Feature feature) {
        super(uniqueId, name, source);
        this.feature = feature;
    }

    static TestDescriptor create(Feature feature, TestDescriptor parent) {
        FeatureOrigin source = FeatureOrigin.fromUri(feature.getUri());
        TestDescriptor descriptor = new FeatureDescriptor(
            source.featureSegment(parent.getUniqueId(), feature),
            getNameOrKeyWord(feature),
            source.featureSource(),
            feature
        );
        parent.addChild(descriptor);
        feature.elements().forEach(scenarioDefinition -> visit(feature, descriptor, source, scenarioDefinition));
        return descriptor;
    }

    private static void visit(Feature feature, TestDescriptor parent, FeatureOrigin source, io.cucumber.plugin.event.Node node) {
        if (node instanceof io.cucumber.plugin.event.Node.Scenario) {
            feature.getPickleAt(node)
                .ifPresent(pickle -> {
                    PickleDescriptor descriptor = new PickleDescriptor(
                        source.scenarioSegment(parent.getUniqueId(), node),
                        getNameOrKeyWord(node),
                        source.nodeSource(node),
                        pickle
                    );
                    parent.addChild(descriptor);
                });
        }

        if (node instanceof io.cucumber.plugin.event.Node.Rule) {
            NodeDescriptor descriptor = new NodeDescriptor(
                source.ruleSegment(parent.getUniqueId(), node),
                getNameOrKeyWord(node),
                source.nodeSource(node)
            );
            parent.addChild(descriptor);
            io.cucumber.plugin.event.Node.Rule rule = (io.cucumber.plugin.event.Node.Rule) node;
            rule.elements().forEach(section -> visit(feature, descriptor, source, section));
        }

        if (node instanceof ScenarioOutline) {
            NodeDescriptor descriptor = new NodeDescriptor(
                source.scenarioSegment(parent.getUniqueId(), node),
                getNameOrKeyWord(node),
                source.nodeSource(node)
            );
            parent.addChild(descriptor);
            ScenarioOutline scenarioOutline = (ScenarioOutline) node;
            scenarioOutline.elements().forEach(section -> visit(feature, descriptor, source, section));
        }

        if (node instanceof io.cucumber.plugin.event.Node.Examples) {
            NodeDescriptor descriptor = new NodeDescriptor(
                source.examplesSegment(parent.getUniqueId(), node),
                getNameOrKeyWord(node),
                source.nodeSource(node)
            );
            parent.addChild(descriptor);
            io.cucumber.plugin.event.Node.Examples examples = (io.cucumber.plugin.event.Node.Examples) node;
            examples.elements().forEach(example -> visit(feature, descriptor, source, example));
        }

        if (node instanceof io.cucumber.plugin.event.Node.Example) {
            feature.getPickleAt(node)
                .ifPresent(pickle -> {
                    PickleDescriptor descriptor = new PickleDescriptor(
                        source.exampleSegment(parent.getUniqueId(), node),
                        getNameOrKeyWord(node),
                        source.nodeSource(node),
                        pickle
                    );
                    parent.addChild(descriptor);
                });
        }

    }

    private static String getNameOrKeyWord(io.cucumber.plugin.event.Node node) {
        return node.getName().isEmpty() ? node.getKeyword() : node.getName();
    }

    @Override
    public CucumberEngineExecutionContext prepare(CucumberEngineExecutionContext context) {
        context.beforeFeature(feature);
        return context;
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

}
