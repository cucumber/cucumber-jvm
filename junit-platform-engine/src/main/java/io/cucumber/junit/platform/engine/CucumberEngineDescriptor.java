package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.Optional;

class CucumberEngineDescriptor extends EngineDescriptor implements Node<CucumberEngineExecutionContext> {

    CucumberEngineDescriptor(UniqueId uniqueId) {
        super(uniqueId, "Cucumber");
    }

    @Override
    public CucumberEngineExecutionContext before(CucumberEngineExecutionContext context) {
        context.emitMetaAndStartTestRun();
        return context;
    }

    @Override
    public void after(CucumberEngineExecutionContext context) {
        context.finishTestRun();
    }

    void mergeFeature(FeatureDescriptor descriptor) {
        recursivelyMerge(descriptor, this);
    }

    private static void recursivelyMerge(TestDescriptor descriptor, TestDescriptor parent) {
        Optional<? extends TestDescriptor> byUniqueId = parent.findByUniqueId(descriptor.getUniqueId());
        if (!byUniqueId.isPresent()) {
            parent.addChild(descriptor);
        } else {
            byUniqueId.ifPresent(
                existingParent -> descriptor.getChildren()
                    .forEach(child -> recursivelyMerge(child, existingParent))
            );
        }
    }
}
