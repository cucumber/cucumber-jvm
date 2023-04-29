package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.Optional;
import java.util.function.Consumer;

class CucumberEngineDescriptor extends EngineDescriptor implements Node<CucumberEngineExecutionContext> {

    static final String ENGINE_ID = "cucumber";

    CucumberEngineDescriptor(UniqueId uniqueId) {
        super(uniqueId, "Cucumber");
    }

    @Override
    public CucumberEngineExecutionContext prepare(CucumberEngineExecutionContext context) {
        return ifChildren(context, CucumberEngineExecutionContext::startTestRun);
    }

    @Override
    public CucumberEngineExecutionContext before(CucumberEngineExecutionContext context) {
        return ifChildren(context, CucumberEngineExecutionContext::runBeforeAllHooks);
    }

    @Override
    public void after(CucumberEngineExecutionContext context) {
        ifChildren(context, CucumberEngineExecutionContext::runAfterAllHooks);
    }

    @Override
    public void cleanUp(CucumberEngineExecutionContext context) {
        ifChildren(context, CucumberEngineExecutionContext::finishTestRun);
    }

    /*
     * Problem: The JUnit Platform will always execute all engines that
     * participated in discovery. In combination with the JUnit Platform Suite
     * Engine this may result in CucumberEngine being executed multiple times.
     * To ensure Cucumber only performs works if/when there are tests to run we
     * don't do anything unless there are tests. I.e. only when this test
     * descriptor has children.
     */
    private CucumberEngineExecutionContext ifChildren(
            CucumberEngineExecutionContext context, Consumer<CucumberEngineExecutionContext> action
    ) {
        if (!getChildren().isEmpty()) {
            action.accept(context);
        }
        return context;
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
                        .forEach(child -> recursivelyMerge(child, existingParent)));
        }
    }
}
