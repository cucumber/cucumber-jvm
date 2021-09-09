package io.cucumber.plugin.event;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public enum HookType {
    BEFORE, AFTER, BEFORE_STEP, AFTER_STEP
}
