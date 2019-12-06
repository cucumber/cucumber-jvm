package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Rule;
import io.cucumber.core.gherkin.Scenario;
import io.cucumber.core.gherkin.ScenarioOutline;
import io.cucumber.core.gherkin.Located;
import io.cucumber.core.gherkin.Named;
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
        feature.children().forEach(scenarioDefinition -> visit(feature, descriptor, source, scenarioDefinition));
        return descriptor;
    }

    private static <T extends Located & Named> void visit(Feature feature, TestDescriptor parent, FeatureOrigin source, T node) {
        if (node instanceof Scenario) {
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

        if (node instanceof Rule) {
            NodeDescriptor descriptor = new NodeDescriptor(
                source.ruleSegment(parent.getUniqueId(), node),
                getNameOrKeyWord(node),
                source.nodeSource(node)
            );
            parent.addChild(descriptor);
            Rule rule = (Rule) node;
            rule.children().forEach(section -> visit(feature, descriptor, source, section));
        }

        if (node instanceof ScenarioOutline) {
            NodeDescriptor descriptor = new NodeDescriptor(
                source.scenarioSegment(parent.getUniqueId(), node),
                getNameOrKeyWord(node),
                source.nodeSource(node)
            );
            parent.addChild(descriptor);
            ScenarioOutline scenarioOutline = (ScenarioOutline) node;
            scenarioOutline.children().forEach(section -> visit(feature, descriptor, source, section));
        }

        if (node instanceof Examples) {
            NodeDescriptor descriptor = new NodeDescriptor(
                source.examplesSegment(parent.getUniqueId(), node),
                getNameOrKeyWord(node),
                source.nodeSource(node)
            );
            parent.addChild(descriptor);
            Examples examples = (Examples) node;
            examples.children().forEach(example -> visit(feature, descriptor, source, example));
        }

        if (node instanceof Example) {
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

    private static <T extends Named> String getNameOrKeyWord(T node) {
        String name = node.getName();
        return name.isEmpty() ? node.getKeyWord() : name;
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
