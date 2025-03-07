package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.plugin.event.Location;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
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

final class PickleDescriptor extends AbstractCucumberTestDescriptor implements Node<CucumberEngineExecutionContext> {

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
     * Note that Cucumber will remove the {code @} symbol from all Gherkin tags.
     * So a scenario tagged with {@code @Smoke} becomes a test tagged with
     * {@code Smoke}.
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
