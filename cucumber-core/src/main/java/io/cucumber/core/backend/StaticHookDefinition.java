package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.util.Optional;

@API(status = API.Status.EXPERIMENTAL)
public interface StaticHookDefinition extends Located {

    void execute();

    int getOrder();

    default Optional<String> getName() {
        return Optional.empty();
    }

    default Optional<HookType> getHookType() {
        return Optional.empty();
    }

    enum HookType {
        BEFORE_ALL,
        AFTER_ALL
    }
}
