package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

class NodeDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final CucumberEngineOptions options;

    NodeDescriptor(ConfigurationParameters parameters, UniqueId uniqueId, String name, TestSource source) {
        super(uniqueId, name, source);
        this.options = new CucumberEngineOptions(parameters);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public ExecutionMode getExecutionMode() {
        return this.options.getExecutionModeForScenario();
    }
}
