package io.cucumber.testng;

import org.apiguardian.api.API;

import java.net.URI;
import java.util.List;

import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Step;

/**
 * Wraps CucumberPickle to avoid exposing it as part of the public api.
 */
@API(status = API.Status.STABLE)
public final class Pickle implements io.cucumber.core.gherkin.Pickle {

    private final io.cucumber.core.gherkin.Pickle pickle;

    Pickle(io.cucumber.core.gherkin.Pickle pickle) {
        this.pickle = pickle;
    }

    io.cucumber.core.gherkin.Pickle getPickle() {
        return pickle;
    }

    @Override
    public String getKeyword() {
        return pickle.getKeyword();
    }

    @Override
    public String getLanguage() {
        return pickle.getLanguage();
    }

    @Override
    public String getName() {
        return pickle.getName();
    }

    @Override
    public Location getLocation() {
        return pickle.getLocation();
    }

    @Override
    public Location getScenarioLocation() {
        return pickle.getScenarioLocation();
    }

    @Override
    public List<Step> getSteps() {
        return pickle.getSteps();
    }

    @Override
    public List<String> getTags() {
        return pickle.getTags();
    }

    @Override
    public URI getUri() {
        return pickle.getUri();
    }

    @Override
    public String getId() {
        return pickle.getId();
    }
}
