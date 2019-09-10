package io.cucumber.jupiter.engine;

import io.cucumber.core.feature.CucumberPickle;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.util.List;

import static io.cucumber.jupiter.engine.PickleDescriptor.createExample;

class ScenarioOutlineDescriptor extends AbstractTestDescriptor {

    private ScenarioOutlineDescriptor(UniqueId uniqueId, String name, TestSource source) {
        super(uniqueId, name, source);
    }

    static TestDescriptor create(List<CucumberPickle> pickles, FeatureOrigin source, TestDescriptor parent) {
        CucumberPickle outlinePickle = pickles.get(0);
        UniqueId uniqueId = source.outlineSegment(parent.getUniqueId(), pickles);
        TestSource testSource = source.outlineSource(pickles);
        TestDescriptor descriptor = new ScenarioOutlineDescriptor(uniqueId, outlinePickle.getName(), testSource);

        int index = 1;
        for (CucumberPickle pickleEvent : pickles) {
            descriptor.addChild(createExample(pickleEvent, index++, source, descriptor));
        }

        return descriptor;
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

}
