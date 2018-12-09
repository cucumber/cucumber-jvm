package io.cucucumber.jupiter.engine;

import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FeatureDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final CucumberFeature feature;

    private FeatureDescriptor(UniqueId uniqueId, String name, TestSource source, CucumberFeature feature) {
        super(uniqueId, name, source);
        this.feature = feature;
    }

    static TestDescriptor create(CucumberFeature feature, TestDescriptor parent) {
        FeatureOrigin source = FeatureOrigin.fromUri(URI.create(feature.getUri()));
        UniqueId uniqueId = source.featureSegment(parent.getUniqueId(), feature);
        TestSource testSource = source.featureSource(feature);
        TestDescriptor featureDescriptor = new FeatureDescriptor(uniqueId, feature.getName(), testSource, feature);
        addFeatureElements(compileFeature(feature), source, featureDescriptor);
        return featureDescriptor;
    }

    private static void addFeatureElements(Collection<List<PickleEvent>> picklesPerScenario, FeatureOrigin source, TestDescriptor featureDescriptor) {
        picklesPerScenario.spliterator().forEachRemaining(pickleEvents -> {
            if (isScenario(pickleEvents)) {
                PickleEvent pickle = pickleEvents.get(0);
                featureDescriptor.addChild(PickleDescriptor.createScenario(pickle, source, featureDescriptor));
            } else {
                featureDescriptor.addChild(ScenarioOutlineDescriptor.create(pickleEvents, source, featureDescriptor));
            }
        });
    }

    private static Collection<List<PickleEvent>> compileFeature(CucumberFeature feature) {
        // A scenarioSource with examples compiles into multiple pickle
        // We group these pickle by their original location
        Map<Integer, List<PickleEvent>> picklesPerScenario = new LinkedHashMap<>();
        for (PickleEvent pickle : feature.getPickles()) {
            List<PickleLocation> locations = pickle.pickle.getLocations();
            int scenarioLocation = locations.get(locations.size() - 1).getLine();
            picklesPerScenario.putIfAbsent(scenarioLocation, new ArrayList<>());
            picklesPerScenario.get(scenarioLocation).add(pickle);
        }

        return picklesPerScenario.values();
    }

    private static boolean isScenario(List<PickleEvent> pickleEvents) {
        return pickleEvents.size() == 1;
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
