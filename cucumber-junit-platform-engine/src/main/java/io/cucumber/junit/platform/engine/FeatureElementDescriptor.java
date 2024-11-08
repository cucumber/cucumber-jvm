package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Pickle;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

abstract class FeatureElementDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final ExecutionMode executionMode;
    private final int line;

    FeatureElementDescriptor(
            CucumberConfiguration configuration, UniqueId uniqueId, String name, TestSource source, int line
    ) {
        super(uniqueId, name, source);
        this.executionMode = configuration.getExecutionModeFeature();
        this.line = line;
    }

    @Override
    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    int getLine() {
        return line;
    }

    static final class ExamplesDescriptor extends FeatureElementDescriptor {

        ExamplesDescriptor(
                CucumberConfiguration configuration, UniqueId uniqueId, String name, TestSource source, int line
        ) {
            super(configuration, uniqueId, name, source, line);
        }

        @Override
        public Type getType() {
            return Type.CONTAINER;
        }

    }

    static final class RuleDescriptor extends FeatureElementDescriptor {

        RuleDescriptor(
                CucumberConfiguration configuration, UniqueId uniqueId, String name, TestSource source, int line
        ) {
            super(configuration, uniqueId, name, source, line);
        }

        @Override
        public Type getType() {
            return Type.CONTAINER;
        }

    }

    static final class ScenarioOutlineDescriptor extends FeatureElementDescriptor {

        ScenarioOutlineDescriptor(
                CucumberConfiguration configuration, UniqueId uniqueId, String name,
                TestSource source, int line
        ) {
            super(configuration, uniqueId, name, source, line);
        }

        @Override
        public Type getType() {
            return Type.CONTAINER;
        }

    }

    static final class PickleDescriptor extends FeatureElementDescriptor {

        private final Pickle pickle;
        private final Set<TestTag> tags;
        private final Set<ExclusiveResource> exclusiveResources = new LinkedHashSet<>(0);

        PickleDescriptor(
                CucumberConfiguration configuration, UniqueId uniqueId, String name, TestSource source,
                int line, Pickle pickle
        ) {
            super(configuration, uniqueId, name, source, line);
            this.pickle = pickle;
            this.tags = getTags(pickle);
            this.tags.forEach(tag -> {
                ExclusiveResourceConfiguration exclusiveResourceConfiguration = configuration
                        .getExclusiveResourceConfiguration(tag.getName());
                exclusiveResourceConfiguration.exclusiveReadWriteResource()
                        .map(resource -> new ExclusiveResource(resource, LockMode.READ_WRITE))
                        .forEach(exclusiveResources::add);
                exclusiveResourceConfiguration.exclusiveReadResource()
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

    }

}
