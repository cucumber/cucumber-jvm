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

    FeatureFileDescriptor(UniqueId uniqueId, CucumberFeature feature, TestSource source) {
        super(uniqueId, feature.getGherkinFeature().getFeature().getName(), source);
        this.feature = feature;
    }

    void addScenario(CucumberFeature feature, PickleEvent pickle, boolean inPackage) {
        UniqueId scenarioId = getUniqueId().append("scenario", PickleDescriptor.pickleId(pickle));
        TestDescriptor scenarioDescriptor = new PickleDescriptor(scenarioId, fromPickle(feature, pickle), pickle, inPackage);
        addChild(scenarioDescriptor);
    }

    void addScenarioOutline(CucumberFeature feature, List<PickleEvent> pickleEvents, boolean inPackage) {
        PickleEvent pickle = pickleEvents.get(0);
        List<PickleLocation> locations = pickle.pickle.getLocations();
        PickleLocation scenarioLocation = locations.get(locations.size() - 1);
        UniqueId scenarioOutlineId = getUniqueId().append("outline", String.valueOf(scenarioLocation.getLine()));
        String scenarioName = pickle.pickle.getName();
        ScenarioOutlineDescriptor scenarioOutlineDescriptor = new ScenarioOutlineDescriptor(scenarioOutlineId, scenarioName, fromOutline(feature, pickle), inPackage);
        scenarioOutlineDescriptor.addExamples(feature, pickleEvents);
        addChild(scenarioOutlineDescriptor);
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
