package io.cucumber.core.event;

import static java.util.Locale.ROOT;

public enum HookType {
    Before, After, BeforeStep, AfterStep;

    @Override
    public String toString() {
        return super.toString().toLowerCase(ROOT);
    }
}
