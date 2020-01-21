package io.cucumber.testng;

import org.apiguardian.api.API;

import java.net.URI;
import java.util.List;

import io.cucumber.core.gherkin.Location;

/**
 * Wraps CucumberPickle to avoid exposing it as part of the public api.
 */
@API(status = API.Status.STABLE)
public final class Pickle {

    private final io.cucumber.core.gherkin.Pickle pickle;

    Pickle(io.cucumber.core.gherkin.Pickle pickle) {
        this.pickle = pickle;
    }

    io.cucumber.core.gherkin.Pickle getPickle() {
        return pickle;
    }

    public String getName() {
        return pickle.getName();
    }

    public Location getScenarioLocation() {
        return pickle.getScenarioLocation();
    }

    public List<String> getTags() {
        return pickle.getTags();
    }

    public URI getUri() {
        return pickle.getUri();
    }

}
