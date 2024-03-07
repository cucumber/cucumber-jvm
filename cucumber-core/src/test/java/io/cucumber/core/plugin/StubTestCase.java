package io.cucumber.core.plugin;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStep;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public class StubTestCase implements TestCase {
    private final URI uri;
    private final Location location;

    public StubTestCase() {
        this.uri = null;
        this.location = null;
    }

    public StubTestCase(URI uri, Location location) {
        this.uri = uri;
        this.location = location;
    }

    @Override
    public Integer getLine() {
        return null;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getKeyword() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getScenarioDesignation() {
        return null;
    }

    @Override
    public List<String> getTags() {
        return null;
    }

    @Override
    public List<TestStep> getTestSteps() {
        return null;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public UUID getId() {
        return null;
    }
}
