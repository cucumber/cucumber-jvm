package io.cucumber.jupiter.engine;

import gherkin.ast.ScenarioOutline;
import io.cucumber.core.feature.CucumberFeature;
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

    static TestDescriptor createOutlineDescriptor(CucumberFeature feature, TestDescriptor parent) {
        FeatureOrigin source = FeatureOrigin.fromUri(feature.getUri());
        UniqueId uniqueId = source.featureSegment(parent.getUniqueId(), feature);
        TestSource testSource = source.featureSource();
        TestDescriptor featureDescriptor = new FeatureDescriptor(uniqueId, feature.getName(), testSource, feature);
        addFeatureElements(feature, source, featureDescriptor);
        return featureDescriptor;
    }

    private static void addFeatureElements(CucumberFeature feature, FeatureOrigin source, TestDescriptor featureDescriptor) {
        feature.getGherkinFeature().getChildren().forEach(scenarioDefinition -> {
            if (scenarioDefinition instanceof ScenarioOutline) {
                ScenarioOutline scenarioOutline = (ScenarioOutline) scenarioDefinition;
                featureDescriptor.addChild(ScenarioOutlineDescriptor.createOutlineDescriptor(feature, scenarioOutline, source, featureDescriptor));
            } else {
                featureDescriptor.addChild(PickleDescriptor.createScenario(feature, scenarioDefinition, source, featureDescriptor));

            }
        });
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
