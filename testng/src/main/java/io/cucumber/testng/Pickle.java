package io.cucumber.testng;

import org.apiguardian.api.API;

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
}
