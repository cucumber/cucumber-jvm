package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.plugin.event.Location;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.Node;

import java.net.URI;

class FeatureDescriptor extends AbstractCucumberTestDescriptor implements Node<CucumberEngineExecutionContext> {

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

    @Override
    protected URI getUri() {
        return feature.getUri();
    }

    @Override
    protected Location getLocation() {
        return feature.getLocation();
    }
}
