package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Feature;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

class FeatureDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final Feature feature;

    FeatureDescriptor(UniqueId uniqueId, String name, TestSource source, Feature feature) {
        super(uniqueId, name, source);
        this.feature = feature;
    }

    private static void pruneRecursively(TestDescriptor descriptor, Predicate<TestDescriptor> toKeep) {
        if (!toKeep.test(descriptor)) {
            if (descriptor.isTest()) {
                descriptor.removeFromHierarchy();
            }
            List<TestDescriptor> children = new ArrayList<>(descriptor.getChildren());
            children.forEach(child -> pruneRecursively(child, toKeep));
        }
    }

    void prune(Predicate<TestDescriptor> toKeep) {
        pruneRecursively(this, toKeep);
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
