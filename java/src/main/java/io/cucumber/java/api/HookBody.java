package io.cucumber.java.api;

import io.cucumber.core.api.Scenario;

public interface HookBody {
    void accept(Scenario scenario) throws Throwable;
}
