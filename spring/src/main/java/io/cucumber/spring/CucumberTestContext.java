package io.cucumber.spring;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public final class CucumberTestContext {
    public static final String SCOPE_CUCUMBER_GLUE = "cucumber-glue";

    private CucumberTestContext() {
    }
}
