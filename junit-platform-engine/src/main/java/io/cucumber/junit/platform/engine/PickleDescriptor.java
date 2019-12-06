package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.resource.ClasspathSupport;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

class PickleDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final Pickle pickleEvent;

    PickleDescriptor(UniqueId uniqueId, String name, TestSource source, Pickle pickleEvent) {
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

    /**
     * Returns the set of {@linkplain TestTag tags} for a pickle.
     * <p>
     * Note that Cucumber will remove the {code @} symbol from all Gherkin tags.
     * So a scenario tagged with {@code @Smoke} becomes a test tagged with
     * {@code Smoke}.
     *
     * @return the set of tags
     */
    @Override
    public Set<TestTag> getTags() {
        return pickleEvent.getTags().stream()
            .map(tag -> tag.substring(1))
            .filter(TestTag::isValid)
            .map(TestTag::create)
            // Retain input order
            .collect(collectingAndThen(toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
    }

    Optional<String> getPackage() {
        return getSource()
            .filter(ClasspathResourceSource.class::isInstance)
            .map(ClasspathResourceSource.class::cast)
            .map(ClasspathResourceSource::getClasspathResourceName)
            .map(ClasspathSupport::packageNameOfResource);
    }

}
