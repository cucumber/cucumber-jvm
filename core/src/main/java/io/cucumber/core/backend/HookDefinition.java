package io.cucumber.core.backend;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface HookDefinition extends Located {

    void execute(TestCaseState state);

    String getTagExpression();

    int getOrder();

}
