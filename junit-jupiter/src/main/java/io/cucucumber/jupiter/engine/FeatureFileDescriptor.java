package io.cucucumber.jupiter.engine;

import cucumber.runtime.model.CucumberFeature;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

public class FeatureFileDescriptor extends AbstractTestDescriptor{
    public FeatureFileDescriptor(UniqueId uniqueId, CucumberFeature feature, TestSource source) {
        super(uniqueId, feature.getGherkinFeature().getFeature().getName(), source);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }


}
