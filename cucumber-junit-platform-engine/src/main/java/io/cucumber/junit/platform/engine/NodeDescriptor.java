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
import org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.cucumber.junit.platform.engine.Constants.EXECUTION_EXCLUSIVE_RESOURCES_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_MODE_FEATURE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.READ_SUFFIX;
import static io.cucumber.junit.platform.engine.Constants.READ_WRITE_SUFFIX;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

abstract class NodeDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final ExecutionMode executionMode;

    NodeDescriptor(ConfigurationParameters parameters, UniqueId uniqueId, String name, TestSource source) {
        super(uniqueId, name, source);
        this.executionMode = parameters
                .get(EXECUTION_MODE_FEATURE_PROPERTY_NAME,
                    value -> ExecutionMode.valueOf(value.toUpperCase(Locale.US)))
                .orElse(ExecutionMode.CONCURRENT);
    }

    @Override
    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    static final class ExamplesDescriptor extends NodeDescriptor {

        ExamplesDescriptor(ConfigurationParameters parameters, UniqueId uniqueId, String name, TestSource source) {
            super(parameters, uniqueId, name, source);
        }

        @Override
        public Type getType() {
            return Type.CONTAINER;
        }

    }

    static final class RuleDescriptor extends NodeDescriptor {

        RuleDescriptor(ConfigurationParameters parameters, UniqueId uniqueId, String name, TestSource source) {
            super(parameters, uniqueId, name, source);
        }

        @Override
        public Type getType() {
            return Type.CONTAINER;
        }

    }

    static final class ScenarioOutlineDescriptor extends NodeDescriptor {

        ScenarioOutlineDescriptor(
                ConfigurationParameters parameters, UniqueId uniqueId, String name,
                TestSource source
        ) {
            super(parameters, uniqueId, name, source);
        }

        @Override
        public Type getType() {
            return Type.CONTAINER;
        }

    }

    static final class PickleDescriptor extends NodeDescriptor {

        private final Pickle pickle;
        private final Set<TestTag> tags;
        private final Set<ExclusiveResource> exclusiveResources = new LinkedHashSet<>(0);

        PickleDescriptor(
                ConfigurationParameters parameters, UniqueId uniqueId, String name, TestSource source,
                Pickle pickle
        ) {
            super(parameters, uniqueId, name, source);
            this.pickle = pickle;
            this.tags = getTags(pickle);
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

        Pickle getPickle() {
            return pickle;
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
                if (expression.evaluate(pickle.getTags())) {
                    return SkipResult.doNotSkip();
                }
                return SkipResult
                        .skip(
                            "'" + Constants.FILTER_TAGS_PROPERTY_NAME + "=" + expression
                                    + "' did not match this scenario");
            });
        }

        private Optional<SkipResult> shouldBeSkippedByNameFilter(CucumberEngineExecutionContext context) {
            return context.getOptions().nameFilter().map(pattern -> {
                if (pattern.matcher(pickle.getName()).matches()) {
                    return SkipResult.doNotSkip();
                }
                return SkipResult
                        .skip("'" + Constants.FILTER_NAME_PROPERTY_NAME + "=" + pattern
                                + "' did not match this scenario");
            });
        }

        @Override
        public CucumberEngineExecutionContext execute(
                CucumberEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor
        ) {
            context.runTestCase(pickle);
            return context;
        }

        @Override
        public Set<ExclusiveResource> getExclusiveResources() {
            return exclusiveResources;
        }

        /**
         * Returns the set of {@linkplain TestTag tags} for a pickle.
         * <p>
         * Note that Cucumber will remove the {code @} symbol from all Gherkin
         * tags. So a scenario tagged with {@code @Smoke} becomes a test tagged
         * with {@code Smoke}.
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

}
