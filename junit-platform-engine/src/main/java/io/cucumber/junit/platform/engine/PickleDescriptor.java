package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.resource.ClasspathSupport;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class PickleDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final CucumberPickle pickleEvent;

    PickleDescriptor(UniqueId uniqueId, String name, TestSource source, CucumberPickle pickleEvent) {
        super(uniqueId, name, source);
        this.pickleEvent = pickleEvent;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public CucumberEngineExecutionContext execute(CucumberEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
        context.runTestCase(pickleEvent);
        return context;
    }

    @Override
    public Set<TestTag> getTags() {
        return pickleEvent.getTags().stream()
            .filter(TestTag::isValid)
            .map(TestTag::create)
            .collect(Collectors.toSet());
    }

    Optional<String> getPackage() {
        return getSource()
            .filter(ClasspathResourceSource.class::isInstance)
            .map(ClasspathResourceSource.class::cast)
            .map(ClasspathResourceSource::getClasspathResourceName)
            .map(ClasspathSupport::packageNameOfResource);
    }

}
