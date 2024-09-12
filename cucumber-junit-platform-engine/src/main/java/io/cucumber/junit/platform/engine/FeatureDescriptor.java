package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Feature;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

class FeatureDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final Feature feature;

    FeatureDescriptor(UniqueId uniqueId, String name, TestSource source, Feature feature) {
        super(uniqueId, name, source);
        this.feature = feature;
    }

    Feature getFeature() {
        return feature;
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
