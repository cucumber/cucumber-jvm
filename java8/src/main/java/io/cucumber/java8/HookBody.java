package io.cucumber.java8;

import cucumber.api.Scenario;

@FunctionalInterface
public interface HookBody {
    void accept(Scenario scenario) throws Throwable;
}
