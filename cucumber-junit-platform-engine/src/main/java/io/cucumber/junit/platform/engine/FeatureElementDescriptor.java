package io.cucumber.junit.platform.engine;

import io.cucumber.plugin.event.Location;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.Node;

import java.net.URI;

abstract class FeatureElementDescriptor extends AbstractCucumberTestDescriptor
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
