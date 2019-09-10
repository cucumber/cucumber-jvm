package io.cucumber.jupiter.engine;

import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class FeatureDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final CucumberFeature feature;

    private FeatureDescriptor(UniqueId uniqueId, String name, TestSource source, CucumberFeature feature) {
        super(uniqueId, name, source);
        this.feature = feature;
    }

    static TestDescriptor create(CucumberFeature feature, TestDescriptor parent) {
        FeatureOrigin source = FeatureOrigin.fromUri(feature.getUri());
        UniqueId uniqueId = source.featureSegment(parent.getUniqueId(), feature);
        TestSource testSource = source.featureSource(feature);
        TestDescriptor featureDescriptor = new FeatureDescriptor(uniqueId, feature.getName(), testSource, feature);
        addFeatureElements(compileFeature(feature), source, featureDescriptor);
        return featureDescriptor;
    }

    private static void addFeatureElements(Collection<List<CucumberPickle>> picklesPerScenario, FeatureOrigin source, TestDescriptor featureDescriptor) {
        picklesPerScenario.spliterator().forEachRemaining(pickleEvents -> {
            if (isScenario(pickleEvents)) {
                CucumberPickle pickle = pickleEvents.get(0);
                featureDescriptor.addChild(PickleDescriptor.createScenario(pickle, source, featureDescriptor));
            } else {
                featureDescriptor.addChild(ScenarioOutlineDescriptor.create(pickleEvents, source, featureDescriptor));
            }
        });
    }

    private static Collection<List<CucumberPickle>> compileFeature(CucumberFeature feature) {
        // A scenarioSource with examples compiles into multiple pickle
        // We group these pickle by their original location
        Map<Integer, List<CucumberPickle>> picklesPerScenario = new LinkedHashMap<>();
        for (CucumberPickle pickle : feature.getPickles()) {
            picklesPerScenario.putIfAbsent(pickle.getScenarioLine(), new ArrayList<>());
            picklesPerScenario.get(pickle.getScenarioLine()).add(pickle);
        }

        return picklesPerScenario.values();
    }

    private static boolean isScenario(List<CucumberPickle> pickleEvents) {
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
