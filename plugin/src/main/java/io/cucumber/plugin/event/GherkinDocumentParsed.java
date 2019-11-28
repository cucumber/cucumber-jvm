package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;

@API(status = API.Status.EXPERIMENTAL)
public final class GherkinDocumentParsed extends TimeStampedEvent {
    private final CucumberFeature feature;

    public GherkinDocumentParsed(Instant instant, CucumberFeature feature) {
        super(instant);
        this.feature = feature;
    }

    public CucumberFeature getFeature() {
        return feature;
    }
}
