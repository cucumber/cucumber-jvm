package io.cucumber.core.api.event;

public enum HookType {
    Before, After, BeforeStep, AfterStep;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
