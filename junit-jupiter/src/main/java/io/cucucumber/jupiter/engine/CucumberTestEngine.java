package io.cucucumber.jupiter.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;

import java.util.Optional;

public final class CucumberTestEngine extends HierarchicalTestEngine<CucumberEngineExecutionContext> {

    @Override
	public String getId() {
		return "cucumber";
	}

	@Override
	public Optional<String> getGroupId() {
		return Optional.of("io.cucucumber");
	}

	@Override
	public Optional<String> getArtifactId() {
		return Optional.of("cucucumber-junit-jupiter");
	}


	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		CucumberEngineDescriptor engineDescriptor = new CucumberEngineDescriptor(uniqueId);
		new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	@Override
	protected CucumberEngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new CucumberEngineExecutionContext(
		    request.getEngineExecutionListener(),
			request.getConfigurationParameters()
        );
	}
}
