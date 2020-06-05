package io.cucumber.java8;

import org.apiguardian.api.API;

@FunctionalInterface
@API(status = API.Status.STABLE)
public interface HookBody {

    void accept(Scenario scenario) throws Throwable;

}
