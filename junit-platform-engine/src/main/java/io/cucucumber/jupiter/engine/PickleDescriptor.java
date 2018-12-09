package io.cucucumber.jupiter.engine;

import gherkin.events.PickleEvent;
import gherkin.pickles.PickleTag;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PickleDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final PickleEvent pickleEvent;

    private PickleDescriptor(UniqueId uniqueId, String name, TestSource source, PickleEvent pickleEvent) {
        super(uniqueId, name, source);
        this.pickleEvent = pickleEvent;
    }

    static PickleDescriptor createExample(PickleEvent pickleEvent, int index, FeatureOrigin source, TestDescriptor parent) {
        UniqueId uniqueId = source.exampleSegment(parent.getUniqueId(), pickleEvent);
        TestSource testSource = source.exampleSource(pickleEvent);
        return new PickleDescriptor(uniqueId, "Example #" + index, testSource, pickleEvent);
    }

    static PickleDescriptor createScenario(PickleEvent pickle, FeatureOrigin source, TestDescriptor parent) {
        UniqueId uniqueId = source.scenarioSegment(parent.getUniqueId(), pickle);
        TestSource testSource = source.scenarioSource(pickle);
        return new PickleDescriptor(uniqueId, pickle.pickle.getName(), testSource, pickle);
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

    public Optional<String> getPackage() {
        return getSource()
            .filter(ClasspathResourceSource.class::isInstance)
            .map(ClasspathResourceSource.class::cast)
            .map(ClasspathResourceSource::getClasspathResourceName)
            .map(ClasspathSupport::packageNameOfResource);
    }

}
