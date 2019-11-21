package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.CucumberExample;
import io.cucumber.core.feature.CucumberExamples;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberScenario;
import io.cucumber.core.feature.CucumberScenarioOutline;
import io.cucumber.core.feature.Located;
import io.cucumber.core.feature.Named;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

class FeatureDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final CucumberFeature feature;

    private FeatureDescriptor(UniqueId uniqueId, String name, TestSource source, CucumberFeature feature) {
        super(uniqueId, name, source);
        this.feature = feature;
    }

    static TestDescriptor create(CucumberFeature cucumberFeature, TestDescriptor parent) {
        FeatureOrigin source = FeatureOrigin.fromUri(cucumberFeature.getUri());
        TestDescriptor descriptor = new FeatureDescriptor(
            source.featureSegment(parent.getUniqueId(), cucumberFeature),
            getNameOrKeyWord(cucumberFeature),
            source.featureSource(),
            cucumberFeature
        );
        parent.addChild(descriptor);
        cucumberFeature.children().forEach(scenarioDefinition -> visit(cucumberFeature, descriptor, source, scenarioDefinition));
        return descriptor;
    }

    private static <T extends Located & Named> void visit(CucumberFeature feature, TestDescriptor parent, FeatureOrigin source, T node) {
        if (node instanceof CucumberScenario) {
            feature.getPickleAt(node.getLocation())
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

        if (node instanceof CucumberScenarioOutline) {
            NodeDescriptor descriptor = new NodeDescriptor(
                source.scenarioSegment(parent.getUniqueId(), node),
                getNameOrKeyWord(node),
                source.nodeSource(node)
            );
            parent.addChild(descriptor);
            CucumberScenarioOutline scenarioOutline = (CucumberScenarioOutline) node;
            scenarioOutline.children().forEach(section -> visit(feature, descriptor, source, section));
        }

        if (node instanceof CucumberExamples) {
            NodeDescriptor descriptor = new NodeDescriptor(
                source.examplesSegment(parent.getUniqueId(), node),
                getNameOrKeyWord(node),
                source.nodeSource(node)
            );
            parent.addChild(descriptor);
            CucumberExamples examples = (CucumberExamples) node;
            examples.children().forEach(example -> visit(feature, descriptor, source, example));
        }

        if (node instanceof CucumberExample) {
            feature.getPickleAt(node.getLocation())
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

    private static <T extends Located & Named> String getNameOrKeyWord(T node) {
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
