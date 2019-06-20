package io.cucumber.java8;

import io.cucumber.core.api.Scenario;

@FunctionalInterface
public interface HookBody {
    void accept(Scenario scenario) throws Throwable;
}
