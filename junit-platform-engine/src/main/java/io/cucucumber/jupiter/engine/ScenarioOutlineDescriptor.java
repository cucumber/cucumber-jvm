package io.cucucumber.jupiter.engine;

import gherkin.events.PickleEvent;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.util.List;

import static io.cucucumber.jupiter.engine.PickleDescriptor.createExample;

class ScenarioOutlineDescriptor extends AbstractTestDescriptor {
    static TestDescriptor create(List<PickleEvent> pickles, FeatureOrigin source, TestDescriptor parent) {
        PickleEvent outlinePickle = pickles.get(0);
        UniqueId uniqueId = source.outlineSegment(parent.getUniqueId(), pickles);
        TestSource testSource = source.outlineSource(pickles);
        TestDescriptor descriptor = new ScenarioOutlineDescriptor(uniqueId, outlinePickle.pickle.getName(), testSource);

        int index = 1;
        for (PickleEvent pickleEvent : pickles) {
            descriptor.addChild(createExample(pickleEvent, index++, source, descriptor));
        }

        return descriptor;
    }

    private ScenarioOutlineDescriptor(UniqueId uniqueId, String name, TestSource source) {
        super(uniqueId, name, source);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

}
