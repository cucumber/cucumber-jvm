package io.cucucumber.jupiter.engine;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

class CucumberEngineDescriptor extends EngineDescriptor implements Node<CucumberEngineExecutionContext> {

    CucumberEngineDescriptor(UniqueId uniqueId) {
        super(uniqueId, "Cucumber JVM");
    }

    @Override
    public CucumberEngineExecutionContext before(CucumberEngineExecutionContext context) {
        context.startTestRun();
        return context;
    }

    @Override
    public void after(CucumberEngineExecutionContext context) {
        context.finishTestRun();
    }
}
