package cucumber.api.event;

import cucumber.runtime.model.CucumberFeature;

import java.util.List;

public final class TestRunStarted extends TimeStampedEvent {
    public final List<CucumberFeature> features;

    public TestRunStarted(Long timeStamp, List<CucumberFeature> features) {
        super(timeStamp);
        this.features = features;
    }
}
