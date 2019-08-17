package io.cucumber.testng;

import io.cucumber.core.feature.CucumberPickle;
import org.apiguardian.api.API;

/**
 * Wraps CucumberPickle to avoid exposing it as part of the public api.
 */
@API(status = API.Status.STABLE)
public final class Pickle {

    private final CucumberPickle cucumberPickle;

    Pickle(CucumberPickle cucumberPickle) {
        this.cucumberPickle = cucumberPickle;
    }

    CucumberPickle getCucumberPickle() {
        return cucumberPickle;
    }
}
