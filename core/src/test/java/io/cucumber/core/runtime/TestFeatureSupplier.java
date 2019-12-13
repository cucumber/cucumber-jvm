package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.plugin.event.TestSourceRead;

import java.util.Arrays;
import java.util.List;

public class TestFeatureSupplier implements FeatureSupplier {
    private final EventBus bus ;
    private final List<Feature> features;

    public TestFeatureSupplier(EventBus bus, Feature... features) {
        this(bus, Arrays.asList(features));
    }

    public TestFeatureSupplier(EventBus bus, List<Feature> features) {
        this.bus = bus;
        this.features = features;
    }

    @Override
    public List<Feature> get() {
        for (Feature feature : features) {
            bus.send(new TestSourceRead(bus.getInstant(), feature.getUri(), feature.getSource()));
        }
        return features;
    }
}
