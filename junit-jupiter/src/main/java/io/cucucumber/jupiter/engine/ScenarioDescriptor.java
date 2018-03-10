package io.cucucumber.jupiter.engine;

import cucumber.api.event.EventHandler;
import cucumber.api.event.TestCaseFinished;
import gherkin.events.PickleEvent;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public class ScenarioDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final PickleEvent pickleEvent;

    ScenarioDescriptor(UniqueId uniqueId, PickleEvent pickleEvent, TestSource source) {
        super(uniqueId, pickleEvent.pickle.getName(), source);
        this.pickleEvent = pickleEvent;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public CucumberEngineExecutionContext execute(CucumberEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
        context.runPickle(pickleEvent);
        return context;
    }

    @Override
    public void after(CucumberEngineExecutionContext context) throws Exception {

    }
}
