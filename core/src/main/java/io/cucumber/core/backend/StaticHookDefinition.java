package io.cucumber.core.backend;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface StaticHookDefinition extends Located {

    void execute();

    int getOrder();
}
