package io.cucucumber.jupiter.engine;

import gherkin.events.PickleEvent;
import gherkin.pickles.PickleTag;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.Set;
import java.util.stream.Collectors;

class PickleDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final PickleEvent pickleEvent;
    private final boolean inPackage;

    PickleDescriptor(UniqueId uniqueId, String name, TestSource source, PickleEvent pickleEvent, boolean inPackage) {
        super(uniqueId, name, source);
        this.pickleEvent = pickleEvent;
        this.inPackage = inPackage;
    }

    PickleDescriptor(UniqueId scenarioId, TestSource pickleSource, PickleEvent pickle, boolean inPackage) {
        this(scenarioId, pickle.pickle.getName(), pickleSource, pickle, inPackage);
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public CucumberEngineExecutionContext execute(CucumberEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
        context.runPickle(pickleEvent);
        return context;
    }

    @Override
    public Set<TestTag> getTags() {
        return pickleEvent.pickle.getTags().stream()
            .map(PickleTag::getName)
            .filter(TestTag::isValid)
            .map(TestTag::create)
            .collect(Collectors.toSet());
    }

    public String getPackage() {
        if (!inPackage) {
            return null;
        }
        String uri = pickleEvent.uri;
        int lastPathSeparator = uri.lastIndexOf('/');
        if (lastPathSeparator < 0) {
            return uri;
        }
        return uri.substring(0, lastPathSeparator).replaceAll("/", ".");
    }
}
