package io.cucucumber.jupiter.engine;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public class CucumberEngineDescriptor extends EngineDescriptor implements Node<CucumberEngineExecutionContext> {

	public CucumberEngineDescriptor(UniqueId uniqueId) {
		super(uniqueId, "Cucumber JVM");
	}

	@Override
	public CucumberEngineExecutionContext prepare(CucumberEngineExecutionContext context) {
        EngineExecutionListener executionListener = context.getExecutionListener();
        return new CucumberEngineExecutionContext(executionListener, context.getConfigurationParameters());
	}

	@Override
	public void cleanUp(CucumberEngineExecutionContext context) throws Exception {
	}
}
