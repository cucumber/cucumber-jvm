package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.resource.ClasspathSupport;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.config.PrefixedConfigurationParameters;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.cucumber.junit.platform.engine.Constants.EXECUTION_EXCLUSIVE_RESOURCES_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.READ_SUFFIX;
import static io.cucumber.junit.platform.engine.Constants.READ_WRITE_SUFFIX;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;

class PickleDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final Pickle pickleEvent;
    private final Set<TestTag> tags;
    private final Set<ExclusiveResource> exclusiveResources = new LinkedHashSet<>(0);

    PickleDescriptor(
            ConfigurationParameters parameters, UniqueId uniqueId, String name, TestSource source, Pickle pickleEvent
    ) {
        super(uniqueId, name, source);
        this.pickleEvent = pickleEvent;
        this.tags = getTags(pickleEvent);
        this.tags.forEach(tag -> {
            ExclusiveResourceOptions exclusiveResourceOptions = new ExclusiveResourceOptions(parameters, tag);
            exclusiveResourceOptions.exclusiveReadWriteResource()
                    .map(resource -> new ExclusiveResource(resource, LockMode.READ_WRITE))
                    .forEach(exclusiveResources::add);
            exclusiveResourceOptions.exclusiveReadResource()
                    .map(resource -> new ExclusiveResource(resource, LockMode.READ))
                    .forEach(exclusiveResources::add);
        });
    }

    private Set<TestTag> getTags(Pickle pickleEvent) {
        return pickleEvent.getTags().stream()
                .map(tag -> tag.substring(1))
                .filter(TestTag::isValid)
                .map(TestTag::create)
                // Retain input order
                .collect(collectingAndThen(toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public SkipResult shouldBeSkipped(CucumberEngineExecutionContext context) {
        return Stream.of(shouldBeSkippedByTagFilter(context), shouldBeSkippedByNameFilter(context))
                .flatMap(skipResult -> skipResult.map(Stream::of).orElseGet(Stream::empty))
                .filter(SkipResult::isSkipped)
                .findFirst()
                .orElseGet(SkipResult::doNotSkip);
    }

    private Optional<SkipResult> shouldBeSkippedByTagFilter(CucumberEngineExecutionContext context) {
        return context.getOptions().tagFilter().map(expression -> {
            if (expression.evaluate(pickleEvent.getTags())) {
                return SkipResult.doNotSkip();
            }
            return SkipResult
                    .skip(
                        "'" + Constants.FILTER_TAGS_PROPERTY_NAME + "=" + expression + "' did not match this scenario");
        });
    }

    private Optional<SkipResult> shouldBeSkippedByNameFilter(CucumberEngineExecutionContext context) {
        return context.getOptions().nameFilter().map(pattern -> {
            if (pattern.matcher(pickleEvent.getName()).matches()) {
                return SkipResult.doNotSkip();
            }
            return SkipResult
                    .skip("'" + Constants.FILTER_NAME_PROPERTY_NAME + "=" + pattern + "' did not match this scenario");
        });
    }

    @Override
    public CucumberEngineExecutionContext execute(
            CucumberEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor
    ) {
        context.runTestCase(pickleEvent);
        return context;
    }

    @Override
    public Set<ExclusiveResource> getExclusiveResources() {
        return exclusiveResources;
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
        return tags;
    }

    Optional<String> getPackage() {
        return getSource()
                .filter(ClasspathResourceSource.class::isInstance)
                .map(ClasspathResourceSource.class::cast)
                .map(ClasspathResourceSource::getClasspathResourceName)
                .map(ClasspathSupport::packageNameOfResource);
    }

    private static final class ExclusiveResourceOptions {

        private final ConfigurationParameters parameters;

        ExclusiveResourceOptions(ConfigurationParameters parameters, TestTag tag) {
            this.parameters = new PrefixedConfigurationParameters(
                parameters,
                EXECUTION_EXCLUSIVE_RESOURCES_PREFIX + tag.getName());
        }

        public Stream<String> exclusiveReadWriteResource() {
            return parameters.get(READ_WRITE_SUFFIX, s -> Arrays.stream(s.split(","))
                    .map(String::trim))
                    .orElse(Stream.empty());
        }

        public Stream<String> exclusiveReadResource() {
            return parameters.get(READ_SUFFIX, s -> Arrays.stream(s.split(","))
                    .map(String::trim))
                    .orElse(Stream.empty());
        }

    }

}
