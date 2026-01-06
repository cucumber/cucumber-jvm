package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.util.Optional;

@API(status = API.Status.STABLE)
public interface HookDefinition extends Located {

    void execute(TestCaseState state);

    String getTagExpression();

    int getOrder();

    default Optional<HookType> getHookType() {
        return Optional.empty();
    }

    enum HookType {

        BEFORE,

        AFTER,

        BEFORE_STEP,

        AFTER_STEP
    }
}
