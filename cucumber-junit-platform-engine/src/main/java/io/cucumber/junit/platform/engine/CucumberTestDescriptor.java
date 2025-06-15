package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.plugin.event.Location;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.junit.platform.engine.support.hierarchical.Node;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

abstract class CucumberTestDescriptor extends AbstractTestDescriptor {

    protected CucumberTestDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
        super(uniqueId, displayName, source);
    }

    protected abstract URI getUri();

    protected abstract Location getLocation();

    static class FeatureDescriptor extends CucumberTestDescriptor implements Node<CucumberEngineExecutionContext> {

        private final Feature feature;

        FeatureDescriptor(UniqueId uniqueId, String name, TestSource source, Feature feature) {
            super(uniqueId, name, source);
            this.feature = feature;
        }

        Feature getFeature() {
            return feature;
        }

        @Override
        public CucumberEngineExecutionContext prepare(CucumberEngineExecutionContext context) {
            context.beforeFeature(feature);
            return context;
        }

        @Override
        public Type getType() {
            return Type.CONTAINER;
        }

        @Override
        protected URI getUri() {
            return feature.getUri();
        }

        @Override
        protected Location getLocation() {
            return feature.getLocation();
        }
    }

    abstract static class FeatureElementDescriptor extends CucumberTestDescriptor
            implements Node<CucumberEngineExecutionContext> {

        private final CucumberConfiguration configuration;
        private final io.cucumber.plugin.event.Node element;

        FeatureElementDescriptor(
                CucumberConfiguration configuration, UniqueId uniqueId, String name, TestSource source,
                io.cucumber.plugin.event.Node element
        ) {
            super(uniqueId, name, source);
            this.configuration = configuration;
            this.element = element;
        }

        @Override
        public ExecutionMode getExecutionMode() {
            return configuration.getExecutionModeFeature();
        }

        @Override
        protected Location getLocation() {
            return element.getLocation();
        }

        @Override
        protected URI getUri() {
            return element.getUri();
        }

        static final class ExamplesDescriptor extends FeatureElementDescriptor {

            ExamplesDescriptor(
                    CucumberConfiguration configuration, UniqueId uniqueId, String name, TestSource source,
                    io.cucumber.plugin.event.Node element
            ) {
                super(configuration, uniqueId, name, source, element);
            }

            @Override
            public Type getType() {
                return Type.CONTAINER;
            }

        }

        static final class RuleDescriptor extends FeatureElementDescriptor {

            RuleDescriptor(
                    CucumberConfiguration configuration, UniqueId uniqueId, String name, TestSource source,
                    io.cucumber.plugin.event.Node element
            ) {
                super(configuration, uniqueId, name, source, element);
            }

            @Override
            public Type getType() {
                return Type.CONTAINER;
            }

        }

        static final class ScenarioOutlineDescriptor extends FeatureElementDescriptor {

            ScenarioOutlineDescriptor(
                    CucumberConfiguration configuration, UniqueId uniqueId, String name,
                    TestSource source, io.cucumber.plugin.event.Node element
            ) {
                super(configuration, uniqueId, name, source, element);
            }

            @Override
            public Type getType() {
                return Type.CONTAINER;
            }

        }
    }

    static final class PickleDescriptor extends CucumberTestDescriptor implements Node<CucumberEngineExecutionContext> {

        private final Pickle pickle;
        private final CucumberConfiguration configuration;

        PickleDescriptor(
                CucumberConfiguration configuration, UniqueId uniqueId, String name, TestSource source,
                Pickle pickle
        ) {
            super(uniqueId, name, source);
            this.configuration = configuration;
            this.pickle = pickle;
        }

        Pickle getPickle() {
            return pickle;
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
            return context.getConfiguration().tagFilter().map(expression -> {
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
            return context.getConfiguration().nameFilter().map(pattern -> {
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
            return getTags().stream()
                    .map(tag -> configuration.getExclusiveResourceConfiguration(tag.getName()))
                    .flatMap(ExclusiveResourceConfiguration::getExclusiveResources)
                    .collect(toSet());
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
            return pickle.getTags().stream()
                    .map(tag -> tag.substring(1))
                    .filter(TestTag::isValid)
                    .map(TestTag::create)
                    // Retain input order
                    .collect(collectingAndThen(toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
        }

        @Override
        protected URI getUri() {
            return pickle.getUri();
        }

        @Override
        protected Location getLocation() {
            return pickle.getLocation();
        }

        @Override
        public ExecutionMode getExecutionMode() {
            return configuration.getExecutionModeFeature();
        }
    }
}
