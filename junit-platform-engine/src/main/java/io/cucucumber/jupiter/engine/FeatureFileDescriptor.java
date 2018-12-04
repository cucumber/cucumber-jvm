package io.cucucumber.jupiter.engine;

import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.List;

import static io.cucucumber.jupiter.engine.FeatureSource.fromOutline;
import static io.cucucumber.jupiter.engine.FeatureSource.fromPickle;

class FeatureFileDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {
    private final CucumberFeature feature;

    FeatureFileDescriptor(UniqueId uniqueId, String name, TestSource source, CucumberFeature feature) {
        super(uniqueId, name, source);
        this.feature = feature;
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
