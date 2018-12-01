package io.cucucumber.jupiter.engine;

import gherkin.events.PickleEvent;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

class PickleDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final PickleEvent pickleEvent;

    PickleDescriptor(UniqueId uniqueId, String name, TestSource source, PickleEvent pickleEvent) {
        super(uniqueId, name, source);
        this.pickleEvent = pickleEvent;
    }

    PickleDescriptor(UniqueId scenarioId, TestSource pickleSource, PickleEvent pickle) {
        this(scenarioId, pickle.pickle.getName(), pickleSource, pickle);
    }

    static String pickleId(PickleEvent pickle) {
        return String.valueOf(pickle.pickle.getLocations().get(0).getLine());
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
}
