package io.cucucumber.jupiter.engine;

import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.util.List;

import static io.cucucumber.jupiter.engine.FeatureSource.fromPickle;

class ScenarioOutlineDescriptor extends AbstractTestDescriptor {

    ScenarioOutlineDescriptor(UniqueId uniqueId, String name, TestSource source) {
        super(uniqueId, name, source);
    }

    void addExamples(CucumberFeature feature, List<PickleEvent> pickleEvents) {
        int index = 1;
        for (PickleEvent pickleEvent : pickleEvents) {
            UniqueId scenarioId = getUniqueId().append("example", DiscoverySelectorResolver.pickleId(pickleEvent));
            TestDescriptor scenarioDescriptor = new PickleDescriptor(scenarioId, "Example #" + index++, fromPickle(feature, pickleEvent), pickleEvent);
            addChild(scenarioDescriptor);
        }
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

}
