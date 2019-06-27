package io.cucumber.core.runtime;

import io.cucumber.core.api.event.TestSourceRead;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

public class TestFeatureSupplier implements FeatureSupplier {
    private final EventBus bus ;
    private final List<CucumberFeature> features;

    public TestFeatureSupplier(EventBus bus, CucumberFeature... features) {
        this(bus, Arrays.asList(features));
    }

    public TestFeatureSupplier(EventBus bus, List<CucumberFeature> features) {
        this.bus = bus;
        this.features = features;
    }

    @Override
    public List<CucumberFeature> get() {
        for (CucumberFeature feature : features) {
            bus.send(new TestSourceRead(bus.getInstant(), feature.getUri().toString(), feature.getSource()));
        }
        return features;
    }
}
